package de.domjos.cloudapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class Settings(private val context: Context) {
    private val Context.userPreferenceDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

    companion object {
        val timeSpanKey = floatPreferencesKey("user_time_span")
    }

    var timeSpan: Float = 20.0f

    init {

        this.context.userPreferenceDataStore.data.map { preferences ->
            timeSpan = preferences[timeSpanKey] ?: 20.0F
        }
    }

    suspend fun save() {
        this.context.userPreferenceDataStore.edit { preferences ->
            preferences[timeSpanKey] = timeSpan
        }
    }

    fun getStore(): DataStore<Preferences> {
        return context.userPreferenceDataStore
    }
}