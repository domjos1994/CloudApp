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
import android.provider.CalendarContract.SyncState
import de.domjos.cloudapp2.caldav.CalendarCalDav
import de.domjos.cloudapp2.database.model.Authentication
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.Long

@Suppress("JoinDeclarationAndAssignment")
class PhoneCalendarHelper(private val account: Account?, private val contentResolver: ContentResolver, private val db: DB) {
    private var authId: Long
    private var authentication: Authentication
    private var calendars: Map<Long, String>? = null
    private var ids: MutableList<Long>
    private val itemsToAdd: MutableList<CalendarEvent>

    init {
        this.itemsToAdd = mutableListOf<CalendarEvent>()
        this.authentication = this.db.authenticationDao().getSelectedItem()!!
        this.authId = this.authentication.id
        this.ids = mutableListOf<Long>()
    }

    fun sync() {
        this.ids.clear()
        this.itemsToAdd.clear()
        this.insertLogMessage("Start syncing calendars!")
        this.insertLogMessage("Create Calendars")
        this.createCalendars()

        this.updateByStatus()
    }

    private fun updateByStatus() {
        val phoneEvents = this.getData()
        val appEvents = this.db.calendarEventDao().getAll(this.authId)

        // -1 do nothing
        // 0 insert phone item
        // 1 insert app item
        // 2 update phone item
        // 3 update app item
        val states = mutableListOf<Array<Long>>()
        phoneEvents.forEach { item ->
            val find = appEvents.find { it.id==item[3] }
            if(find == null) {
                if(item[3] > 0L) {
                    this.deletePhoneItem(item[0])
                } else {
                    states.add(arrayOf(item[0], 1))
                }
            } else {
                if((find.lastUpdatedEventApp ?: 0L) > item[2]) {
                    states.add(arrayOf(find.id, 2))
                } else if((find.lastUpdatedEventApp ?: 0L) < item[2]) {
                    states.add(arrayOf(item[0], 3))
                }
            }
        }
        appEvents.forEach {item ->
            val find = phoneEvents.find { it[3]==item.id }
            if(find == null) {
                if(item.eventId.isNotEmpty()) {
                    this.deleteAppItem(item.id)
                } else {
                    states.add(arrayOf(item.id, 0))
                }
            }
        }

        states.forEach {
            when(it[1]) {
                0L -> this.insertPhoneItem(it[0])
                1L -> this.insertAppItem(it[0])
                2L -> this.updatePhoneItem(it[0])
                3L -> this.updateAppItem(it[0])
            }
        }
    }

    private fun getData() : MutableList<Array<Long>> {
        val items: MutableList<Array<Long>> = mutableListOf<Array<Long>>()
        try {
            val uri = this.asSyncAdapter(Events.CONTENT_URI)
            val projection = arrayOf(Events._ID, Events.DIRTY, Events.LAST_SYNCED, Events.SYNC_DATA1)
            val where = "${Events.OWNER_ACCOUNT}=?"
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
            val where = "${Events._ID}=?"
            val args = arrayOf("$id")
            this.contentResolver.delete(asSyncAdapter(Events.CONTENT_URI), where, args)
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

        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert phone item in app!")
        }
    }

    private fun insertPhoneItem(id: Long) {
        try {

        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert app item in phone!")
        }
    }

    private fun updatePhoneItem(id: Long) {
        try {

        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem updating app item in phone!")
        }
    }

    private fun updateAppItem(id: Long) {
        try {

        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem updating phone item in app!")
        }
    }

    private fun updateDirty(eventId: Long) {
        try {
            val uri = this.asSyncAdapter(Events.CONTENT_URI)
            val where = "${Events._ID}=?"
            val args = arrayOf("$eventId")

            val values = ContentValues()
            values.put(Events.DIRTY, 0)
            values.put(Events.LAST_SYNCED, Date().time)

            this.contentResolver.update(uri, values, where, args)
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem update dirty-bit!")
        }
    }

