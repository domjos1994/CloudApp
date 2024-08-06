package de.domjos.cloudapp2.features.notifications.screens

import android.content.Context
import android.util.Log
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.data.repository.NotificationsRepository
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.features.notifications.screens.model.NotificationItem
import de.domjos.cloudapp2.rest.model.notifications.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import de.domjos.cloudapp2.appbasics.R
import java.text.SimpleDateFormat

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val authenticationDAO: AuthenticationDAO,
    private val calendarEventDAO: CalendarEventDAO,
    private val contactDAO: ContactDAO,
    private val settings: Settings
): ViewModel() {
    private val _notifications = MutableStateFlow(listOf<NotificationItem>())
    val notifications: StateFlow<List<NotificationItem>> get() = _notifications
    private val _allTypes = MutableStateFlow(true)
    val allTypes: StateFlow<Boolean> get() = _allTypes
    val message = MutableLiveData<String>()

    fun reload(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val notificationItems = mutableListOf<NotificationItem>()
            try {
                val server = settings.getSetting(Settings.notificationTypeServerKey, true)
                val app = settings.getSetting(Settings.notificationTypeAppKey, true)
                _allTypes.value = server && app

                if(server) {
                    notificationsRepository.reload().collect { items ->
                        val lst = mutableListOf<NotificationItem>()
                        items.forEach { item ->
                            lst.add(NotificationItem(NotificationItem.Type.Server, item))
                        }
                        notificationItems.addAll(lst)
                    }
                }
                if(app) {
                    val days = settings.getSetting(Settings.notificationTimeKey, 7.0f)
                    val calendar = Calendar.getInstance(Locale.getDefault())
                    val startTime = calendar.time.time
                    calendar.add(Calendar.DAY_OF_MONTH, days.toInt())
                    val endTime = calendar.time.time

                    var contacts = contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
                    contacts = contacts.filter { if(it.birthDay != null) it.birthDay?.time!! in startTime..endTime else false}
                    var events = calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
                    events = events.filter { it.from in startTime..endTime }

                    contacts.forEach {
                        val description = "${context.getString(R.string.contact_birthDate)}: ${it.givenName} ${it.familyName?:""}".trim()
                        notificationItems.add(NotificationItem(
                            type = NotificationItem.Type.App,
                            date = it.birthDay!!,
                            title = "${it.givenName} ${it.familyName?:""}".trim(),
                            description = description,
                            icon = {Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = description)}
                        ))
                    }

                    events.forEach {
                        val sdf = SimpleDateFormat(context.getString(R.string.sys_format), Locale.getDefault())
                        val start = sdf.format(Date(it.from))
                        val end = sdf.format(Date(it.to))
                        val description = "${it.title}: $start - $end".trim()
                        notificationItems.add(NotificationItem(
                            type = NotificationItem.Type.App,
                            date = Date(it.from),
                            title = it.title,
                            description = description,
                            icon = {Icon(imageVector = Icons.Filled.DateRange, contentDescription = description)}
                        ))
                    }
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
            } finally {
                _notifications.value = notificationItems
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