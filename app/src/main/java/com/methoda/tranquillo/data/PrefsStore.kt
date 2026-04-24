package com.methoda.tranquillo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "perfectly_tranquillo_prefs")

class PrefsStore(private val context: Context) {

    val sound: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_SOUND] ?: DEFAULT_SOUND }

    val fontPair: Flow<String> =
        context.dataStore.data.map { it[KEY_FONT_PAIR] ?: DEFAULT_FONT_PAIR }

    suspend fun setSound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setFontPair(pair: String) {
        context.dataStore.edit { it[KEY_FONT_PAIR] = pair }
    }

    companion object {
        const val DEFAULT_SOUND = true
        const val DEFAULT_FONT_PAIR = "fraunces"
        private val KEY_SOUND = booleanPreferencesKey("sound")
        private val KEY_FONT_PAIR = stringPreferencesKey("font_pair")
    }
}
