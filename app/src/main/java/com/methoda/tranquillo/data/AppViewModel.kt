package com.methoda.tranquillo.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.methoda.tranquillo.PerfectlyTranquilloApp
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

    /** Stones collected — persisted to Room (#5). */
    val stones: StateFlow<List<StoneKind>> = stonesRepo.allStones
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Additive per-resource fill bumps awarded by action screens (#4). */
    private val actionFills = MutableStateFlow<Map<Pair<ResourceKey, Phase>, Float>>(emptyMap())

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

    val fontPair: StateFlow<String> = app.prefs.fontPair.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_FONT_PAIR
    )

    /** ISO date for the day the rest of the app considers "today". Bumped at
     *  midnight by the rollover coroutine in [init]. */
    private val currentDate = MutableStateFlow(isoToday())

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

    /**
     * Today's mandala resources — entry-derived fills *plus* habit-fill bumps
     * (+0.15 per completed mapped habit), *plus* action-flow bumps (#4), all
     * capped at 1.0.
     */
    val today: StateFlow<TodayState> = currentDate.flatMapLatest { date ->
        combine(
            listOf(
                repo.entryMapForDate(date),
                repo.fillsForDate(date),
                habits.fillTotalsForDate(date),
                intent,
                goodThing,
                phaseOverride,
                actionFills,
                morningMood,
                morningDone,
                eveningDone
            )
        ) { values ->
        @Suppress("UNCHECKED_CAST")
        val entries = values[0] as Map<Pair<ResourceKey, Phase>, EntryPair>
        @Suppress("UNCHECKED_CAST")
        val baseResources = values[1] as Map<ResourceKey, AmPmFill>
        @Suppress("UNCHECKED_CAST")
        val habitBumps = values[2] as Map<ResourceKey, Float>
        val intentText = values[3] as String
        val goodThingText = values[4] as String
        val override = values[5] as Phase?
        @Suppress("UNCHECKED_CAST")
        val actionBumps = values[6] as Map<Pair<ResourceKey, Phase>, Float>
        val moodText = values[7] as String
        val mDone = values[8] as Boolean
        val eDone = values[9] as Boolean

        val merged = mergeResources(baseResources, habitBumps, actionBumps)

            TodayState(
                entries = entries,
                resources = merged,
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
        // Restore morningDone (in-memory) from persisted morningDoneDate so that
        // re-opening the app same-day after a process death doesn't re-prompt.
        viewModelScope.launch {
            val savedDate = app.prefs.morningDoneDate.first()
            if (savedDate == currentDate.value) morningDone.value = true
        }
        // Midnight rollover loop — when crossing 00:00 the in-memory layer
        // (intent / goodThing / mood / morningDone / eveningDone / actionFills)
        // is cleared and `currentDate` advances so all date-keyed flows re-query.
        viewModelScope.launch {
            while (isActive) {
                delay(msUntilNextMidnight() + 1_000L)
                currentDate.value = isoToday()
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
        }
    }

    private fun clearTodayInMemory() {
        intent.value = ""
        goodThing.value = ""
        morningMood.value = ""
        morningDone.value = false
        eveningDone.value = false
        actionFills.value = emptyMap()
        phaseOverride.value = null
    }

    private fun msUntilNextMidnight(): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (next.timeInMillis - now.timeInMillis).coerceAtLeast(1L)
    }

    fun setIntent(text: String) { intent.value = text }
    fun setGoodThing(text: String) { goodThing.value = text }
    fun setPhase(p: Phase) { phaseOverride.value = p }

    // ---- action flow state (#4) -----------------------------------------

    fun setMorningMood(mood: String) { morningMood.value = mood }
    fun setMorningDone(done: Boolean) { morningDone.value = done }
    fun setEveningDone(done: Boolean) { eveningDone.value = done }

    /** Append a stone — persisted via [StonesRepository]. */
    fun addStone(kind: StoneKind, source: String = "") {
        viewModelScope.launch { stonesRepo.addStone(kind, source) }
    }

    /**
     * Add [delta] to the action-fill layer for ([key], [phase]). Caller does
     * not need to clamp — merging + display both cap at 1.0.
     */
    fun addResourceFill(key: ResourceKey, phase: Phase, delta: Float) {
        viewModelScope.launch {
            val cur = actionFills.value
            val k = key to phase
            val next = ((cur[k] ?: 0f) + delta).coerceIn(0f, 1f)
            actionFills.value = cur.toMutableMap().also { it[k] = next }
        }
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
        viewModelScope.launch { app.prefs.setUserName(name.trim().ifEmpty { PrefsStore.DEFAULT_USER_NAME }) }
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

    /** Reset today's mandala entries + habit fills + uncheck today's habits. */
    fun resetToday() {
        viewModelScope.launch {
            val today = currentDate.value
            app.db.mandalaEntryDao().deleteAllForDate(today)
            app.db.habitFillDao().deleteAllForDate(today)
            app.db.habitDao().clearLastDoneIfDate(today)
            app.db.weeklyHabitDao().clearLastDoneIfDate(today)
            clearTodayInMemory()
        }
    }

    /** Wipe all rows — mandala, habits, fills, stones. Keeps prefs. */
    fun clearAll() {
        viewModelScope.launch {
            app.db.clearAllTables()
            // Re-seed default habits so the user isn't stuck on an empty Habits screen.
            HabitSeeder.seedAllIfEmpty(app.db)
            clearTodayInMemory()
        }
    }

    /** Fire a sample notification immediately (QA hook for #6 / #7 demo button). */
    fun fireDemoReminder() {
        com.methoda.tranquillo.notifications.HabitReminderWorker.postNotification(
            context = app,
            habitId = "demo",
            label = "A small thing",
            hint = "this is what a reminder feels like — gentle."
        )
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        fun isoToday(): String = DATE_FORMAT.format(Date())

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

        fun mergeResources(
            base: Map<ResourceKey, AmPmFill>,
            habitBumps: Map<ResourceKey, Float>,
            actionBumps: Map<Pair<ResourceKey, Phase>, Float> = emptyMap()
        ): Map<ResourceKey, AmPmFill> {
            if (habitBumps.isEmpty() && actionBumps.isEmpty()) return base
            val out = base.toMutableMap()
            for ((key, bump) in habitBumps) {
                val current = out[key] ?: AmPmFill()
                val newPm = (current.pm + bump).coerceIn(0f, 1f)
                out[key] = current.copy(pm = newPm)
            }
            for ((keyPhase, bump) in actionBumps) {
                val (key, phase) = keyPhase
                val current = out[key] ?: AmPmFill()
                out[key] = when (phase) {
                    Phase.Am -> current.copy(am = (current.am + bump).coerceIn(0f, 1f))
                    Phase.Pm -> current.copy(pm = (current.pm + bump).coerceIn(0f, 1f))
                }
            }
            return out
        }
    }
}
