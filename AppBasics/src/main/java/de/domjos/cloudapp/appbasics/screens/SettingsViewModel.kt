package de.domjos.cloudapp.appbasics.screens

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.appbasics.helper.Settings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
) : ViewModel() {
    private var settings: Settings? = null

    fun init(context: Context): DataStore<Preferences> {
        settings = Settings(context)
        return settings!!.getStore()
    }

}