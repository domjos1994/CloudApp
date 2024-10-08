package de.domjos.cloudapp2.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp2.appbasics.R
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest
import de.domjos.cloudapp2.data.Settings


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val timeSpanPreference = createSeekBarPreference(
        Settings.timeSpanKey, 20.0f, R.string.settings_timeSpan_title, R.string.settings_timeSpan_header,
        R.drawable.baseline_access_time_24, {"${it.toInt()} min"}, 1.0f.rangeTo(200.0f)
    )

    // features
    val featureNotificationsPreference = createSwitchPreference(
        Settings.featureNotifications, true, stringResource(R.string.notifications),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.notifications)),
        R.drawable.baseline_notifications_24
    )
    val featureDataPreference = createSwitchPreference(
        Settings.featureData, true, stringResource(R.string.data),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.data)),
        R.drawable.baseline_folder_24
    )
    val featureNotesPreference = createSwitchPreference(
        Settings.featureNotes, true, stringResource(R.string.notes),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.notes)),
        R.drawable.baseline_note_24
    )
    val featureContactsPreference = createSwitchPreference(
        Settings.featureContacts, true, stringResource(R.string.contacts),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.contacts)),
        R.drawable.baseline_person_24
    )
    val featureCalendarsPreference = createSwitchPreference(
        Settings.featureCalendars, true, stringResource(R.string.calendars),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.calendars)),
        R.drawable.baseline_calendar_month_24
    )
    val featureToDosPreference = createSwitchPreference(
        Settings.featureToDos, true, stringResource(R.string.todos),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.todos)),
        R.drawable.baseline_check_24
    )
    val featureChatsPreference = createSwitchPreference(
        Settings.featureChats, true, stringResource(R.string.chats),
        String.format(stringResource(R.string.settings_features_show), stringResource(R.string.chats)),
        R.drawable.baseline_chat_24
    )

    // cloud
    val cloudThemePreference = createSwitchPreference(
        Settings.themeFromCloudKey, true, R.string.settings_theme_cloud_title,
        R.string.settings_theme_cloud_header, R.drawable.baseline_design_services_24
    )
    val cloudThemeMobilePreference = createSwitchPreference(
        Settings.themeFromCloudMobileKey, true, R.string.settings_theme_cloud_mobile_title,
        R.string.settings_theme_cloud_mobile_header, R.drawable.baseline_signal_wifi_connected_no_internet_4_24
    )

    // notifications
    val notificationTypeAppPreference = createSwitchPreference(
        key = Settings.notificationTypeAppKey,
        default = true,
        titleId = R.string.settings_notifications_type_app_title,
        headerId = R.string.settings_notifications_type_app_header,
        resId = R.drawable.baseline_notifications_24
    )
    val notificationTypeServerPreference = createSwitchPreference(
        key = Settings.notificationTypeServerKey,
        default = true,
        titleId = R.string.settings_notifications_type_server_title,
        headerId = R.string.settings_notifications_type_server_header,
        resId = R.drawable.baseline_notifications_24
    )
    val notificationTimePreference = createSeekBarPreference(
        key = Settings.notificationTimeKey,
        default = 7.0f,
        titleId = R.string.settings_notifications_time_title,
        headerId = R.string.settings_notifications_time_header,
        resId = R.drawable.baseline_access_time_24,
        representation = {"${it.toInt()} days"},
        range = 1.0f.rangeTo(365.0f)
    )

    // contacts
    val contactRegularityPreference = createSeekBarPreference(
        Settings.contactRegularityKey, 1.0f, R.string.settings_contact_regularity_title,
        R.string.settings_contact_regularity_header, R.drawable.baseline_access_time_24,
        {"${it.toInt()} min"}, 1.0f.rangeTo(60.0f)
    )
    val cardavRegularityPreference = createSeekBarPreference(
        Settings.cardavRegularityKey, 0.0f, R.string.settings_cardav_regularity_title,
        R.string.settings_cardav_regularity_header, R.drawable.baseline_access_time_24,
        {"${it.toInt()} min"}, 0.0f.rangeTo(10.0f)
    )

    // calendar
    val calendarRegularityPreference = createSeekBarPreference(
        Settings.calendarRegularityKey, 1.0f, R.string.settings_calendar_regularity_title,
        R.string.settings_calendar_regularity_header, R.drawable.baseline_access_time_24,
        {"${it.toInt()} min"}, 1.0f.rangeTo(60.0f)
    )
    val caldavRegularityPreference = createSeekBarPreference(
        Settings.caldavRegularityKey, 0.0f, R.string.settings_caldav_regularity_title,
        R.string.settings_caldav_regularity_header, R.drawable.baseline_access_time_24,
        {"${it.toInt()} min"}, 0.0f.rangeTo(10.0f)
    )

    // data
    val filesInInternal = createSwitchPreference(
        Settings.dataShowInInternalViewer, true, R.string.settings_data_show_internal_title,
        R.string.settings_data_show_internal_header,
        R.drawable.baseline_preview_24
    )
    val pdfInInternal = createSwitchPreference(
        Settings.dataShowPdfInInternalViewer, true, R.string.settings_data_show_pdf_title,
        R.string.settings_data_show_pdf_header, R.drawable.baseline_picture_as_pdf_24
    )
    val imgInInternal = createSwitchPreference(
        Settings.dataShowImageInInternalViewer, true, R.string.settings_data_show_img_title,
        R.string.settings_data_show_img_header, R.drawable.baseline_image_24
    )
    val textInInternal = createSwitchPreference(
        Settings.dataShowTextInInternalViewer, true, R.string.settings_data_show_txt_title,
        R.string.settings_data_show_txt_header, R.drawable.baseline_text_snippet_24
    )
    val mdInInternal = createSwitchPreference(
        Settings.dataShowMarkDownInInternalViewer, true, R.string.settings_data_show_md_title,
        R.string.settings_data_show_md_header, R.drawable.baseline_picture_as_pdf_24
    )

    val featureGroup = Preference.PreferenceGroup(stringResource(R.string.settings_features), true,
        listOf(
            featureNotificationsPreference, featureDataPreference, featureNotesPreference,
            featureContactsPreference, featureCalendarsPreference, featureToDosPreference,
            featureChatsPreference
        )
    )
    val cloudGroup = Preference.PreferenceGroup(stringResource(id = R.string.settings_cloud_title), true, listOf(cloudThemePreference, cloudThemeMobilePreference))
    val notificationGroup = Preference.PreferenceGroup(stringResource(R.string.settings_notifications), true, listOf(notificationTypeAppPreference, notificationTypeServerPreference, notificationTimePreference))
    val contactGroup = Preference.PreferenceGroup(stringResource(R.string.contacts), true, listOf(contactRegularityPreference, cardavRegularityPreference))
    val calendarGroup = Preference.PreferenceGroup(stringResource(R.string.calendars), true, listOf(calendarRegularityPreference, caldavRegularityPreference))
    val dataGroup = Preference.PreferenceGroup(stringResource(R.string.data), true, listOf(filesInInternal, pdfInInternal, imgInInternal, textInInternal, mdInInternal))

    PreferenceScreen(
        items = listOf(timeSpanPreference, featureGroup, cloudGroup, notificationGroup, contactGroup, calendarGroup, dataGroup),
        dataStore = viewModel.init(),
        statusBarPadding = true
    )
}

