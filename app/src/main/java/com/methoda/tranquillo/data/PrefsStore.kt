package com.methoda.tranquillo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "perfectly_tranquillo_prefs")

class PrefsStore(private val context: Context) {

    val sound: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_SOUND] ?: DEFAULT_SOUND }

    val fontPair: Flow<String> =
        context.dataStore.data.map { it[KEY_FONT_PAIR] ?: DEFAULT_FONT_PAIR }

    val userName: Flow<String> =
        context.dataStore.data.map { it[KEY_USER_NAME] ?: DEFAULT_USER_NAME }

    val palette: Flow<String> =
        context.dataStore.data.map { it[KEY_PALETTE] ?: DEFAULT_PALETTE }

    val notifMode: Flow<String> =
        context.dataStore.data.map { it[KEY_NOTIF_MODE] ?: DEFAULT_NOTIF_MODE }

    /** ISO date the user last completed (or dismissed) the Morning gate. */
    val morningDoneDate: Flow<String> =
        context.dataStore.data.map { it[KEY_MORNING_DONE_DATE] ?: "" }

    val ambientSound: Flow<String> =
        context.dataStore.data.map { it[KEY_AMBIENT_SOUND] ?: DEFAULT_AMBIENT_SOUND }

    /** ISO date the user started using the app (or last reset everything).
     *  Empty until first read; the AppViewModel seeds it on first launch. */
    val journeyStartDate: Flow<String> =
        context.dataStore.data.map { it[KEY_JOURNEY_START_DATE] ?: "" }

    val notifIcon: Flow<String> =
        context.dataStore.data.map { it[KEY_NOTIF_ICON] ?: DEFAULT_NOTIF_ICON }

    suspend fun setSound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setFontPair(pair: String) {
        context.dataStore.edit { it[KEY_FONT_PAIR] = pair }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun setPalette(id: String) {
        context.dataStore.edit { it[KEY_PALETTE] = id }
    }

    suspend fun setNotifMode(mode: String) {
        context.dataStore.edit { it[KEY_NOTIF_MODE] = mode }
    }

    suspend fun setMorningDoneDate(date: String) {
        context.dataStore.edit { it[KEY_MORNING_DONE_DATE] = date }
    }

    suspend fun setAmbientSound(id: String) {
        context.dataStore.edit { it[KEY_AMBIENT_SOUND] = id }
    }

    suspend fun setJourneyStartDate(date: String) {
        context.dataStore.edit { it[KEY_JOURNEY_START_DATE] = date }
    }

    suspend fun setNotifIcon(id: String) {
        context.dataStore.edit { it[KEY_NOTIF_ICON] = id }
    }

    /** Read [notifIcon] eagerly off the datastore — used by Workers. */
    suspend fun notifIconNow(): String =
        context.dataStore.data.map { it[KEY_NOTIF_ICON] ?: DEFAULT_NOTIF_ICON }.first()

    /** Read [notifMode] eagerly off the datastore — used by Workers running outside the UI. */
    suspend fun notifModeNow(): String =
        context.dataStore.data.map { it[KEY_NOTIF_MODE] ?: DEFAULT_NOTIF_MODE }.first()

    companion object {
        const val DEFAULT_SOUND = true
        const val DEFAULT_FONT_PAIR = "caveat"
        // Empty default so the Settings field starts blank with a placeholder.
        // Display layers (e.g. Home greeting) fall back to "friend" when blank.
        const val DEFAULT_USER_NAME = ""
        const val USER_NAME_FALLBACK = "friend"
        const val DEFAULT_PALETTE = "deep_tide"
        const val DEFAULT_NOTIF_MODE = "sound"
        const val DEFAULT_AMBIENT_SOUND = "none"
        const val DEFAULT_NOTIF_ICON = "waves"

        const val NOTIF_SILENT = "silent"
        const val NOTIF_SOUND = "sound"
        const val NOTIF_VIBRATE = "vibrate"

        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_FONT_PAIR = stringPreferencesKey("font_pair")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_PALETTE = stringPreferencesKey("palette")
        private val KEY_NOTIF_MODE = stringPreferencesKey("notif_mode")
        private val KEY_MORNING_DONE_DATE = stringPreferencesKey("morning_done_date")
        private val KEY_AMBIENT_SOUND = stringPreferencesKey("ambient_sound")
        private val KEY_JOURNEY_START_DATE = stringPreferencesKey("journey_start_date")
        private val KEY_NOTIF_ICON = stringPreferencesKey("notif_icon")
    }
}
