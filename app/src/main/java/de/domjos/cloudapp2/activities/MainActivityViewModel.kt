package de.domjos.cloudapp2.activities

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.data.repository.AuthenticationRepository
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.capabilities.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val settings: Settings
) : ViewModel() {
    var message = MutableLiveData<String?>()

    fun getCapabilities(onResult: (Data?) -> Unit, authentication: Authentication?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                onResult(authenticationRepository.getCapabilities(authentication))
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun updateWidget(widget: GlanceAppWidget, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(widget.javaClass)
                glanceIds.forEach { glanceId ->
                    widget.update(context, glanceId)
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return authenticationRepository.hasAuthentications()
    }

    fun saveFirstStart() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                settings.setSetting(Settings.firstStartKey, false)
                settings.save()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun createWorkRequest(period: Float, flexPeriod: Float, worker: Class<out ListenableWorker>): WorkRequest? {
        return try {
            if(flexPeriod != 0.0f) {
                val workerBuilder = PeriodicWorkRequest.Builder(
                    worker,
                    period.toLong(), TimeUnit.MILLISECONDS,
                    flexPeriod.toLong(), TimeUnit.MILLISECONDS
                )
                workerBuilder.build()
            }
            null
        } catch (ex: Exception) {
            message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
            null
        }
    }

    fun getFirstStart(): Boolean {
        val state = mutableStateOf(false)
        viewModelScope.launch {
            state.value = settings.getSetting(Settings.firstStartKey, true)
        }
        return state.value
    }

    fun getContactWorkerPeriod(onGet: (Float) -> Unit) {
        viewModelScope.launch {
            onGet(settings.getSetting(Settings.cardavRegularityKey, 0.0f))
        }
    }


    fun getCalendarWorkerPeriod(onGet: (Float) -> Unit) {
        viewModelScope.launch {
            onGet(settings.getSetting(Settings.caldavRegularityKey, 0.0f))
        }
    }

    fun getCloudTheme(onGet: (Boolean) -> Unit) {
        viewModelScope.launch {
            onGet(settings.getSetting(Settings.themeFromCloudKey, true))
        }
    }

    fun getCloudThemeMobile(onGet: (Boolean) -> Unit) {
        viewModelScope.launch {
            onGet(settings.getSetting(Settings.themeFromCloudMobileKey, true))
        }
    }
}