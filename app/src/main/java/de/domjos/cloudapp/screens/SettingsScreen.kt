package de.domjos.cloudapp.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp.appbasics.R
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest
import de.domjos.cloudapp.data.Settings


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val timeSpanPreference = createSeekBarPreference(
        Settings.timeSpanKey, 20.0f, R.string.settings_timeSpan_title, R.string.settings_timeSpan_header,
        R.drawable.baseline_access_time_24, {"${it.toInt()} min"}, 1.0f.rangeTo(200.0f)
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

    val cloudGroup = Preference.PreferenceGroup(stringResource(id = R.string.settings_cloud_title), true, listOf(cloudThemePreference, cloudThemeMobilePreference))
    val contactGroup = Preference.PreferenceGroup(stringResource(R.string.contacts), true, listOf(contactRegularityPreference, cardavRegularityPreference))
    val calendarGroup = Preference.PreferenceGroup(stringResource(R.string.calendars), true, listOf(calendarRegularityPreference, caldavRegularityPreference))
    val dataGroup = Preference.PreferenceGroup(stringResource(R.string.data), true, listOf(filesInInternal, pdfInInternal, imgInInternal, textInInternal, mdInInternal))

    PreferenceScreen(
        items = listOf(timeSpanPreference, cloudGroup, contactGroup, calendarGroup, dataGroup),
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

fun <T> createPreferenceRequest(key: Preferences.Key<T>, default: T): PreferenceRequest<T> {
    return PreferenceRequest(key, default)
}