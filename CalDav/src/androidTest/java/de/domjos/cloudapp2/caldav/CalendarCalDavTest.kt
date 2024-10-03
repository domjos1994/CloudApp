package de.domjos.cloudapp2.caldav

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.database.model.Authentication
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.Properties
import de.domjos.cloudapp2.caldav.test.R
import de.domjos.cloudapp2.caldav.utils.Helper
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.UUID

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CalendarCalDavTest {
    private lateinit var calendarCalDav: CalendarCalDav

    @Rule
    @JvmField
    var runtimePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)

    companion object {
        private var context: Context? = null
        @JvmStatic
        private var authentication: Authentication? = null
        @JvmStatic
        private var props: Properties? = null

        /**
         * Read connection
         * and initialize authentication
         */
        @JvmStatic
        @BeforeClass
        fun before() {
            this.context = InstrumentationRegistry.getInstrumentation().targetContext
            val stream = this.context!!.resources.openRawResource(R.raw.example)
            props = Properties()
            props?.load(stream)
            stream.close()

            authentication = Authentication(
                1L, "N28", props!!["url"].toString(),
                props!!["user"].toString(), props!!["pwd"].toString(),
                true, "", null
            )
        }
    }

    @Before
    fun init() {
        this.calendarCalDav = CalendarCalDav(authentication)
    }

    @Test
    fun testGettingCalendars() {
        runBlocking {

            // list calendars
            val lst = calendarCalDav.getCalendars()
            assertNotEquals(0, lst.size)
        }
    }

    @Test
    fun testInsertingDeletingCalendars() {
        runBlocking {

            // list calendars
            val lst = calendarCalDav.getCalendars()
            assertNotEquals(0, lst.size)

            // creating new calendar
            val uuid = UUID.randomUUID().toString().lowercase()
            val model = CalendarModel(uuid, uuid)
            calendarCalDav.insertCalendar(model)

            // list calendars
            val tmp = calendarCalDav.getCalendars()
            assertNotEquals(lst.size, tmp.size)
            val item = tmp.find { it.name == uuid }
            assertNotNull(item)

            // delete calendar
            calendarCalDav.deleteCalendar(item!!)
        }
    }

    @Test
    fun testUpdatingCalendars() {
        runBlocking {

            // list calendars
            val lst = calendarCalDav.getCalendars()
            assertNotEquals(0, lst.size)

            // update calendar
            val calendar = lst[0]
            val old = calendar.label
            val new = "$old 1"
            calendar.label = new
            calendarCalDav.updateCalendar(calendar)

            // find calendar
            var lst2 = calendarCalDav.getCalendars()
            var item = lst2.find { it.label == new }
            assertNotNull(item)

            // update back
            item?.label = old
            calendarCalDav.updateCalendar(item!!)

            // find calendar
            lst2 = calendarCalDav.getCalendars()
            item = lst2.find { it.label == old }
            assertNotNull(item)
        }
    }

    @Test
    fun testGettingEvents() {
        runBlocking {

            // list all calendars with events
            val allEvents = calendarCalDav.reloadCalendarEvents({ _, _->}, "")
            assertNotEquals(0, allEvents.size)

            // check size
            var size = 0
            allEvents.forEach { (_, calendarEvents) ->
                size += calendarEvents.size
            }
            assertNotEquals(0, size)
        }
    }

    @Test
    fun testInsertingDeletingEvents() {
        runBlocking {

            // new uuid and new calendar
            val uuid = UUID.randomUUID().toString().lowercase()
            val cal = CalendarModel(uuid, uuid)
            cal.path = calendarCalDav.insertCalendar(cal)

            // list calendars
            val calendars = calendarCalDav.getCalendars()


            calendarCalDav.reloadCalendarEvents({ _, _->}, "").filter { it.key == uuid }.forEach { (name, events) ->

                // create new event with uuid
                val calName = UUID.randomUUID().toString().lowercase()

                // find calendar
                val calendar = calendars.find { it.name == name }
                assertNotNull(calendar)

                // create new event
                val start = Helper.getString(Calendar.getInstance().time)
                val endCal = Calendar.getInstance()
                endCal.add(Calendar.HOUR, 1)
                val end = Helper.getString(endCal.time)

                val newEvent = CalendarEvent(
                    id = 0L, string_from = start, string_to = end,
                    title = calName, calendar = name,
                    authId = authentication!!.id
                )
                calendarCalDav.newCalendarEvent(calendar!!, newEvent)

                // load events and compare
                var newEvents = calendarCalDav.loadCalendarEvents(calendar, { _, _->}, "")
                assertNotEquals(events.size, newEvents.size)

                // find event
                val item = newEvents.find { newEvent.title == it.title }
                assertNotNull(item)

                // delete event
                calendarCalDav.deleteCalendarEvent(item!!)

                // load events and compare
                newEvents = calendarCalDav.loadCalendarEvents(calendar, { _, _->}, "")
                assertEquals(events.size, newEvents.size)
            }

            // delete calendar
            calendarCalDav.deleteCalendar(cal)
        }
    }

    @Test
    fun testUpdatingEvents() {
        runBlocking {
            // new uuid and new calendar
            val uuid = UUID.randomUUID().toString().lowercase()
            val cal = CalendarModel(uuid, uuid)
            cal.path = calendarCalDav.insertCalendar(cal)

            // list calendars
            val calendars = calendarCalDav.getCalendars()

            calendarCalDav.reloadCalendarEvents({ _, _->}, "").filter { it.key == uuid }.forEach { (name, events) ->

                // create new events with uuid
                val oldCalName = UUID.randomUUID().toString().lowercase()
                val newCalName = UUID.randomUUID().toString().lowercase()

                // find calendar
                val calendar = calendars.find { it.name == name }
                assertNotNull(calendar)

                // create new event
                val start = Helper.getString(Calendar.getInstance().time)
                val endCal = Calendar.getInstance()
                endCal.add(Calendar.HOUR, 1)
                val end = Helper.getString(endCal.time)

                val newEvent = CalendarEvent(
                    id = 0L, string_from = start, string_to = end,
                    title = oldCalName, calendar = name,
                    authId = authentication!!.id
                )
                calendarCalDav.newCalendarEvent(calendar!!, newEvent)

                // load events and compare
                var newEvents = calendarCalDav.loadCalendarEvents(calendar, { _, _->}, "")
                assertNotEquals(events.size, newEvents.size)

                // find event
                val item = newEvents.find { newEvent.title == it.title }
                assertNotNull(item)

                // update event
                item!!.title = newCalName
                calendarCalDav.updateCalendarEvent(item)

                // find event
                val foundItem = calendarCalDav.loadCalendarEvents(calendar, { _, _->}, "").find { it.title == newCalName }
                assertNotNull(foundItem)

                // delete event
                calendarCalDav.deleteCalendarEvent(item)

                // load events and compare
                newEvents = calendarCalDav.loadCalendarEvents(calendar, { _, _->}, "")
                assertEquals(events.size, newEvents.size)
            }

            // delete calendar
            calendarCalDav.deleteCalendar(cal)
        }
    }
}