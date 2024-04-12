package de.domjos.cloudapp.features.notifications.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.NotificationsRepository
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository
): ViewModel() {
    private val _notifications = MutableStateFlow(listOf<Notification>())
    val notifications: StateFlow<List<Notification>> get() = _notifications

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            notificationsRepository.reload().collect {
                _notifications.value = it
            }
        }
    }

    fun getFullIconLink(notification: Notification): String {
        return notificationsRepository.getFullLink(notification)
    }

    fun hasAuthentications(): Boolean {
        return notificationsRepository.hasAuthentications()
    }
}