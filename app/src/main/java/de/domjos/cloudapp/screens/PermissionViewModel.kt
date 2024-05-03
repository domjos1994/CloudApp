package de.domjos.cloudapp.screens

import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.Settings
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {

    fun getContactRegularitySetting(): Float {
        val state = mutableFloatStateOf(1.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.contactRegularityKey, 1.0f)
        }
        return state.floatValue
    }

    fun getCalendarRegularitySetting(): Float {
        val state = mutableFloatStateOf(1.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.calendarRegularityKey, 1.0f)
        }
        return state.floatValue
    }
}