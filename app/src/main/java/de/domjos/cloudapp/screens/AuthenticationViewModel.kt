package de.domjos.cloudapp.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.AuthenticationRepository
import de.domjos.cloudapp.database.model.Authentication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import de.domjos.cloudapp.appbasics.R
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    var uiState: StateFlow<AuthenticationUiState> = authenticationRepository
        .authentications.map<List<Authentication>, AuthenticationUiState> {AuthenticationUiState.Success(data = it)}
        .catch { emit(AuthenticationUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthenticationUiState.Loading)
    fun checkAuthentications(authentication: Authentication) {
        viewModelScope.launch {
            authenticationRepository.check(authentication)
        }
    }

    fun insertAuthentication(authentication: Authentication, msg: String, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.insert(authentication, msg))
        }
    }

    fun updateAuthentication(authentication: Authentication, msg: String, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.update(authentication, msg))
        }
    }

    fun deleteAuthentication(authentication: Authentication, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.delete(authentication))
        }
    }

    fun checkConnection(authentication: Authentication, onSuccess: (Int, Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(authentication.url.trim().startsWith("http")) {
                    val user = authenticationRepository.checkConnection(authentication)

                    if(user != null) {
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess(R.string.login_check_success, true)
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.Main) {
                            onSuccess(R.string.login_check_user, false)
                        }
                    }
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        onSuccess(R.string.login_check_url, false)
                    }
                }
            } catch (ex: Exception) {
                viewModelScope.launch(Dispatchers.Main) {
                    onSuccess(R.string.login_check_not_success, false)
                }
            }
        }
    }
}

sealed interface AuthenticationUiState {
    data object Loading : AuthenticationUiState
    data class Error(val throwable: Throwable) : AuthenticationUiState
    data class Success(val data: List<Authentication>) : AuthenticationUiState
}