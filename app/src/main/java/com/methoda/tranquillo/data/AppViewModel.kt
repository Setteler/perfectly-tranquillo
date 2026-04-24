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
 * plus in-memory (intent, goodThing, currentPhase) in sub-project #2/#3.
 */
data class TodayState(
    val entries: Map<Pair<ResourceKey, Phase>, EntryPair> = emptyMap(),
    val resources: Map<ResourceKey, AmPmFill> = emptyMap(),
    val intent: String = "",
    val goodThing: String = "",
    val currentPhase: Phase = Phase.Am
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

    val userName: StateFlow<String> = app.prefs.userName.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_USER_NAME
    )

    private val todayDate: String get() = isoToday()

    /**
     * Today's mandala resources — entry-derived fills *plus* habit-fill bumps
     * (+0.15 per completed mapped habit), applied to the PM half and capped at
     * 1.0. Per design README § "Resource↔habit/action mapping".
     */
    val today: StateFlow<TodayState> = combine(
        repo.entryMapForDate(todayDate),
        repo.fillsForDate(todayDate),
        habits.fillTotalsForDate(todayDate),
        intent,
        goodThing,
        phaseOverride
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

        val merged = mergeResources(baseResources, habitBumps)

        TodayState(
            entries = entries,
            resources = merged,
            intent = intentText,
            goodThing = goodThingText,
            currentPhase = override ?: autoPhase()
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
            habitBumps: Map<ResourceKey, Float>
        ): Map<ResourceKey, AmPmFill> {
            if (habitBumps.isEmpty()) return base
            val out = base.toMutableMap()
            for ((key, bump) in habitBumps) {
                val current = out[key] ?: AmPmFill()
                val newPm = (current.pm + bump).coerceIn(0f, 1f)
                out[key] = current.copy(pm = newPm)
            }
            return out
        }
    }
}
