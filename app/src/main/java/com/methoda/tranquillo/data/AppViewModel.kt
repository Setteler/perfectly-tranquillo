package com.methoda.tranquillo.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.methoda.tranquillo.PerfectlyTranquilloApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val app: PerfectlyTranquilloApp = application as PerfectlyTranquilloApp
    private val repo: MandalaRepository = MandalaRepository(app.db.mandalaEntryDao())
    private val habits: HabitsRepository = HabitsRepository(
        habitDao = app.db.habitDao(),
        weeklyHabitDao = app.db.weeklyHabitDao(),
        habitFillDao = app.db.habitFillDao(),
        reminderScheduler = app.reminderScheduler
    )
    private val stonesRepo: StonesRepository = StonesRepository(app.db.stoneDao())

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

    /** 7-day average fill per resource (rolling window, today inclusive). */
    val sevenDayAverages: StateFlow<Map<ResourceKey, Float>> =
        repo.averageFillsInRange(isoDaysAgo(6), isoToday(), days = 7)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

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

    private val todayDate: String get() = isoToday()

    /**
     * Today's mandala resources — entry-derived fills *plus* habit-fill bumps
     * (+0.15 per completed mapped habit), *plus* action-flow bumps (#4), all
     * capped at 1.0.
     */
    val today: StateFlow<TodayState> = combine(
        listOf(
            repo.entryMapForDate(todayDate),
            repo.fillsForDate(todayDate),
            habits.fillTotalsForDate(todayDate),
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TodayState())

    val dailyHabits: StateFlow<List<HabitUi>> =
        habits.dailyHabitsUi().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val weeklyHabits: StateFlow<List<WeeklyHabitUi>> =
        habits.weeklyHabitsUi().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveMandalaEntry(
        key: ResourceKey,
        phase: Phase,
        resource: String,
        challenge: String
    ) {
        viewModelScope.launch {
            repo.saveEntry(todayDate, key, phase, "resource", resource.trim())
            repo.saveEntry(todayDate, key, phase, "challenge", challenge.trim())
        }
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

    /** Reset today's mandala entries + habit fills + uncheck today's habits. */
    fun resetToday() {
        viewModelScope.launch {
            val today = isoToday()
            app.db.mandalaEntryDao().deleteAllForDate(today)
            app.db.habitFillDao().deleteAllForDate(today)
            app.db.habitDao().clearLastDoneIfDate(today)
            app.db.weeklyHabitDao().clearLastDoneIfDate(today)
            actionFills.value = emptyMap()
            morningDone.value = false
            eveningDone.value = false
            morningMood.value = ""
            intent.value = ""
            goodThing.value = ""
        }
    }

    /** Wipe all rows — mandala, habits, fills, stones. Keeps prefs. */
    fun clearAll() {
        viewModelScope.launch {
            app.db.clearAllTables()
            // Re-seed default habits so the user isn't stuck on an empty Habits screen.
            HabitSeeder.seedAllIfEmpty(app.db)
            actionFills.value = emptyMap()
            morningDone.value = false
            eveningDone.value = false
            morningMood.value = ""
            intent.value = ""
            goodThing.value = ""
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

        /** ISO date for [n] days before today. n=0 → today; n=6 → 6 days ago. */
        fun isoDaysAgo(n: Int): String {
            val cal = Calendar.getInstance()
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
