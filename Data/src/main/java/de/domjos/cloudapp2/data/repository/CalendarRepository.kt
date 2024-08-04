package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.caldav.CalDav
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import java.util.LinkedList
import javax.inject.Inject

interface CalendarRepository {
    fun loadData(calendar: String, startTime: Long, endTime:Long): List<CalendarEvent>
    fun getCalendars(): List<CalendarModel>
    fun countData(calendar: String, event: java.util.Calendar): LinkedList<Int>
    fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String)
    fun reloadCalendar(name: String, updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String)
    fun insert(calendarEvent: CalendarEvent)
    fun update(calendarEvent: CalendarEvent)
    fun delete(calendarEvent: CalendarEvent)
    fun hasAuthentications(): Boolean
}

class DefaultCalendarRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val calendarEventDAO: CalendarEventDAO
) : CalendarRepository {
    private val calDav = CalDav(authenticationDAO.getSelectedItem())

    override fun loadData(calendar: String, startTime: Long, endTime: Long): List<CalendarEvent> {
        return if(authenticationDAO.getSelectedItem()!=null) {
            if(calendar == "") {
                calendarEventDAO.getItemsByTime(startTime, endTime, authenticationDAO.getSelectedItem()!!.id)
            } else {
                calendarEventDAO.getItemsByTimeAndCalendar(calendar, startTime, endTime, authenticationDAO.getSelectedItem()!!.id)
            }
        } else {
            listOf()
        }
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override fun getCalendars(): List<CalendarModel> {
        val lst = mutableListOf<CalendarModel>()
        lst.add(CalendarModel("", "", ""))
        lst.addAll(calDav.getCalendars())
        return lst
    }

    override fun countData(calendar: String, event: java.util.Calendar): LinkedList<Int> {
        val start = event.clone() as java.util.Calendar
        val end = event.clone() as java.util.Calendar
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

            if(authenticationDAO.getSelectedItem() != null) {
                if(calendar == "") {
                    if(calendarEventDAO.count(start.time.time, end.time.time, authenticationDAO.getSelectedItem()!!.id)!=0L) {
                        lst.add(day)
                    }
                } else {
                    if(calendarEventDAO.count(calendar, start.time.time, end.time.time, authenticationDAO.getSelectedItem()!!.id)!=0L) {
                        lst.add(day)
                    }
                }
            }
        }

        return lst
    }

    override fun reload(updateProgress: (Float, String) -> Unit, progressLabel: String, saveLabel: String) {
        val data = this.calDav.reloadCalendarEvents(updateProgress, progressLabel)
        data.keys.forEach { key ->
            var progress = 0L
            val max = data[key]?.size!! / 100.0f
            data[key]?.forEach { event ->
                val uid = event.uid
                try {
                    if(this.authenticationDAO.getSelectedItem() != null) {
                        val id = this.authenticationDAO.getSelectedItem()!!.id
                        val tmp = this.calendarEventDAO.getAll(id, uid)
                        if(tmp != null) {
                            event.eventId = tmp.eventId
                            event.lastUpdatedEventPhone = tmp.lastUpdatedEventPhone
                        }
                        this.calendarEventDAO.clear(id, uid)
                    }
                } catch (_: Exception) {}

                event.authId = authenticationDAO.getSelectedItem()!!.id
                calendarEventDAO.insertCalendarEvent(event)
                progress += 1L
                updateProgress(progress*max/100.0f, String.format(saveLabel, key))
            }
        }
    }

    override fun reloadCalendar(
        name: String,
        updateProgress: (Float, String) -> Unit,
        progressLabel: String,
        saveLabel: String
    ) {
        val calendars = this.calDav.getCalendars()
        val calendar = calendars.find { it.name == name }
        var progress = 0L

        if(calendar != null) {
            val data = this.calDav.loadCalendarEvents(calendar, updateProgress, progressLabel)
            data.forEach { event ->
                val max = data.size / 100.0f

                val uid = event.uid
                try {
                    if(this.authenticationDAO.getSelectedItem() != null) {
                        val id = this.authenticationDAO.getSelectedItem()!!.id
                        val tmp = this.calendarEventDAO.getAll(id, uid)
                        if(tmp != null) {
                            event.eventId = tmp.eventId
                            event.lastUpdatedEventPhone = tmp.lastUpdatedEventPhone
                        }
                        this.calendarEventDAO.clear(id, uid)
                    }
                } catch (_: Exception) {}

                event.authId = authenticationDAO.getSelectedItem()!!.id
                calendarEventDAO.insertCalendarEvent(event)
                progress += 1L
                updateProgress(progress*max/100.0f, String.format(saveLabel, name))
            }
        }
    }

    override fun insert(calendarEvent: CalendarEvent) {
        val item = this.calDav.getCalendars().find { it.name == calendarEvent.calendar }

        if(item != null && validate(calendarEvent)) {
            calendarEvent.authId = authenticationDAO.getSelectedItem()!!.id
            this.calendarEventDAO.insertCalendarEvent(calendarEvent)
            this.calDav.newCalendarEvent(item, calendarEvent)
        }
    }

    override fun update(calendarEvent: CalendarEvent) {
        val item = this.calDav.getCalendars().find { it.name == calendarEvent.calendar }

        if(item != null && validate(calendarEvent)) {
            this.calendarEventDAO.updateCalendarEvent(calendarEvent)
            this.calDav.updateCalendarEvent(calendarEvent)
        }
    }

    override fun delete(calendarEvent: CalendarEvent) {
        this.calendarEventDAO.deleteCalendarEvent(calendarEvent)
        this.calDav.deleteCalendarEvent(calendarEvent)
    }

    private fun validate(calendarEvent: CalendarEvent): Boolean {
        val calendar = calendarEvent.calendar
        val authId = authenticationDAO.getSelectedItem()?.id!!
        val start = calendarEvent.from
        val end = calendarEvent.to
        return this.calendarEventDAO.validate(calendar, start, end, authId, calendarEvent.id) == 0
    }
}