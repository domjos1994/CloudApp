/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.ContentProviderOperation
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
import kotlin.Long

@Suppress("JoinDeclarationAndAssignment")
class PhoneCalendarHelper(private val account: Account?, private val contentResolver: ContentResolver, private val db: DB) {
    private var authId: Long
    private var authentication: Authentication?
    private var calendars: Map<Long, String>? = null
    private val operations = mutableListOf<ContentProviderOperation>()

    init {
        this.authentication = this.db.authenticationDao().getSelectedItem()
        this.authId = this.authentication?.id ?: 0L
    }

    fun sync() {
        this.operations.clear()
        this.insertLogMessage("Start syncing calendars!")
        this.insertLogMessage("Create Calendars")
        this.createCalendars()

        this.updateByStatus()
    }

    private fun updateByStatus() {
        try {
            val phoneEvents = this.getData()
            val appEvents = this.db.calendarEventDao().getAll(this.authId)

            // 0 insert phone item
            // 1 insert app item
            // 2 update phone item
            // 3 update app item
            // 4 delete phone item
            // 5 delete app item
            val states = mutableListOf<Array<Long>>()
            phoneEvents.forEach { item ->
                val find = appEvents.find { it.id==item[3] }
                if(find == null) {
                    if(item[3] > 0L) {
                        states.add(arrayOf(item[0], 4))
                    } else {
                        states.add(arrayOf(item[0], 1))
                    }
                } else {
                    if((find.lastUpdatedEventApp ?: 0L) > item[2]) {
                        states.add(arrayOf(find.id, 2))
                    } else if((find.lastUpdatedEventApp ?: 0L) < item[2]) {
                        states.add(arrayOf(item[0], 3))
                    } else {
                        if(item[1] == 1L) {
                            states.add(arrayOf(item[0], 3))
                        }
                    }
                }
            }
            appEvents.forEach {item ->
                val find = phoneEvents.find { it[3]==item.id }
                if(find == null) {
                    if(item.eventId.isNotEmpty()) {
                        states.add(arrayOf(item.id, 5))
                    } else {
                        states.add(arrayOf(item.id, 0))
                    }
                }
            }


            states.forEach {
                when(it[1]) {
                    0L -> this.insertPhoneItem(it[0])
                    1L -> this.insertAppItem(it[0])
                    2L -> this.updateAppItem(it[0])
                    3L -> this.updatePhoneItem(it[0])
                    4L -> this.deletePhoneItem(it[0])
                    5L -> this.deleteAppItem(it[0])
                }
            }
            this.contentResolver.notifyChange(Events.CONTENT_URI, null)
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem syncing data!")
        } finally {
            this.db.close()
        }
    }

