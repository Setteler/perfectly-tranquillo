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
        habitFillDao = app.db.habitFillDao()
    )

    // In-memory today pieces — persist in #5.
    private val intent = MutableStateFlow("")
    private val goodThing = MutableStateFlow("")
    private val phaseOverride = MutableStateFlow<Phase?>(null)
    private val morningMood = MutableStateFlow("")
    private val morningDone = MutableStateFlow(false)
    private val eveningDone = MutableStateFlow(false)

    /** Stones collected this session — #5 persists to a Stones table. */
    private val _stones = MutableStateFlow<List<StoneKind>>(emptyList())
    val stones: StateFlow<List<StoneKind>> = _stones

    /** Additive per-resource fill bumps awarded by action screens (#4). */
    private val actionFills = MutableStateFlow<Map<Pair<ResourceKey, Phase>, Float>>(emptyMap())

    val userName: StateFlow<String> = app.prefs.userName.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_USER_NAME
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

    /** Append a stone to the in-session pouch (persistence in #5). */
    fun addStone(kind: StoneKind) {
        viewModelScope.launch { _stones.value = _stones.value + kind }
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

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getDefault()
        }
        fun isoToday(): String = DATE_FORMAT.format(Date())

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
