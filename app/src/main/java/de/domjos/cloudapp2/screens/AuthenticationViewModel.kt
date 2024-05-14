package de.domjos.cloudapp2.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.AuthenticationRepository
import de.domjos.cloudapp2.database.model.Authentication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import de.domjos.cloudapp2.appbasics.R
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    var uiState: StateFlow<AuthenticationUiState> = authenticationRepository
        .authentications.map<List<Authentication>, AuthenticationUiState> {AuthenticationUiState.Success(data = it)}
        .catch { emit(AuthenticationUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthenticationUiState.Loading)
    var message = MutableLiveData<String?>()
    var resId = MutableLiveData<Int?>()
    fun checkAuthentications(authentication: Authentication) {
        viewModelScope.launch {
            try {
                authenticationRepository.check(authentication)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun insertAuthentication(authentication: Authentication, msg: String) {
        viewModelScope.launch {
            try {
                if(!authenticationRepository.hasAuthentications()) {
                    authentication.selected = true
                }
                val result = authenticationRepository.insert(authentication, msg)
                if(result != "") {
                    message.postValue(result)
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun updateAuthentication(authentication: Authentication, msg: String) {
        viewModelScope.launch {
            try {
                val result = authenticationRepository.update(authentication, msg)
                if(result != "") {
                    message.postValue(result)
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteAuthentication(authentication: Authentication) {
        viewModelScope.launch {
            try {
                val result = authenticationRepository.delete(authentication)
                if(result != "") {
                    message.postValue(result)
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun checkConnection(authentication: Authentication, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(authentication.url.trim().startsWith("http")) {
                    val user = authenticationRepository.checkConnection(authentication)

                    if(user != null) {
                        viewModelScope.launch(Dispatchers.Main) {
                            resId.postValue(R.string.login_check_success)
                            onSuccess(true)
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            resId.postValue(R.string.login_check_user)
                            onSuccess(false)
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        resId.postValue(R.string.login_check_url)
                        onSuccess(false)
                    }
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }
}

sealed interface AuthenticationUiState {
    data object Loading : AuthenticationUiState
    data class Error(val throwable: Throwable) : AuthenticationUiState
    data class Success(val data: List<Authentication>) : AuthenticationUiState
}