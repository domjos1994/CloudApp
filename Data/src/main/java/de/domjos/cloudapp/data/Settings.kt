package de.domjos.cloudapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class Settings(private val context: Context) {

    companion object {
        val timeSpanKey = floatPreferencesKey("user_time_span")
        val contactRegularityKey = floatPreferencesKey("user_contact_regularity")

        val firstStartKey = booleanPreferencesKey("first_start")
    }

    var timeSpan: Float = 20.0f
    var contactRegularity: Float = 20.0f

    suspend fun <T> getSetting(key: Preferences.Key<T>, default: T): T {
        val data = this.context.dataStore.data.first()
        val res = data[key] as T
        return res ?: default
    }

    suspend fun <T> setSetting(key: Preferences.Key<T>, value: T) {
        this.context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun save() {
        this.context.dataStore.edit { preferences ->
            preferences[timeSpanKey] = timeSpan
            preferences[contactRegularityKey] = contactRegularity
        }
    }

    fun getStore(): DataStore<Preferences> {
        return context.dataStore
    }
}