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
        val calendarRegularityKey = floatPreferencesKey("user_calendar_regularity")

        val cardavRegularityKey = floatPreferencesKey("user_cardav_regularity")
        val caldavRegularityKey = floatPreferencesKey("user_caldav_regularity")

        val themeFromCloudKey = booleanPreferencesKey("user_theme_from_cloud")
        val themeFromCloudMobileKey = booleanPreferencesKey("user_theme_from_cloud_mobile")

        val firstStartKey = booleanPreferencesKey("first_start")
    }

    var timeSpan: Float = 20.0f
    var contactRegularity: Float = 1.0f
    var calendarRegularity: Float = 1.0f
    var caldavRegularity: Float = 0.0f
    var cardavRegularity: Float = 0.0f
    var themeFromCloud: Boolean = true
    var themeFromCloudMobile: Boolean = true

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> getSetting(key: Preferences.Key<T>, default: T): T {
        val data = this.context.dataStore.data.first()
        val res = data[key] as T
        return res ?: default
    }

    suspend fun getTimeSpanSetting(): Float {
        return getSetting(timeSpanKey, 20.0f)
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
            preferences[calendarRegularityKey] = calendarRegularity
            preferences[cardavRegularityKey] = cardavRegularity
            preferences[caldavRegularityKey] = caldavRegularity
            preferences[themeFromCloudKey] = themeFromCloud
            preferences[themeFromCloudMobileKey] = themeFromCloudMobile
        }
    }

    fun getStore(): DataStore<Preferences> {
        return context.dataStore
    }
}