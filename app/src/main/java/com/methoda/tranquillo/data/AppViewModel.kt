package com.methoda.tranquillo.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.methoda.tranquillo.PerfectlyTranquilloApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Snapshot of today's state — driven by Room (entries/resources) + in-memory
 * (intent, goodThing, currentPhase) in sub-project #2. Persistence for intent
 * and goodThing is scheduled for #5 along with midnight rollover.
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

    // In-memory today pieces — persist in #5.
    private val intent = MutableStateFlow("")
    private val goodThing = MutableStateFlow("")
    private val phaseOverride = MutableStateFlow<Phase?>(null)

    val userName: StateFlow<String> = app.prefs.userName.stateIn(
        viewModelScope, SharingStarted.Eagerly, PrefsStore.DEFAULT_USER_NAME
    )

    private val todayDate: String get() = isoToday()

    val today: StateFlow<TodayState> = combine(
        repo.entryMapForDate(todayDate),
        repo.fillsForDate(todayDate),
        intent,
        goodThing,
        phaseOverride
    ) { entries, resources, intentText, goodThingText, override ->
        TodayState(
            entries = entries,
            resources = resources,
            intent = intentText,
            goodThing = goodThingText,
            currentPhase = override ?: autoPhase()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TodayState())

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
    }
}
