package com.methoda.tranquillo.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.methoda.tranquillo.PerfectlyTranquilloApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Snapshot of today's state — driven by Room (entries/resources + habit fills)
 * plus in-memory (intent, goodThing, currentPhase, action flags) in sub-project
 * #2/#3/#4.
 */
data class TodayState(
    val entries: Map<Pair<ResourceKey, Phase>, EntryPair> = emptyMap(),
    val resources: Map<ResourceKey, AmPmFill> = emptyMap(),
    val intent: String = "",
    val goodThing: String = "",
    val currentPhase: Phase = Phase.Am,
    val morningMood: String = "",
    val morningDone: Boolean = false,
    val eveningDone: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app: PerfectlyTranquilloApp = application as PerfectlyTranquilloApp
    private val repo: MandalaRepository = MandalaRepository(app.db.mandalaEntryDao())
    private val stonesRepo: StonesRepository = StonesRepository(app.db.stoneDao())
    private val goodThingDao = app.db.goodThingDao()
    private val habits: HabitsRepository = HabitsRepository(
        habitDao = app.db.habitDao(),
        weeklyHabitDao = app.db.weeklyHabitDao(),
        habitFillDao = app.db.habitFillDao(),
        stonesRepo = stonesRepo,
        reminderScheduler = app.reminderScheduler
    )

    // In-memory today pieces.
    private val intent = MutableStateFlow("")
    private val goodThing = MutableStateFlow("")
    private val phaseOverride = MutableStateFlow<Phase?>(null)
    private val morningMood = MutableStateFlow("")
    private val morningDone = MutableStateFlow(false)
    private val eveningDone = MutableStateFlow(false)

    /** Tick that advances every time we cross the 05:00 day boundary — used
     *  to recompute the week-start cutoff for the Garden jar. */
    private val weekTick = MutableStateFlow(0)

    /** Shells collected this week. The week starts on Sunday at 05:00 local
     *  (so Saturday night → Sunday morning is the reset). */
    val stones: StateFlow<List<StoneKind>> =
        weekTick.flatMapLatest { stonesRepo.stonesSince(currentWeekStartMs()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userName: StateFlow<String> = app.prefs.userName.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_USER_NAME
    )

    val palette: StateFlow<String> = app.prefs.palette.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_PALETTE
    )

    val notifMode: StateFlow<String> = app.prefs.notifMode.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_NOTIF_MODE
    )

    val soundEnabled: StateFlow<Boolean> = app.prefs.sound.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_SOUND
    )

    val ambientSound: StateFlow<String> = app.prefs.ambientSound.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_AMBIENT_SOUND
    )

    val notifIcon: StateFlow<String> = app.prefs.notifIcon.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_NOTIF_ICON
    )

    val fontPair: StateFlow<String> = app.prefs.fontPair.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_FONT_PAIR
    )

    /** ISO date for the day the rest of the app considers "today". The day
     *  boundary is 05:00 local — entries made between 00:00 and 04:59 are
     *  attributed to the previous calendar date. Bumped at 05:00 by the
     *  rollover coroutine in [init]. */
    private val currentDate = MutableStateFlow(isoEffectiveToday())

    /** Days since the user started using the app (or last Clear All) — used
     *  by the Home eyebrow ("DAY 1", "DAY 12", etc.). 1-based. */
    val dayOfJourney: StateFlow<Int> =
        currentDate.flatMapLatest { date ->
            app.prefs.journeyStartDate.map { start ->
                if (start.isBlank()) 1
                else daysBetween(start, date).coerceAtLeast(0) + 1
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    /**
     * Whether to gate-show the Morning screen on app open. True if the local
     * hour is in the "morning" window (5..13) AND the user hasn't completed
     * (or dismissed) Morning today. Persisted via [PrefsStore.morningDoneDate].
     */
    val shouldShowMorningGate: StateFlow<Boolean> =
        currentDate.flatMapLatest { date ->
            app.prefs.morningDoneDate.map { doneDate ->
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                hour in MORNING_HOUR_START..MORNING_HOUR_END_INCLUSIVE && doneDate != date
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** 7-day average fill per resource (rolling window, today inclusive). */
    val sevenDayAverages: StateFlow<Map<ResourceKey, Float>> =
        currentDate.flatMapLatest { date ->
            repo.averageFillsInRange(isoDaysAgoOf(date, 6), date, days = 7)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Per-day mandala fills for the last 7 days (today inclusive). Used by
     *  the Garden week strip. Missing dates fall through as empty maps. */
    val weekFills: StateFlow<Map<String, Map<ResourceKey, AmPmFill>>> =
        currentDate.flatMapLatest { date ->
            repo.fillsByDateInRange(isoDaysAgoOf(date, 6), date)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Mandala entries from the last 30 days, newest first. Garden archive. */
    val archiveEntries: StateFlow<List<MandalaEntryEntity>> =
        currentDate.flatMapLatest { date ->
            repo.entriesInRange(isoDaysAgoOf(date, 30), date)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** "Ser emot från" archive — last 30 days of good_thing entries. */
    val goodThingsArchive: StateFlow<List<GoodThingEntity>> =
        currentDate.flatMapLatest { date ->
            goodThingDao.inRange(isoDaysAgoOf(date, 30), date)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Today's mandala resources — derived purely from saved mandala entries.
     * Petals never auto-fill from habits or action flows; only an explicit
     * mandala entry adds fill.
     */
    val today: StateFlow<TodayState> = currentDate.flatMapLatest { date ->
        combine(
            listOf(
                repo.entryMapForDate(date),
                repo.fillsForDate(date),
                intent,
                goodThing,
                phaseOverride,
                morningMood,
                morningDone,
                eveningDone
            )
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val entries = values[0] as Map<Pair<ResourceKey, Phase>, EntryPair>
            @Suppress("UNCHECKED_CAST")
            val baseResources = values[1] as Map<ResourceKey, AmPmFill>
            val intentText = values[2] as String
            val goodThingText = values[3] as String
            val override = values[4] as Phase?
            val moodText = values[5] as String
            val mDone = values[6] as Boolean
            val eDone = values[7] as Boolean

            TodayState(
                entries = entries,
                resources = baseResources,
                intent = intentText,
                goodThing = goodThingText,
                currentPhase = override ?: autoPhase(),
                morningMood = moodText,
                morningDone = mDone,
                eveningDone = eDone
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TodayState())

    val dailyHabits: StateFlow<List<HabitUi>> =
        currentDate.flatMapLatest { date ->
            habits.dailyHabitsUi(todayProvider = { date })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val weeklyHabits: StateFlow<List<WeeklyHabitUi>> =
        currentDate.flatMapLatest { date ->
            habits.weeklyHabitsUi(todayProvider = { date })
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Drive the ambient audio engine from the (sound × ambient) combo.
        viewModelScope.launch {
            combine(soundEnabled, ambientSound) { on, id -> on to id }
                .collect { (on, id) -> app.ambientPlayer.apply(on, id) }
        }
        // Restore morningDone (in-memory) from persisted morningDoneDate so that
        // re-opening the app same-day after a process death doesn't re-prompt.
        viewModelScope.launch {
            val savedDate = app.prefs.morningDoneDate.first()
            if (savedDate == currentDate.value) morningDone.value = true
        }
        // Restore the "Looking forward to" text from the good_things table.
        viewModelScope.launch {
            val saved = goodThingDao.forDate(currentDate.value)?.text.orEmpty()
            if (saved.isNotBlank()) goodThing.value = saved
        }
        // First-launch seed for the journey day-counter.
        viewModelScope.launch {
            val start = app.prefs.journeyStartDate.first()
            if (start.isBlank()) app.prefs.setJourneyStartDate(currentDate.value)
        }
        // 5am rollover loop — when crossing the day boundary the in-memory
        // layer (intent / goodThing / mood / morningDone / eveningDone) is
        // cleared and `currentDate` advances so all date-keyed flows re-query.
        // weekTick also bumps so the Garden jar recomputes its Sunday cutoff.
        viewModelScope.launch {
            while (isActive) {
                delay(msUntilNextDayBoundary() + 1_000L)
                currentDate.value = isoEffectiveToday()
                weekTick.value = weekTick.value + 1
                clearTodayInMemory()
            }
        }
    }

    fun saveMandalaEntry(
        key: ResourceKey,
        phase: Phase,
        resource: String,
        challenge: String
    ) {
        viewModelScope.launch {
            val date = currentDate.value
            repo.saveEntry(date, key, phase, "resource", resource.trim())
            repo.saveEntry(date, key, phase, "challenge", challenge.trim())
            maybeAwardMandalaCompleteShell(date)
        }
    }

    /** If today now has at least one entry for every one of the 8 resource
     *  petals (regardless of phase) and we haven't already awarded today's
     *  mandala-complete shell, award one. */
    private suspend fun maybeAwardMandalaCompleteShell(date: String) {
        val entries = repo.entriesForDate(date).first()
        val filledKeys = entries
            .filter { it.text.isNotBlank() }
            .mapNotNull { runCatching { ResourceKey.valueOf(it.key) }.getOrNull() }
            .toSet()
        if (filledKeys.size < ResourceKey.orderedClockwise.size) return
        val already = stonesRepo.countSinceBySource(
            source = MANDALA_COMPLETE_SOURCE,
            sinceMs = startOfEffectiveTodayMs()
        )
        if (already > 0) return
        stonesRepo.addStone(StoneKind.Shell, source = MANDALA_COMPLETE_SOURCE)
    }

    private fun clearTodayInMemory() {
        intent.value = ""
        goodThing.value = ""
        morningMood.value = ""
        morningDone.value = false
        eveningDone.value = false
        phaseOverride.value = null
    }

    /** ms until the next 05:00 local — the app's day boundary. */
    private fun msUntilNextDayBoundary(): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, DAY_BOUNDARY_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return (next.timeInMillis - now.timeInMillis).coerceAtLeast(1L)
    }

    fun setIntent(text: String) { intent.value = text }
    /** "Looking forward to" / Ser emot från — persists in the good_things
     *  table (its own dedicated section in the Garden, separate from Spirit). */
    fun setGoodThing(text: String) {
        goodThing.value = text
        viewModelScope.launch {
            val date = currentDate.value
            val cleaned = text.trim()
            if (cleaned.isBlank()) goodThingDao.deleteForDate(date)
            else goodThingDao.upsert(GoodThingEntity(date = date, text = cleaned))
        }
    }
    fun setPhase(p: Phase) { phaseOverride.value = p }

    // ---- action flow state (#4) -----------------------------------------

    fun setMorningMood(mood: String) { morningMood.value = mood }
    fun setMorningDone(done: Boolean) { morningDone.value = done }
    fun setEveningDone(done: Boolean) { eveningDone.value = done }

    /** Append a stone — persisted via [StonesRepository]. */
    fun addStone(kind: StoneKind, source: String = "") {
        viewModelScope.launch { stonesRepo.addStone(kind, source) }
    }

    // ---- habit actions ---------------------------------------------------

    fun toggleDailyHabit(id: String) {
        viewModelScope.launch { habits.toggleDaily(id) }
    }

    fun toggleWeeklyHabit(id: String) {
        viewModelScope.launch { habits.toggleWeekly(id) }
    }

    fun addDailyHabit(label: String, hint: String = "") {
        viewModelScope.launch { habits.addDailyHabit(label, hint) }
    }

    fun addWeeklyHabit(label: String, hint: String = "", day: Int) {
        viewModelScope.launch { habits.addWeeklyHabit(label, hint, day) }
    }

    fun removeDailyHabit(id: String) {
        viewModelScope.launch { habits.removeDaily(id) }
    }

    fun removeWeeklyHabit(id: String) {
        viewModelScope.launch { habits.removeWeekly(id) }
    }

    fun setReminder(id: String, isWeekly: Boolean, time: String?) {
        viewModelScope.launch {
            if (isWeekly) habits.setWeeklyReminder(id, time)
            else          habits.setDailyReminder(id, time)
        }
    }

    // ---- settings (#7) --------------------------------------------------

    fun setUserName(name: String) {
        viewModelScope.launch { app.prefs.setUserName(name.trim()) }
    }

    fun setPalette(id: String) {
        viewModelScope.launch { app.prefs.setPalette(id) }
    }

    fun setNotifMode(mode: String) {
        viewModelScope.launch { app.prefs.setNotifMode(mode) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { app.prefs.setSound(enabled) }
    }

    fun setAmbientSound(id: String) {
        viewModelScope.launch { app.prefs.setAmbientSound(id) }
    }

    fun setNotifIcon(id: String) {
        viewModelScope.launch { app.prefs.setNotifIcon(id) }
    }

    /** Mark Morning seen for today (suppresses the gate). Called by both the
     *  "Begin the day" button and the X dismiss. The button additionally
     *  awards the Moon stone via the existing flow. */
    fun markMorningSeenToday() {
        viewModelScope.launch {
            app.prefs.setMorningDoneDate(currentDate.value)
        }
    }

    fun setFontPair(id: String) {
        viewModelScope.launch { app.prefs.setFontPair(id) }
    }

    /** Reset today's mandala entries + habit fills + uncheck today's habits +
     *  clear today's "Looking forward to". */
    fun resetToday() {
        viewModelScope.launch {
            val today = currentDate.value
            app.db.mandalaEntryDao().deleteAllForDate(today)
            app.db.habitFillDao().deleteAllForDate(today)
            app.db.habitDao().clearLastDoneIfDate(today)
            app.db.weeklyHabitDao().clearLastDoneIfDate(today)
            goodThingDao.deleteForDate(today)
            clearTodayInMemory()
        }
    }

    /** Wipe all rows — mandala, habits, fills, stones, good things. Resets
     *  the journey-day counter to 1. Keeps user prefs (palette, name, etc.).
     *  clearAllTables() is a blocking Room call that must run off the main
     *  thread, otherwise it throws IllegalStateException. */
    fun clearAll() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                app.db.clearAllTables()
            }
            HabitSeeder.seedAllIfEmpty(app.db)
            app.prefs.setJourneyStartDate(currentDate.value)
            app.prefs.setMorningDoneDate("")
            clearTodayInMemory()
        }
    }

    /** Fire a sample notification immediately (Settings demo button). */
    fun fireDemoReminder() {
        viewModelScope.launch {
            val iconRes = com.methoda.tranquillo.notifications.HabitReminderWorker
                .iconResForId(app.prefs.notifIconNow())
            com.methoda.tranquillo.notifications.HabitReminderWorker.postNotification(
                context = app,
                habitId = "demo",
                label = "A small thing",
                hint = "this is what a reminder feels like — gentle.",
                iconRes = iconRes
            )
        }
    }

    companion object {
        /** Tag used on the Shell stone awarded when all 8 petals are filled. */
        const val MANDALA_COMPLETE_SOURCE = "mandala-complete"

        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        fun isoToday(): String = DATE_FORMAT.format(Date())

        /** The hour at which a new "day" begins for the app — entries made
         *  between 00:00 and DAY_BOUNDARY_HOUR-1:59 belong to the previous
         *  calendar date. */
        const val DAY_BOUNDARY_HOUR = 5

        /** ISO date treating the night as part of the previous day: when the
         *  current local hour is < [DAY_BOUNDARY_HOUR], returns yesterday. */
        fun isoEffectiveToday(): String {
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.HOUR_OF_DAY) < DAY_BOUNDARY_HOUR) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            return DATE_FORMAT.format(cal.time)
        }

        /** Epoch-ms of the start of the current week, which is the most recent
         *  Sunday at 05:00 local. Saturday night up to Sunday 04:59 still
         *  counts as the previous week (matches [isoEffectiveToday] semantics).
         */
        fun currentWeekStartMs(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, DAY_BOUNDARY_HOUR)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Walk backward to the most recent Sunday at 05:00 that is
            // <= now. Caps at 7 iterations.
            val now = System.currentTimeMillis()
            var safety = 0
            while ((cal.timeInMillis > now ||
                    cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) &&
                safety < 8) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                safety += 1
            }
            return cal.timeInMillis
        }

        /** Epoch-ms of the start of [isoEffectiveToday] — i.e. the most recent
         *  05:00 boundary. Used to scope per-day counts (e.g. "did we already
         *  award today's mandala-complete shell?"). */
        fun startOfEffectiveTodayMs(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, DAY_BOUNDARY_HOUR)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis > System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }
            return cal.timeInMillis
        }

        /** Inclusive: morning window is 05:00..13:59 (matches autoPhase). */
        const val MORNING_HOUR_START = 5
        const val MORNING_HOUR_END_INCLUSIVE = 13

        /** ISO date for [n] days before today. n=0 → today; n=6 → 6 days ago. */
        fun isoDaysAgo(n: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -n)
            return DATE_FORMAT.format(cal.time)
        }

        /** ISO date for [n] days before [base]. */
        fun isoDaysAgoOf(base: String, n: Int): String {
            val cal = Calendar.getInstance()
            runCatching { cal.time = DATE_FORMAT.parse(base) ?: return@runCatching }
            cal.add(Calendar.DAY_OF_YEAR, -n)
            return DATE_FORMAT.format(cal.time)
        }

        /** Days between two ISO dates (b - a). Negative if a > b. */
        fun daysBetween(a: String, b: String): Int {
            val ca = runCatching { DATE_FORMAT.parse(a) }.getOrNull() ?: return 0
            val cb = runCatching { DATE_FORMAT.parse(b) }.getOrNull() ?: return 0
            val diffMs = cb.time - ca.time
            return (diffMs / 86_400_000L).toInt()
        }

        fun autoPhase(): Phase {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            // Morning: 5am–1:59pm. Evening: 2pm–4:59am.
            return if (hour in 5..13) Phase.Am else Phase.Pm
        }

        fun dayOfYear(): Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        fun dayOfWeekShort(): String {
            val cal = Calendar.getInstance()
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY    -> "Monday"
                Calendar.TUESDAY   -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY  -> "Thursday"
                Calendar.FRIDAY    -> "Friday"
                Calendar.SATURDAY  -> "Saturday"
                else               -> "Sunday"
            }
        }

        /** Today's day-of-week as a 0..6 index (Sunday=0) matching the prototype. */
        fun dayOfWeekIndex(): Int {
            return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY    -> 0
                Calendar.MONDAY    -> 1
                Calendar.TUESDAY   -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY  -> 4
                Calendar.FRIDAY    -> 5
                Calendar.SATURDAY  -> 6
                else               -> 0
            }
        }

    }
}