    private fun getData() : MutableList<Array<Long>> {
        val items: MutableList<Array<Long>> = mutableListOf()
        try {
            val uri = this.asSyncAdapter(Events.CONTENT_URI)
            val projection = arrayOf(Events._ID, Events.DIRTY, Events.LAST_SYNCED, Events.SYNC_DATA1)
            val where = "${Events.ACCOUNT_NAME}=? AND ${Events.DELETED}=0"
            val args = arrayOf(account?.name)
            val cursor = this.contentResolver.query(uri, projection, where, args, null)
            cursor?.use { c ->
                while(c.moveToNext()) {
                    val eventId = this.getValue(c, Events._ID, 0L) ?: 0L
                    val dirty = this.getValue(c, Events.DIRTY, 0L) ?: 0L
                    val lastSynced = this.getValue(c, Events.LAST_SYNCED, 0L) ?: 0L
                    val appId = (this.getValue(c, Events.SYNC_DATA1, "0") ?: "0").toLong()
                    items.add(arrayOf(eventId, dirty, lastSynced, appId))
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem loading Base-Data!")
        }
        return items
    }

    private fun deletePhoneItem(id: Long) {
        try {
            val where = "${Events._ID}=? AND ${Events.DELETED}=0"
            val args = arrayOf("$id")
            val values = ContentValues()
            values.put(Events.DELETED, 1)
            this.contentResolver.update(asSyncAdapter(Events.CONTENT_URI), values, where, args)
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem deleting item from phone!")
        }
    }

    private fun deleteAppItem(id: Long) {
        try {
            this.db.calendarEventDao().deleteCalendarEvent(id)
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem deleting item from app!")
        }
    }

    private fun insertAppItem(id: Long) {
        try {
            val phoneItem = this.getPhoneItem(id)
            if(phoneItem != null) {
                val appItem = this.insertOrUpdateAppEvent(phoneItem)
                this.connectOrUpdatePhoneItem(appItem)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert phone item in app!")
        }
    }

    private fun insertPhoneItem(id: Long) {
        try {
            val appItem = this.getAppItem(id)
            if(appItem != null) {
                val phoneItem = this.insertOrUpdatePhoneEvent(appItem)
                this.connectOrUpdateAppItem(phoneItem)
            }
            android.util.Log.d("Sync Calendar", "Insert item $id")
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert app item in phone!")
        }
    }

    private fun updatePhoneItem(id: Long) {
        try {
            val phoneItem = this.getPhoneItem(id)
            if(phoneItem != null) {
                val appItem = this.insertOrUpdateAppEvent(phoneItem)
                this.connectOrUpdatePhoneItem(appItem)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem updating app item in phone!")
        }
    }

    private fun updateAppItem(id: Long) {
        try {
            val appItem = this.getAppItem(id)
            if(appItem != null) {
                val phoneItem = this.insertOrUpdatePhoneEvent(appItem)
                this.connectOrUpdateAppItem(phoneItem)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem updating phone item in app!")
        }
    }

    private fun getAppItem(id: Long): CalendarEvent? {
        var event: CalendarEvent? = null
        try {
            val events = this.db.calendarEventDao().getAll(this.authId)
            event = events.find { it.id == id }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem fetching app-event")
        }
        return event
    }

    private fun getPhoneItem(id: Long): CalendarEvent? {
        var event: CalendarEvent? = null
        try {
            val projection = arrayOf(
                Events.TITLE, Events.CALENDAR_ID, Events.DTSTART, Events.DTEND,
                Events.RRULE, Events.STATUS, Events.EVENT_LOCATION, Events.DESCRIPTION,
                Events.SYNC_DATA1, Events.LAST_SYNCED, Events.DIRTY, Events.EVENT_COLOR
            )
            val where = "${Events._ID}=?"
            val args = arrayOf("$id")

            val cursor = this.contentResolver.query(asSyncAdapter(Events.CONTENT_URI), projection, where, args, null)
            cursor?.use { c ->
                while(c.moveToNext()) {
                    val title = this.getValue(c, Events.TITLE, "") ?: ""
                    val calendar = calendars?.get(this.getValue(c, Events.CALENDAR_ID, 0L) ?: 0L) ?: ""
                    val start = this.getValue(c, Events.DTSTART, 0L) ?: 0L
                    val end = this.getValue(c, Events.DTEND, 0L) ?: 0L
                    val rrule = this.rrulePhoneToRRuleApp(this.getValue(c, Events.RRULE, "") ?: "")
                    val status = when(this.getValue(c, Events.STATUS, 0) ?: 0) {
                        Events.STATUS_CONFIRMED -> "confirmed"
                        Events.STATUS_CANCELED -> "cancelled"
                        Events.STATUS_TENTATIVE -> "tentative"
                        else -> "confirmed"
                    }
                    val color = this.getValue(c, Events.EVENT_COLOR, "") ?: ""
                    val location = this.getValue(c, Events.EVENT_LOCATION, "") ?: ""
                    val description = this.getValue(c, Events.DESCRIPTION, "") ?: ""
                    val appId = this.getValue(c, Events.SYNC_DATA1, "") ?: ""
                    val lastSynced = this.getValue(c, Events.LAST_SYNCED, 0L) ?: 0L

                    event = CalendarEvent(
                        id = id,
                        uid = "",
                        string_from = dateToString(Date(start)),
                        string_to = dateToString(Date(end)),
                        title = title,
                        location = location,
                        description = description,
                        confirmation = status,
                        categories = "",
                        color = color,
                        calendar = calendar,
                        eventId = appId,
                        lastUpdatedEventServer = -1L,
                        lastUpdatedEventPhone = lastSynced,
                        recurrence = rrule
                    )
                    event.lastUpdatedEventApp = lastSynced
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem fetching phone-event", event)
        }
        return event
    }

    private fun insertOrUpdateAppEvent(event: CalendarEvent): CalendarEvent {
        try {
            val appItem = event.eventId
            val phoneItem = "${event.id}"
            event.eventId = phoneItem
            event.authId = authId
            if(appItem.isEmpty()) {
                event.id = 0
                event.id = this.db.calendarEventDao().insertCalendarEvent(event)
            } else {
                event.id = appItem.toLong()
                this.db.calendarEventDao().updateCalendarEvent(event)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert or update app-event", event)
        }
        return event
    }

    private fun insertOrUpdatePhoneEvent(calendarEvent: CalendarEvent): CalendarEvent {

        try {
            // get calendar
            var id = -1L
            this.calendars?.forEach { (t, u) ->
                if(calendarEvent.calendar == u) {
                    id = t
                }
            }

            if(id != -1L) {
                val contentValues = ContentValues()
                contentValues.put(Events.CALENDAR_ID, id)
                contentValues.put(Events.TITLE, calendarEvent.title)
                val start = stringToDate(calendarEvent.string_from)
                contentValues.put(Events.DTSTART, start.time)
                val end = stringToDate(calendarEvent.string_to)
                contentValues.put(Events.DTEND, end.time)

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
                    val recurrence = this.rruleAppToRRulePhone(calendarEvent.recurrence)

                    if(recurrence.trim().isNotEmpty()) {
                        contentValues.put(Events.RRULE, recurrence)
                    }
                }
                contentValues.put(Events.SYNC_DATA1, calendarEvent.id)
                val ts = Date().time
                contentValues.put(Events.LAST_SYNCED, ts)
                calendarEvent.lastUpdatedEventPhone = ts
                calendarEvent.lastUpdatedEventApp = ts

                if(calendarEvent.eventId.isEmpty()) {
                    val uri = this.contentResolver.insert(asSyncAdapter(Events.CONTENT_URI), contentValues)
                    val eventId = ContentUris.parseId(uri!!)
                    calendarEvent.eventId = "$eventId"
                } else {
                    val where = "${Events._ID}=?"
                    val args = arrayOf(calendarEvent.eventId)
                    this.contentResolver.update(asSyncAdapter(Events.CONTENT_URI), contentValues, where, args)
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem updating or inserting phone-calendar!", appEvent = calendarEvent)
        }
        return calendarEvent
    }

    private fun connectOrUpdateAppItem(phoneItem: CalendarEvent) {
        try {
            val item = this.db.calendarEventDao().getAll(this.authId).find { it.id==phoneItem.id }
            if(item != null) {
                item.eventId = phoneItem.eventId
                item.lastUpdatedEventPhone = phoneItem.lastUpdatedEventPhone
                item.lastUpdatedEventApp = phoneItem.lastUpdatedEventPhone
                this.db.calendarEventDao().updateCalendarEvent(item)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem connecting or updating app-calendar!", phoneEvent = phoneItem)
        }
    }

    private fun connectOrUpdatePhoneItem(appItem: CalendarEvent) {
        try {
            val values = ContentValues()
            values.put(Events.LAST_SYNCED, appItem.lastUpdatedEventApp)
            values.put(Events.SYNC_DATA1, "${appItem.id}")
            values.put(Events.DIRTY, 0)

            val where = "${Events._ID}=?"
            val args = arrayOf(appItem.eventId)
            this.contentResolver.update(asSyncAdapter(Events.CONTENT_URI), values, where, args)
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem connecting or updating phone-calendar!", appEvent = appItem)
        }
    }

    private fun stringToDate(string: String): Date {
        var date = Date()
        try {
            val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            date = sdf.parse(string) ?: Date()
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                date = sdf.parse(string) ?: Date()
            } catch (_: Exception) {}
        }
        return date
    }

    private fun dateToString(date: Date): String {
        var string = ""
        try {
            val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            string = sdf.format(date) ?: ""
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                string = sdf.format(date) ?: ""
            } catch (_: Exception) {}
        }
        return string
    }

    private fun rrulePhoneToRRuleApp(rrule: String): String {
        try {
            var freq = this.getRRuleValue("FREQ", rrule)
            val byDay = this.getRRuleValue("BYDAY", rrule)
            val byMonthDay = this.getRRuleValue("BYMONTHDAY", rrule)
            val byMonth = this.getRRuleValue("BYMONTH", rrule)
            var interval = this.getRRuleValue("INTERVAL", rrule)
            var repeats = this.getRRuleValue("COUNT", rrule)
            var until = this.getRRuleValue("UNTIL", rrule)
            if(until.isEmpty()) {
                until = "0"
            }
            if(repeats.isEmpty()) {
                repeats = "-1"
            }
            if(interval.isEmpty()) {
                interval = "-1"
            }
            freq = when(freq.lowercase()) {
                "daily" -> freq.lowercase()
                "weekly" -> "${freq.lowercase()}($byDay)"
                "monthly" ->"${freq.lowercase()}($byMonthDay)"
                "yearly" ->"${freq.lowercase()}($byMonth)"
                else -> freq.lowercase()
            }
            return "$freq, $interval, $repeats, $until"
        } catch (_: Exception) {}
        return ""
    }

    private fun getRRuleValue(item: String, rrule: String): String {
        try {
            if(rrule.indexOf("${item}=") != -1) {
                val valueAfter = rrule.substring(rrule.indexOf("${item}=") + item.length + 1)
                return valueAfter.substring(0, valueAfter.indexOf(";"))
            }
        } catch (_: Exception) {}
        return ""
    }

    private fun rruleAppToRRulePhone(rrule: String): String {
        val spl = rrule.split(",")
        var type = spl[0]
        val interval = spl[1]
        val repeats = spl[2]
        val untilDate = spl[3]

        var recurrence = ""
        try {
            val items = type.substring(type.indexOf("(") + 1, type.indexOf(")"))
            type = type.substring(0, type.indexOf("("))
            when(type.trim().lowercase()) {
                "daily" -> recurrence = "FREQ=DAILY;"
                "weekly" -> {
                    recurrence = "FREQ=WEEKLY"
                    recurrence = if(items.trim().isNotEmpty()) {
                        "$recurrence;BYDAY=$items"
                    } else {
                        "$recurrence;"
                    }
                }
                "monthly" -> {
                    recurrence = "FREQ=MONTHLY"
                    recurrence = if(items.trim().isNotEmpty()) {
                        "$recurrence;BYMONTHDAY=$items"
                    } else {
                        "$recurrence;"
                    }
                }
                "yearly" -> {
                    recurrence = "FREQ=YEARLY"
                    recurrence = if(items.trim().isNotEmpty()) {
                        "$recurrence;BYMONTH=$items"
                    } else {
                        "$recurrence;"
                    }
                }
                else -> recurrence = "FREQ=DAILY;"
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
        return recurrence
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
            values.put(Calendars.OWNER_ACCOUNT, this.account?.name)
            values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
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
            android.util.Log.e("Exception", "An Error happens!", ex)
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