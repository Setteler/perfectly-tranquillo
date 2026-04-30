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

    /** Read [notifMode] eagerly off the datastore — used by Workers running outside the UI. */
    suspend fun notifModeNow(): String =
        context.dataStore.data.map { it[KEY_NOTIF_MODE] ?: DEFAULT_NOTIF_MODE }.first()

    companion object {
        const val DEFAULT_SOUND = true
        const val DEFAULT_FONT_PAIR = "fraunces"
        const val DEFAULT_USER_NAME = "friend"
        const val DEFAULT_PALETTE = "deep_tide"
        const val DEFAULT_NOTIF_MODE = "sound"

        const val NOTIF_SILENT = "silent"
        const val NOTIF_SOUND = "sound"
        const val NOTIF_VIBRATE = "vibrate"

        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_FONT_PAIR = stringPreferencesKey("font_pair")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_PALETTE = stringPreferencesKey("palette")
        private val KEY_NOTIF_MODE = stringPreferencesKey("notif_mode")
    }
}
