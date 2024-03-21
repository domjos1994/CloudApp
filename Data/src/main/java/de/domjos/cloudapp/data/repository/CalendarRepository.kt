package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.caldav.Calendar
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.dao.CalendarEventDAO
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface CalendarRepository {
    val calendarEvents: Flow<List<CalendarEvent>>

    fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String)
    fun insert(calendarEvent: CalendarEvent)
    fun update(calendarEvent: CalendarEvent)
    fun delete(calendarEvent: CalendarEvent)
}

class DefaultCalendarRepository @Inject constructor(
    authenticationDAO: AuthenticationDAO,
    private val calendarEventDAO: CalendarEventDAO
) : CalendarRepository {
    override val calendarEvents: Flow<List<CalendarEvent>>
        get() = calendarEventDAO.getAll()
    private val calendar = Calendar(authenticationDAO.getSelectedItem()!!)

    override fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String) {
        this.calendarEventDAO.clearAll()
        this.calendar.reloadCalendarEvents(updateProgress, progressLabel)
        val data = this.calendar.calendars

        data.keys.forEach { key ->
            var progress = 0L
            val max = data[key]?.size!! / 100.0f
            data[key]?.forEach { event ->
                calendarEventDAO.insertCalendarEvent(event)
                progress += 1L
                updateProgress(progress*max/100.0f, String.format(saveLabel, key))
            }
        }
    }

    override fun insert(calendarEvent: CalendarEvent) {
        this.calendarEventDAO.insertCalendarEvent(calendarEvent)
        this.calendar.newCalendarEvent(calendarEvent)
    }

    override fun update(calendarEvent: CalendarEvent) {
        this.calendarEventDAO.updateCalendarEvent(calendarEvent)
        this.calendar.updateCalendarEvent(calendarEvent)
    }

    override fun delete(calendarEvent: CalendarEvent) {
        this.calendarEventDAO.deleteCalendarEvent(calendarEvent)
        this.calendar.deleteCalendarEvent(calendarEvent)
    }

}