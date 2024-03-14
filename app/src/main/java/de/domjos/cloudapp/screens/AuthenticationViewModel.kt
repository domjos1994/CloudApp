package de.domjos.cloudapp.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.AuthenticationRepository
import de.domjos.cloudapp.database.model.Authentication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    var uiState: StateFlow<AuthenticationUiState> = authenticationRepository
        .authentications.map<List<Authentication>, AuthenticationUiState> {AuthenticationUiState.Success(data = it)}
        .catch { emit(AuthenticationUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthenticationUiState.Loading)
    fun getAuthentications(onSuccess: (List<Authentication>) -> Unit) {
        viewModelScope.launch {
            authenticationRepository.authentications.collect {
                onSuccess(it)
            }
        }
    }

    fun insertAuthentication(authentication: Authentication, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.insert(authentication))
        }
    }

    fun updateAuthentication(authentication: Authentication, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.update(authentication))
        }
    }

    fun deleteAuthentication(authentication: Authentication, onSuccess: (msg: String) -> Unit) {
        viewModelScope.launch {
            onSuccess(authenticationRepository.delete(authentication))
        }
    }
}

sealed interface AuthenticationUiState {
    object Loading : AuthenticationUiState
    data class Error(val throwable: Throwable) : AuthenticationUiState
    data class Success(val data: List<Authentication>) : AuthenticationUiState
}