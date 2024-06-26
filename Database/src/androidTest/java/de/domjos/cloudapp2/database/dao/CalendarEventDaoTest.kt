/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.BaseTest
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class CalendarEventDaoTest : BaseTest() {
    private lateinit var calendarEventDAO: CalendarEventDAO
    private lateinit var event1: CalendarEvent
    private lateinit var event2: CalendarEvent
    private val cal1 = "Calendar 1"
    private val cal2 = "Calendar 2"
    private val cal = Calendar.getInstance()
    private val title = "Test"

    @Before
    fun before() {
        super.init()
        calendarEventDAO = super.db.calendarEventDao()

        event1 = CalendarEvent(
            id = 0L,
            from = addMonth(1, cal).time.time,
            to = addMonth(2, cal).time.time,
            title = "$title 1",
            location = "",
            description = "",
            confirmation = "",
            categories = "",
            color = "",
            calendar = cal1,
            eventId = "",
            lastUpdatedEventPhone = 0L,
            lastUpdatedEventServer = 0L,
            authId = 0L
        )
        event2 = CalendarEvent(
            id = 0L,
            from = addMonth(2, cal).time.time,
            to = addMonth(3, cal).time.time,
            title = "$title 1",
            location = "",
            description = "",
            confirmation = "",
            categories = "",
            color = "",
            calendar = cal2,
            eventId = "",
            lastUpdatedEventPhone = 0L,
            lastUpdatedEventServer = 0L,
            authId = 0L
        )
    }

    @Test
    fun testCreatingDeletingCalendarEvents() {
        // check if events are empty
        var items = calendarEventDAO.getAllWithoutFlow(0L)
        assertEquals(0, items.count())

        // insert event1
        calendarEventDAO.insertCalendarEvent(event1)
        event1 = calendarEventDAO.getAll(0L, event1.uid)!!

        // check if event exists
        items = calendarEventDAO.getAllWithoutFlow(0L)
        assertNotEquals(0, items.count())

        // check calendars
        val calendars = calendarEventDAO.getCalendars(0L)
        assertEquals(1, calendars.size)
        assertEquals(cal1, calendars[0])

        // delete calendar
        calendarEventDAO.deleteCalendarEvent(event1)

        // check if events are empty
        items = calendarEventDAO.getAllWithoutFlow(0L)
        assertEquals(0, items.count())
    }

    @Test
    fun testUpdatingDeletingCalendarEvents() {
        // check if events are empty
        var items = calendarEventDAO.getAllWithoutFlow(0L)
        assertEquals(0, items.count())

        // insert event1
        calendarEventDAO.insertCalendarEvent(event2)
        event2 = calendarEventDAO.getAll(0L, event2.uid)!!

        // check if event exists
        items = calendarEventDAO.getAllWithoutFlow(0L)
        assertNotEquals(0, items.count())

        // check calendars
        val calendars = calendarEventDAO.getCalendars(0L)
        assertEquals(1, calendars.size)
        assertEquals(cal2, calendars[0])

        // check if number is 1
        var count = calendarEventDAO.count(
            addMonth(2, cal).time.time,
            addMonth(3, cal).time.time, 0L)
        assertEquals(1, count)

        event2.calendar = cal1
        calendarEventDAO.updateCalendarEvent(event2)

        // check if number is 1
        count = calendarEventDAO.count(cal1,
            addMonth(2, cal).time.time,
            addMonth(3, cal).time.time, 0L)
        assertEquals(1, count)

        // delete calendar
        calendarEventDAO.deleteCalendarEvent(event2)

        // check if events are empty
        items = calendarEventDAO.getAllWithoutFlow(0L)
        assertEquals(0, items.count())
    }

    private fun addMonth(number: Int, cal: Calendar): Calendar {
        val tmp: Calendar = cal.clone() as Calendar
        tmp.add(Calendar.MONTH, number)
        return tmp
    }
}