/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.model.Log
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import java.util.Date

import android.provider.CalendarContract.Calendars
import android.provider.CalendarContract.Events
import de.domjos.cloudapp2.caldav.CalendarCalDav
import de.domjos.cloudapp2.database.model.Authentication
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Suppress("JoinDeclarationAndAssignment")
class PhoneCalendarHelper(private val account: Account?, private val contentResolver: ContentResolver, private val db: DB) {
    private var authId: Long
    private var authentication: Authentication
    private var calendars: Map<Long, String>? = null


    init {
        this.authentication = this.db.authenticationDao().getSelectedItem()!!
        this.authId = this.authentication.id
    }

    fun sync() {
        this.insertLogMessage("Start syncing calendars!")
        this.insertLogMessage("Create Calendars")
        this.createCalendars()

        this.insertLogMessage("Start syncing events!")
        this.db.calendarEventDao().getAll(this.authId).forEach { event ->
            this.getPhoneEvent(event)
            this.insertOrUpdatePhoneEvent(event)
        }
    }

    private fun getPhoneEvent(appEvent: CalendarEvent): CalendarEvent? {
        if(appEvent.eventId.isEmpty()) {
            return null
        } else {
            val projection = arrayOf(Events.TITLE, Events.CALENDAR_ID, Events.DTSTART, Events.DTEND)
            val where = "${Events._ID}=?"
            val args = arrayOf(appEvent.eventId)

            val cursor = this.contentResolver.query(asSyncAdapter(Events.CONTENT_URI), projection, where, args, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    val title = this.getValue(c, Events.TITLE, "") ?: ""
                    val cId = this.getValue(c, Events.CALENDAR_ID, 0L) ?: 0L
                    val start = this.getValue(c, Events.DTSTART, 0L) ?: 0L
                    val end = this.getValue(c, Events.DTEND, 0L) ?: 0L

                }
            }
        }
        return null
    }

    private fun insertOrUpdatePhoneEvent(calendarEvent: CalendarEvent) {

        try {
            // get calendar
            var id = -1L
            this.calendars?.forEach { (t, u) ->
                if(calendarEvent.calendar == u) {
                    id = t
                }
            }

            if(id != -1L) {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

                val contentValues = ContentValues()
                contentValues.put(Events.CALENDAR_ID, id)
                contentValues.put(Events.TITLE, calendarEvent.title)
                val start = sdf.parse(calendarEvent.string_from)
                if(start != null) {
                    contentValues.put(Events.DTSTART, start.time)
                }
                val end = sdf.parse(calendarEvent.string_to)
                if(end != null) {
                    contentValues.put(Events.DTEND, end.time)
                }

                if(calendarEvent.description.isNotEmpty()) {
                    contentValues.put(Events.DESCRIPTION, calendarEvent.description)
                }
                if(calendarEvent.location.isNotEmpty()) {
                    contentValues.put(Events.EVENT_LOCATION, calendarEvent.location)
                }
                if(calendarEvent.confirmation.isNotEmpty()) {
                    when(calendarEvent.confirmation.lowercase()) {
                        "confirmed" -> contentValues.put(Events.STATUS, Events.STATUS_CONFIRMED)
                        "cancelled" -> contentValues.put(Events.STATUS, Events.STATUS_CANCELED)
                        "tentative" -> contentValues.put(Events.STATUS, Events.STATUS_TENTATIVE)
                        else -> contentValues.put(Events.STATUS, Events.STATUS_CONFIRMED)
                    }
                }

                if(calendarEvent.recurrence.isNotEmpty()) {
                    val spl = calendarEvent.recurrence.split(",")
                    var type = spl[0]
                    val interval = spl[1]
                    val repeats = spl[2]
                    val untilDate = spl[3]

                    var recurrence = ""
                    try {
                        val items = type.substring(type.indexOf("(") + 1, type.indexOf(")"))
                        type = type.substring(0, type.indexOf("("))
                        when(type.trim().lowercase()) {
                            "daily" -> recurrence = "FREQ=DAILY"
                            "weekly" -> {
                                recurrence = "FREQ=WEEKLY"
                                if(items.trim().isNotEmpty()) {
                                    recurrence = "$recurrence;BYDAY=$items"
                                } else {
                                    recurrence = "$recurrence;"
                                }
                            }
                            "monthly" -> {
                                recurrence = "FREQ=MONTHLY"
                                if(items.trim().isNotEmpty()) {
                                    recurrence = "$recurrence;BYMONTHDAY=$items"
                                } else {
                                    recurrence = "$recurrence;"
                                }
                            }
                            "yearly" -> {
                                recurrence = "FREQ=YEARLY"
                                if(items.trim().isNotEmpty()) {
                                    recurrence = "$recurrence;BYMONTH=$items"
                                } else {
                                    recurrence = "$recurrence;"
                                }
                            }
                            else -> recurrence = "FREQ=DAILY"
                        }
                    } catch (_: Exception) {}
                    try {
                        val intVal = interval.trim().toInt()
                        if(intVal != -1) {
                            recurrence = "${recurrence}INTERVAL=${intVal};"
                        }
                    } catch (_: Exception) {}
                    try {
                        val repVal = repeats.trim().toInt()
                        if(repVal != -1) {
                            recurrence = "${recurrence}COUNT=${repVal};"
                        }
                    } catch (_: Exception) {}
                    try {
                        val tsVal = untilDate.trim().toInt()
                        if(tsVal != 0) {
                            recurrence = "${recurrence}UNTIL=${tsVal};"
                        }
                    } catch (_: Exception) {}

                    if(recurrence.trim().isNotEmpty()) {
                        contentValues.put(Events.RRULE, recurrence)
                    }
                }

                if(calendarEvent.eventId.isEmpty()) {
                    val uri = this.contentResolver.insert(asSyncAdapter(Events.CONTENT_URI), contentValues)
                    val eventId = ContentUris.parseId(uri!!)
                    calendarEvent.eventId = "$eventId"
                    calendarEvent.lastUpdatedEventPhone = Date().time
                    this.db.calendarEventDao().updateCalendarEvent(calendarEvent)
                } else {
                    val where = "${Events._ID}=?"
                    val args = arrayOf(calendarEvent.eventId)
                    this.contentResolver.update(asSyncAdapter(Events.CONTENT_URI), contentValues, where, args)
                    calendarEvent.lastUpdatedEventPhone = Date().time
                    this.db.calendarEventDao().updateCalendarEvent(calendarEvent)
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem get Calendar!", appEvent = calendarEvent)
        }
    }

    private fun createCalendars() {
        val items = this.db.calendarEventDao().getCalendars(this.authId)
        val caldav = CalendarCalDav(this.authentication)
        val cals = caldav.getCalendars()

        items.forEach { item ->
            val cal = cals.find { it.name == item }
            if(cal != null) {
                this.getOrCreateCalendar(cal.name, cal.label)
            }
        }
    }

    private fun getPhoneCalendars(): Map<Long, String> {
        val calendars = mutableMapOf<Long, String>()

        try {
            val calendarUri = this.asSyncAdapter(Calendars.CONTENT_URI)
            val projection = arrayOf(Calendars._ID, Calendars.NAME)
            val whereClause = "${Calendars.DELETED}=?"
            val whereArgs = arrayOf("0")
            val order = Calendars.NAME

            val cursor = this.contentResolver.query(calendarUri, projection, whereClause, whereArgs, order)
            cursor?.use { c ->
                while(c.moveToNext()) {
                    val id = this.getValue(c, Calendars._ID, 0L) ?: 0L
                    val name = this.getValue(c, Calendars.NAME, "") ?: ""

                    if(!calendars.containsKey(id)) {
                        calendars[id] = name
                    }
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem fetching Calendars!")
        }
        return calendars
    }

    private fun getOrCreateCalendar(name: String, label: String): Long {
        var id = -1L
        try {
            val calendarUri = this.asSyncAdapter(Calendars.CONTENT_URI)
            if(this.calendars == null) {
                this.calendars = this.getPhoneCalendars()
            }
            this.calendars?.forEach { (key, value) ->
                if(value == name) id = key
            }

            val values = ContentValues()
            values.put(Calendars.NAME, name)
            values.put(Calendars.CALENDAR_DISPLAY_NAME, label)
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER)
            values.put(Calendars.ACCOUNT_TYPE, this.account?.type)
            values.put(Calendars.ACCOUNT_NAME, this.account?.name)
            values.put(Calendars.SYNC_EVENTS, 1)
            values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            values.put(Calendars.VISIBLE, 1)
            if(id == -1L) {
                val resultUri = this.contentResolver.insert(calendarUri, values)
                if(resultUri != null) {
                    id = ContentUris.parseId(resultUri)
                }
            } else {
                val where = "${Calendars._ID}=?"
                val clause = arrayOf("$id")
                this.contentResolver.update(calendarUri, values, where, clause)
            }
            this.calendars = this.getPhoneCalendars()
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem fetching Calendars!")
        }
        return id
    }


    private fun insertLogException(ex: Exception, msg: String = "", phoneEvent: CalendarEvent? = null, appEvent: CalendarEvent? = null) {
        try {
            var message = ""
            if(msg.isNotEmpty()) {
                message = "${msg}:\n"
            }
            message += "${ex.message}:\n${ex.stackTraceToString()}"

            val log = Log(
                date = Date(),
                itemType = "calendars",
                messageType = "error",
                message = message,
                object1 = phoneEvent?.toString() ?: "",
                object2 = appEvent?.toString() ?: ""
            )
            this.db.logDao().insertItem(log)
        } catch (_: Exception) {}
    }

    private fun insertLogMessage(msg: String, phoneEvent: CalendarEvent? = null, appEvent: CalendarEvent? = null) {
        try {
            val log = Log(
                date = Date(),
                itemType = "calendars",
                messageType = "info",
                message = msg,
                object1 = phoneEvent?.toString() ?: "",
                object2 = appEvent?.toString() ?: ""
            )
            this.db.logDao().insertItem(log)
        } catch (_: Exception) {}
    }

    @Throws(java.lang.Exception::class)
    private fun asSyncAdapter(uri: Uri): Uri {
        return if(this.account != null) {
            uri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, this.account.name)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, this.account.type).build()
        } else {
            uri
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(cursor: Cursor, column: String, default: T): T? {
        val index = cursor.getColumnIndex(column)
        return if(index >= 0) {
            when(default) {
                is String -> cursor.getString(index) as T?
                is Int -> cursor.getInt(index) as T?
                is Long -> cursor.getLong(index) as T?
                is Float -> cursor.getFloat(index) as T?
                is Double -> cursor.getDouble(index) as T?
                is ByteArray -> cursor.getBlob(index) as T?
                else -> null
            }
        } else {
            default
        }
    }
}