package de.domjos.cloudapp.screens

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.Settings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {

    fun init(): DataStore<Preferences> {
        return settings.getStore()
    }
}