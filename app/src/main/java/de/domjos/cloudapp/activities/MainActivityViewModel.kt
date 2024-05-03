package de.domjos.cloudapp.activities

import androidx.compose.runtime.mutableStateOf
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
}