package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import de.domjos.cloudapp.webrtc.requests.NotificationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

interface NotificationsRepository {
    val notifications: Flow<List<Notification>>

    fun reload(): Flow<List<Notification>>
    fun getFullLink(notification: Notification): String
}

class DefaultNotificationsRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : NotificationsRepository {
    override var notifications: Flow<List<Notification>> = reload()

    override fun reload(): Flow<List<Notification>> {
        return NotificationRequest(authenticationDAO.getSelectedItem()).getNotifications()
    }


    override fun getFullLink(notification: Notification): String {
        return if(notification.icon == "") {
            ""
        } else {
            if(notification.icon.lowercase().startsWith("http")) {
                return notification.icon
            } else {
                return "${authenticationDAO.getSelectedItem()?.url}/${notification.icon}"
            }
        }
    }
}