package de.domjos.cloudapp.appbasics.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.helper.Settings
import de.schnettler.datastore.compose.material3.PreferenceScreen
import de.schnettler.datastore.compose.material3.model.Preference
import de.schnettler.datastore.manager.PreferenceRequest


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val timeSpanRequest = PreferenceRequest(
        key = Settings.timeSpanKey,
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

    PreferenceScreen(
        items = listOf(timeSpanPreference),
        dataStore = viewModel.init(LocalContext.current),
        statusBarPadding = true
    )
}