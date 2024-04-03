package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.caldav.Calendar
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.dao.CalendarEventDAO
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import java.util.LinkedList
import javax.inject.Inject

interface CalendarRepository {
    fun loadData(startTime: Long, endTime:Long): List<CalendarEvent>
    fun countData(calendar: java.util.Calendar): LinkedList<Int>
    fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String)
    fun insert(calendarEvent: CalendarEvent)
    fun update(calendarEvent: CalendarEvent)
    fun delete(calendarEvent: CalendarEvent)
}

class DefaultCalendarRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val calendarEventDAO: CalendarEventDAO
) : CalendarRepository {
    private val calendar = Calendar(authenticationDAO.getSelectedItem()!!)

    override fun loadData(startTime: Long, endTime: Long): List<CalendarEvent> {
        return calendarEventDAO.getItemsByTime(startTime, endTime, authenticationDAO.getSelectedItem()!!.id)
    }

    override fun countData(calendar: java.util.Calendar): LinkedList<Int> {
        val start = calendar.clone() as java.util.Calendar
        val end = calendar.clone() as java.util.Calendar
        start.set(java.util.Calendar.HOUR_OF_DAY, 0)
        start.set(java.util.Calendar.MINUTE, 0)
        start.set(java.util.Calendar.SECOND, 0)
        end.set(java.util.Calendar.HOUR_OF_DAY, 23)
        end.set(java.util.Calendar.MINUTE, 59)
        end.set(java.util.Calendar.SECOND, 59)

        val lst = LinkedList<Int>()
        for(day in 1..end.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)) {
            start.set(java.util.Calendar.DAY_OF_MONTH, day)
            end.set(java.util.Calendar.DAY_OF_MONTH, day)

            if(calendarEventDAO.count(start.time.time, end.time.time, authenticationDAO.getSelectedItem()!!.id)!=0L) {
                lst.add(day)
            }
        }

        return lst
    }

    override fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String) {
        this.calendarEventDAO.clearAll(authenticationDAO.getSelectedItem()!!.id)
        this.calendar.reloadCalendarEvents(updateProgress, progressLabel)
        val data = this.calendar.calendars

        data.keys.forEach { key ->
            var progress = 0L
            val max = data[key]?.size!! / 100.0f
            data[key]?.forEach { event ->
                event.authId = authenticationDAO.getSelectedItem()!!.id
                calendarEventDAO.insertCalendarEvent(event)
                progress += 1L
                updateProgress(progress*max/100.0f, String.format(saveLabel, key))
            }
        }
    }

    override fun insert(calendarEvent: CalendarEvent) {
        calendarEvent.authId = authenticationDAO.getSelectedItem()!!.id
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