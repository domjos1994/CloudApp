package de.domjos.cloudapp.features.notifications.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.NotificationsRepository
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository
): ViewModel() {
    var uiState: StateFlow<NotificationUiState> = notificationsRepository
        .notifications.map<List<Notification>, NotificationUiState> {NotificationUiState.Success(data = it)}
        .catch { emit(NotificationUiState.Error(it)) }
        .stateIn(viewModelScope.plus(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), NotificationUiState.Loading)

    fun getFullIconLink(notification: Notification): String {
        return notificationsRepository.getFullLink(notification)
    }
}

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Error(val throwable: Throwable) : NotificationUiState
    data class Success(val data: List<Notification>) : NotificationUiState
}