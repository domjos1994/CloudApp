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
    val timeSpanPreference = Preference.PreferenceItem.SeekBarPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.timeSpanKey, 20.0f),
        stringResource(id = R.string.settings_timeSpan_title),
        stringResource(id = R.string.settings_timeSpan_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"${it.toInt()} min"},
        valueRange = 1.0f.rangeTo(200.0f)
    )

    val cloudThemePreference = Preference.PreferenceItem.SwitchPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.themeFromCloudKey, true),
        stringResource(id = R.string.settings_theme_cloud_title),
        stringResource(id = R.string.settings_theme_cloud_header),
        false,
        { Image(painterResource(R.drawable.baseline_design_services_24), stringResource(id = R.string.settings_theme_cloud_title))},
        true
    )
    val cloudThemeMobilePreference = Preference.PreferenceItem.SwitchPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.themeFromCloudMobileKey, true),
        stringResource(id = R.string.settings_theme_cloud_mobile_title),
        stringResource(id = R.string.settings_theme_cloud_mobile_header),
        false,
        { Image(painterResource(R.drawable.baseline_signal_wifi_connected_no_internet_4_24), stringResource(id = R.string.settings_theme_cloud_mobile_title))},
        true
    )


    val contactRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.contactRegularityKey, 1.0f),
        stringResource(id = R.string.settings_contact_regularity_title),
        stringResource(id = R.string.settings_contact_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"${it.toInt()} min"},
        valueRange = 1.0f.rangeTo(60.0f)
    )
    val cardavRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.cardavRegularityKey, 0.0f),
        stringResource(id = R.string.settings_cardav_regularity_title),
        stringResource(id = R.string.settings_cardav_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"${it.toInt()} min"},
        valueRange = 1.0f.rangeTo(10.0f)
    )

    val calendarRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        createPreferenceRequest(de.domjos.cloudapp.data.Settings.calendarRegularityKey, 1.0f),
        stringResource(id = R.string.settings_calendar_regularity_title),
        stringResource(id = R.string.settings_calendar_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"${it.toInt()} min"},
        valueRange = 1.0f.rangeTo(60.0f)
    )
    val caldavRegularityPreference = createSeekBarPreference(
        Settings.caldavRegularityKey, 0.0f, R.string.settings_caldav_regularity_title,
        R.string.settings_caldav_regularity_header, R.drawable.baseline_access_time_24,
        {"${it.toInt()} min"}, 0.0f.rangeTo(10.0f)
    )

    val cloudGroup = Preference.PreferenceGroup(stringResource(id = R.string.settings_cloud_title), true, listOf(cloudThemePreference, cloudThemeMobilePreference))
    val contactGroup = Preference.PreferenceGroup(stringResource(R.string.contacts), true, listOf(contactRegularityPreference, cardavRegularityPreference))
    val calendarGroup = Preference.PreferenceGroup(stringResource(R.string.calendars), true, listOf(calendarRegularityPreference, caldavRegularityPreference))

    PreferenceScreen(
        items = listOf(timeSpanPreference, cloudGroup, contactGroup, calendarGroup),
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

fun <T> createPreferenceRequest(key: Preferences.Key<T>, default: T): PreferenceRequest<T> {
    return PreferenceRequest(key, default)
}