package de.domjos.cloudapp.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp.appbasics.R
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val timeSpanRequest = PreferenceRequest(
        key = de.domjos.cloudapp.data.Settings.timeSpanKey,
        defaultValue = 20.0f
    )
    val timeSpanPreference = Preference.PreferenceItem.SeekBarPreference(
        timeSpanRequest,
        stringResource(id = R.string.settings_timeSpan_title),
        stringResource(id = R.string.settings_timeSpan_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"$it"},
        valueRange = 1.0f.rangeTo(200.0f)
    )



    val contactRegularityRequest = PreferenceRequest(
        key = de.domjos.cloudapp.data.Settings.contactRegularityKey,
        defaultValue = 1.0f
    )
    val contactRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        contactRegularityRequest,
        stringResource(id = R.string.settings_contact_regularity_title),
        stringResource(id = R.string.settings_contact_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"$it min"},
        valueRange = 1.0f.rangeTo(60.0f)
    )
    val cardavRegularityRequest = PreferenceRequest(
        key = de.domjos.cloudapp.data.Settings.cardavRegularityKey,
        defaultValue = 0.0f
    )
    val cardavRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        cardavRegularityRequest,
        stringResource(id = R.string.settings_cardav_regularity_title),
        stringResource(id = R.string.settings_cardav_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"$it min"},
        valueRange = 1.0f.rangeTo(10.0f)
    )

    val calendarRegularityRequest = PreferenceRequest(
        key = de.domjos.cloudapp.data.Settings.calendarRegularityKey,
        defaultValue = 1.0f
    )
    val calendarRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        calendarRegularityRequest,
        stringResource(id = R.string.settings_calendar_regularity_title),
        stringResource(id = R.string.settings_calendar_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"$it min"},
        valueRange = 1.0f.rangeTo(60.0f)
    )
    val caldavRegularityRequest = PreferenceRequest(
        key = de.domjos.cloudapp.data.Settings.caldavRegularityKey,
        defaultValue = 0.0f
    )
    val caldavRegularityPreference = Preference.PreferenceItem.SeekBarPreference(
        caldavRegularityRequest,
        stringResource(id = R.string.settings_caldav_regularity_title),
        stringResource(id = R.string.settings_caldav_regularity_header),
        false,
        {Image(painterResource(id = R.drawable.baseline_access_time_24), "")},
        true,
        steps = 1, valueRepresentation = {"$it min"},
        valueRange = 0.0f.rangeTo(10.0f)
    )

    val contactGroup = Preference.PreferenceGroup(stringResource(R.string.contacts), true, listOf(contactRegularityPreference, cardavRegularityPreference))
    val calendarGroup = Preference.PreferenceGroup(stringResource(R.string.calendars), true, listOf(calendarRegularityPreference, caldavRegularityPreference))

    PreferenceScreen(
        items = listOf(timeSpanPreference, contactGroup, calendarGroup),
        dataStore = viewModel.init(),
        statusBarPadding = true
    )
}