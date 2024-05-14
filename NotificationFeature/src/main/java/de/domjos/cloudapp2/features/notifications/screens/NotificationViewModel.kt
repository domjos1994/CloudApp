package de.domjos.cloudapp2.features.notifications.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.NotificationsRepository
import de.domjos.cloudapp2.rest.model.notifications.Notification
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
    val message = MutableLiveData<String>()

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                notificationsRepository.reload().collect {
                    _notifications.value = it
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
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