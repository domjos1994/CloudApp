package de.domjos.cloudapp.activities

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.Settings
import de.domjos.cloudapp.data.repository.AuthenticationRepository
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.capabilities.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val settings: Settings
) : ViewModel() {

    fun getCapabilities(onResult: (Data?) -> Unit, authentication: Authentication?) {
        viewModelScope.launch(Dispatchers.IO) {
            onResult(authenticationRepository.getCapabilities(authentication))
        }
    }

    fun updateWidget(widget: GlanceAppWidget, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            glanceIds.forEach { glanceId ->
                widget.update(context, glanceId)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return authenticationRepository.hasAuthentications()
    }

    fun saveFirstStart() {
        viewModelScope.launch(Dispatchers.IO) {
            settings.setSetting(Settings.firstStartKey, false)
            settings.save()
        }
    }

    fun getFirstStart(): Boolean {
        val state = mutableStateOf(false)
        viewModelScope.launch {
            state.value = settings.getSetting(Settings.firstStartKey, true)
        }
        return state.value
    }

    fun getContactWorkerPeriod(): Float {
        val state = mutableFloatStateOf(0.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.cardavRegularityKey, 0.0f)
        }
        return state.floatValue
    }


    fun getCalendarWorkerPeriod(): Float {
        val state = mutableFloatStateOf(0.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.caldavRegularityKey, 0.0f)
        }
        return state.floatValue
    }
}