@Composable
fun createSeekBarPreference(key: Preferences.Key<Float>, default: Float, titleId: Int, headerId: Int, resId: Int, representation: (Float)->String, range: ClosedFloatingPointRange<Float>): Preference.PreferenceItem.SeekBarPreference {
    return Preference.PreferenceItem.SeekBarPreference(
        createPreferenceRequest(key, default),
        stringResource(titleId), stringResource(headerId),
        false,
        {Image(painterResource(resId), key.name)},
        true,
        steps = 1,
        valueRepresentation = representation,
        valueRange = range

    )
}

@Composable
fun createSwitchPreference(key: Preferences.Key<Boolean>, default: Boolean, titleId: Int, headerId: Int, resId: Int): Preference.PreferenceItem.SwitchPreference {
    return Preference.PreferenceItem.SwitchPreference(
        createPreferenceRequest(key, default),
        stringResource(titleId), stringResource(headerId),
        false,
        {Image(painterResource(resId), key.name)},
        true

    )
}

@Composable
fun createSwitchPreference(key: Preferences.Key<Boolean>, default: Boolean, titleId: String, headerId: String, resId: Int): Preference.PreferenceItem.SwitchPreference {
    return Preference.PreferenceItem.SwitchPreference(
        createPreferenceRequest(key, default),
        titleId, headerId,
        false,
        {Image(painterResource(resId), key.name)},
        true

    )
}

fun <T> createPreferenceRequest(key: Preferences.Key<T>, default: T): PreferenceRequest<T> {
    return PreferenceRequest(key, default)
}