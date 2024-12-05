package de.domjos.cloudapp2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

@Suppress("unused")
class Settings(private val context: Context) {

    companion object {
        // internal settings
        val firstStartKey = booleanPreferencesKey("first_start")

        // general settings
        val timeSpanKey = floatPreferencesKey("user_time_span")
        val themeFromCloudKey = booleanPreferencesKey("user_theme_from_cloud")
        val themeFromCloudMobileKey = booleanPreferencesKey("user_theme_from_cloud_mobile")
        val footerViewModeKey = stringPreferencesKey("user_footer_view_mode")

        // notification settings
        val notificationTypeAppKey = booleanPreferencesKey("notification_type_app_key")
        val notificationTypeServerKey = booleanPreferencesKey("notification_type_server_key")
        val notificationTimeKey = floatPreferencesKey("notification_time_key")

        // contact settings
        val contactRegularityKey = floatPreferencesKey("user_contact_regularity")
        val calendarRegularityKey = floatPreferencesKey("user_calendar_regularity")

        // calendar settings
        val carDavRegularityKey = floatPreferencesKey("user_carDav_regularity")
        val calDavRegularityKey = floatPreferencesKey("user_calDav_regularity")

        // data settings
        val dataShowInInternalViewer = booleanPreferencesKey("user_data_show_internal")
        val dataShowPdfInInternalViewer = booleanPreferencesKey("user_data_show_pdf")
        val dataShowImageInInternalViewer = booleanPreferencesKey("user_data_show_image")
        val dataShowTextInInternalViewer = booleanPreferencesKey("user_data_show_text")
        val dataShowMarkDownInInternalViewer = booleanPreferencesKey("user_data_show_markdown")

        // features
        val featureNotifications = booleanPreferencesKey("feature_notifications")
        val featureData = booleanPreferencesKey("feature_data")
        val featureNotes = booleanPreferencesKey("feature_notes")
        val featureContacts = booleanPreferencesKey("feature_contacts")
        val featureCalendars = booleanPreferencesKey("feature_calendars")
        val featureToDos = booleanPreferencesKey("feature_todos")
        val featureChats = booleanPreferencesKey("feature_chats")
    }

    // general settings
    private var timeSpan: Float = 20.0f
    private var themeFromCloud: Boolean = true
    private var themeFromCloudMobile: Boolean = true
    private var footerViewMode: String = "Icon"

    // notification settings
    private var notificationTypeApp: Boolean = true
    private var notificationTypeServer: Boolean = true
    private var notificationTime: Float = 7.0f

    // contact settings
    private var contactRegularity: Float = 1.0f
    private var carDavRegularity: Float = 0.0f

    // calendar settings
    private var calendarRegularity: Float = 1.0f
    private var calDavRegularity: Float = 0.0f

    // data settings
    private var showDataInternal: Boolean = true
    private var showPdfInternal: Boolean = true
    private var showImgInternal: Boolean = true
    private var showTxtInternal: Boolean = true
    private var showMarkDownInternal: Boolean = true

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
            // general settings
            preferences[timeSpanKey] = timeSpan
            preferences[themeFromCloudKey] = themeFromCloud
            preferences[themeFromCloudMobileKey] = themeFromCloudMobile
            preferences[footerViewModeKey] = footerViewMode

            // notification settings
            preferences[notificationTypeAppKey] = notificationTypeApp
            preferences[notificationTypeServerKey] = notificationTypeServer
            preferences[notificationTimeKey] = notificationTime

            // contact settings
            preferences[contactRegularityKey] = contactRegularity
            preferences[carDavRegularityKey] = carDavRegularity

            // calendar settings
            preferences[calendarRegularityKey] = calendarRegularity
            preferences[calDavRegularityKey] = calDavRegularity

            // data settings
            preferences[dataShowInInternalViewer] = showDataInternal
            preferences[dataShowPdfInInternalViewer] = showPdfInternal
            preferences[dataShowImageInInternalViewer] = showImgInternal
            preferences[dataShowTextInInternalViewer] = showTxtInternal
            preferences[dataShowMarkDownInInternalViewer] = showMarkDownInternal

            // feature settings
            preferences[featureNotifications] = true
            preferences[featureData] = true
            preferences[featureNotes] = true
            preferences[featureContacts] = true
            preferences[featureCalendars] = true
            preferences[featureToDos] = true
            preferences[featureChats] = true
        }
    }

    fun getStore(): DataStore<Preferences> {
        return context.dataStore
    }
}