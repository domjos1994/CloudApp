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
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CalDavTest {
    private lateinit var calDav: CalDav

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
        this.calDav = CalDav(authentication)
    }

    @Test
    fun testGettingCalendars() {
        runBlocking {
            val lst = calDav.getCalendars()
            assertNotEquals(0, lst.size)
        }
    }

    @Test
    fun testInsertingDeletingCalendars() {
        runBlocking {
            val lst = calDav.getCalendars()
            assertNotEquals(0, lst.size)

            val model = CalendarModel("test", "Test")
            calDav.insertCalendar(model)
            val tmp = calDav.getCalendars()
            assertNotEquals(lst.size, tmp.size)

            val item = tmp.find { it.name == "test" }
            assertNotNull(item)
            calDav.deleteCalendar(item!!)
        }
    }

    @Test
    fun testUpdatingCalendars() {
        runBlocking {
            val lst = calDav.getCalendars()
            assertNotEquals(0, lst.size)

            val calendar = lst[0]
            val old = calendar.label
            val new = "$old 1"
            calendar.label = new
            calDav.updateCalendar(calendar)

            val lst2 = calDav.getCalendars()
            assertNotEquals(0, lst2.size)
        }
    }

    @Test
    fun testGettingEvents() {
        runBlocking {
            val allEvents = calDav.reloadCalendarEvents({_,_->}, "")
            assertNotEquals(0, allEvents.size)

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
            val calendars = calDav.getCalendars()
            calDav.reloadCalendarEvents({_,_->}, "").forEach {(name, events) ->
                if(name == "persnlich") {
                    val calendar = calendars.find { it.name == name }
                    assertNotNull(calendar)

                    val start = Calendar.getInstance().time.time
                    val endCal = Calendar.getInstance()
                    endCal.add(Calendar.HOUR, 1)
                    val end = endCal.time.time

                    val newEvent = CalendarEvent(
                        id = 0L, from = start, to = end,
                        title = "This is a test!", calendar = name,
                        authId = authentication!!.id
                    )
                    calDav.newCalendarEvent(calendar!!, newEvent)
                    var newEvents = calDav.loadCalendarEvents(calendar, {_,_->}, "")
                    assertNotEquals(events.size, newEvents.size)

                    val item = newEvents.find { newEvent.title == it.title }
                    assertNotNull(item)

                    calDav.deleteCalendarEvent(item!!)

                    newEvents = calDav.loadCalendarEvents(calendar, {_,_->}, "")
                    assertEquals(events.size, newEvents.size)
                }
            }
        }
    }

    @Test
    fun testUpdatingEvents() {
        runBlocking {
            val calendars = calDav.getCalendars()
            calDav.reloadCalendarEvents({_,_->}, "").forEach {(name, events) ->
                if(name == "persnlich") {
                    val calendar = calendars.find { it.name == name }
                    assertNotNull(calendar)

                    val start = Calendar.getInstance().time.time
                    val endCal = Calendar.getInstance()
                    endCal.add(Calendar.HOUR, 1)
                    val end = endCal.time.time

                    val newEvent = CalendarEvent(
                        id = 0L, from = start, to = end,
                        title = "This is a test!", calendar = name,
                        authId = authentication!!.id
                    )
                    calDav.newCalendarEvent(calendar!!, newEvent)
                    var newEvents = calDav.loadCalendarEvents(calendar, {_,_->}, "")
                    assertNotEquals(events.size, newEvents.size)

                    val item = newEvents.find { newEvent.title == it.title }
                    assertNotNull(item)

                    item!!.title = "Updated"
                    calDav.updateCalendarEvent(item)
                    val foundItem = calDav.loadCalendarEvents(calendar, {_,_->}, "").find { it.title == "Updated" }
                    assertNotNull(foundItem)

                    calDav.deleteCalendarEvent(item)

                    newEvents = calDav.loadCalendarEvents(calendar, {_,_->}, "")
                    assertEquals(events.size, newEvents.size)
                }
            }
        }
    }
}