    private fun getPhoneEvent(appEvent: CalendarEvent?): CalendarEvent? {
        var event: CalendarEvent? = null
        try {
            if((appEvent?.eventId ?: "f").isEmpty()) {
                return null
            } else {
                val projection = arrayOf(
                    Events.TITLE, Events.CALENDAR_ID, Events.DTSTART, Events.DTEND,
                    Events.RRULE, Events.STATUS, Events.EVENT_LOCATION, Events.DESCRIPTION,
                    Events.SYNC_DATA1, Events.LAST_SYNCED, Events.DIRTY, Events._ID)
                val where = if(appEvent != null) "${Events._ID}=?" else null
                val args = if(appEvent != null) arrayOf(appEvent.eventId) else null

                val cursor = this.contentResolver.query(asSyncAdapter(Events.CONTENT_URI), projection, where, args, null)
                cursor?.use { c ->
                    while(c.moveToNext()) {
                        val eventId = this.getValue(c, Events._ID, 0L) ?: 0L
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
                        val location = this.getValue(c, Events.EVENT_LOCATION, "") ?: ""
                        val description = this.getValue(c, Events.DESCRIPTION, "") ?: ""
                        val id = this.getValue(c, Events.SYNC_DATA1, "") ?: ""
                        val lastSynced = this.getValue(c, Events.LAST_SYNCED, 0L) ?: 0L
                        val dirty = this.getValue(c, Events.DIRTY, 0) ?: 0

                        val tmp = CalendarEvent(
                            id = if(id.isEmpty()) appEvent?.id ?: 0L else id.toLong(),
                            uid = appEvent?.uid ?: "",
                            string_from = dateToString(Date(start)),
                            string_to = dateToString(Date(end)),
                            title = title,
                            location = location,
                            description = description,
                            confirmation = status,
                            categories = appEvent?.categories ?: "",
                            color = appEvent?.color ?: "",
                            calendar = calendar,
                            eventId = appEvent?.eventId ?: "$eventId",
                            lastUpdatedEventServer = appEvent?.lastUpdatedEventServer ?: -1L,
                            lastUpdatedEventPhone = lastSynced,
                            recurrence = rrule
                        )
                        tmp.lastUpdatedEventApp = appEvent?.lastUpdatedEventApp ?: 0L
                        if(dirty == 1 && lastSynced == 0L && id=="") {
                            this.itemsToAdd.add(tmp)
                        } else {
                            if(id.isNotEmpty()) {
                                val find = this.db.calendarEventDao().getAll(this.authId).find { it.id==id.toLong() }
                                if(find == null) {
                                    val where = "${Events._ID}=?"
                                    val args = arrayOf(id)
                                    this.contentResolver.delete(asSyncAdapter(Events.CONTENT_URI), where, args)
                                }
                            } else {
                                event = tmp
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert or update data", event)
        }
        return event
    }

    private fun insertOrUpdatePhoneEvent(calendarEvent: CalendarEvent): CalendarEvent? {

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
                calendarEvent.lastUpdatedEventPhone = ts
                contentValues.put(Events.LAST_SYNCED, ts)
                contentValues.put(Events.OWNER_ACCOUNT, account?.name)

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
                this.ids.add(calendarEvent.eventId.toLong())

                return calendarEvent
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem get Calendar!", appEvent = calendarEvent)
        }
        return null
    }

    private fun stringToDate(string: String): Date {
        var date: Date = Date()
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
        return recurrence
    }

    private fun insertOrUpdateSyncState() {
        try {
            val uri = asSyncAdapter(SyncState.CONTENT_URI)
            var where = "${SyncState.ACCOUNT_NAME}=? AND ${SyncState.ACCOUNT_TYPE}=?"
            var args = arrayOf(this.account?.name ?: "", this.account?.type ?: "")
            val projection = arrayOf(SyncState._ID, SyncState.DATA)

            var id = -1
            var data = ""
            val cursor = this.contentResolver.query(uri, projection, where, args, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    id = this.getValue(c, SyncState._ID, -1) ?: -1
                    data = this.getValue(c, SyncState.DATA, "") ?: ""
                }
            }

            if(data.isEmpty()) {
                data = "${Date().time}:${this.ids.joinToString(",")}"
            } else {
                val lastIds = data.split(":")[1].split(",").map { it.toLong() }.toMutableList()
                this.ids.forEach { id ->
                    val find = lastIds.find { it == id }
                    if(find == null) {
                        lastIds.add(id)
                    }
                }
                data = "${Date().time}:${lastIds.joinToString(",")}"
            }

            val values = ContentValues()
            values.put(SyncState.ACCOUNT_NAME, this.account?.name)
            values.put(SyncState.ACCOUNT_TYPE, this.account?.type)
            values.put(SyncState.DATA, data)

            if(id != -1) {
                where = "${SyncState._ID}=?"
                args = arrayOf("$id")
                this.contentResolver.update(uri, values, where, args)
            } else {
                this.contentResolver.insert(uri, values)
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem insert Sync-State!")
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
            values.put(Calendars.OWNER_ACCOUNT, this.account?.name)
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