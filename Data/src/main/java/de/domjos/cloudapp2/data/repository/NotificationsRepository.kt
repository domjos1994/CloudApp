package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.rest.requests.NotificationRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface NotificationsRepository {
    val notifications: Flow<List<Notification>>

    fun reload(): Flow<List<Notification>>
    fun getFullLink(notification: Notification): String
    fun hasAuthentications(): Boolean
}

class DefaultNotificationsRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : NotificationsRepository {
    override var notifications: Flow<List<Notification>> = reload()

    override fun reload(): Flow<List<Notification>> {
        return NotificationRequest(authenticationDAO.getSelectedItem()).getNotifications()
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
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