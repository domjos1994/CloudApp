package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.SyncResult
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import de.domjos.cloudapp2.data.repository.stringToDate
import de.domjos.cloudapp2.database.DB
import java.util.Date


class CalendarSyncAdapter @JvmOverloads constructor(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    private lateinit var account: Account
    override fun onPerformSync(
        account: Account?,
        bundle: Bundle?,
        authority: String?,
        contentProviderClient: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        this.account = account!!

        val db = DB.newInstance(this.context)
        val id = db.authenticationDao().getSelectedItem()?.id

        if (id != null) {
            val calendars = db.calendarEventDao().getCalendars(id)
            calendars.forEach { calendar ->
                val cid = addCalendar(calendar)
                val events =
                    db.calendarEventDao().getAll(id)

                events.filter { it.calendar == calendar }.forEach { event ->
                    var eventId = -1L
                    try {
                        val projection = arrayOf(CalendarContract.Events._ID)
                        val selection = "${CalendarContract.Events.CALENDAR_ID}=? AND ${CalendarContract.Events._ID}=?"
                        val selectionArgs = arrayOf("$cid", event.eventId)

                        val cursor = this.contentResolver.query(CalendarContract.Events.CONTENT_URI, projection, selection, selectionArgs, null)
                        if(cursor != null) {
                            if(cursor.moveToFirst()) {
                                val index = cursor.getColumnIndex(CalendarContract.Events._ID)
                                if(index > -1) {
                                    eventId = cursor.getLong(index)
                                }
                            }
                            cursor.close()
                        }
                    } catch (ex: Exception) {
                        Log.e(this.javaClass.name, ex.message, ex)
                    }

                    try {
                        if(event.title != "") {
                            val values = ContentValues().apply {
                                put(CalendarContract.Events.DTSTART, stringToDate(event.string_from).time)
                                put(CalendarContract.Events.DTEND, stringToDate(event.string_to).time)
                                put(CalendarContract.Events.TITLE, event.title)

                                if(event.description != "") {
                                    put(CalendarContract.Events.DESCRIPTION, event.description)
                                }
                                put(CalendarContract.Events.CALENDAR_ID, cid)
                                if(event.location != "") {
                                    put(CalendarContract.Events.EVENT_LOCATION, event.location)
                                }
                                if(event.color != "") {
                                    put(CalendarContract.Events.EVENT_COLOR, event.color)
                                }
                            }

                            if(eventId == -1L) {
                                val uri = this.contentResolver.insert(asSyncAdapter(CalendarContract.Events.CONTENT_URI)!!, values)
                                eventId = ContentUris.parseId(uri!!)
                            } else {
                                val selection = "${CalendarContract.Events.CALENDAR_ID}=? AND ${CalendarContract.Events._ID}=?"
                                val selectionArgs = arrayOf("$cid", event.eventId)
                                this.contentResolver.update(asSyncAdapter(CalendarContract.Events.CONTENT_URI)!!, values, selection, selectionArgs)
                            }
                            db.calendarEventDao().updateEventSync("$eventId", Date().time, event.id)
                        }
                    } catch (ex: Exception) {
                        Log.e(this.javaClass.name, ex.message, ex)
                    }
                }
            }
        }
    }

    private fun addCalendar(name: String): Long {
        var id = -1L
        try {
            val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME)
            val selection = "${CalendarContract.Calendars.VISIBLE}=1 AND ${CalendarContract.Calendars.NAME}=?"
            val selectionArgs = arrayOf(name)


            val cursor = this.contentResolver.query(this.uri, projection, selection, selectionArgs, null)
            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    if(index > -1) {
                        id = cursor.getLong(index)
                    }
                }
                cursor.close()
            }
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
        }

        try {
            if(id == -1L) {
                val cv = ContentValues()
                cv.put(CalendarContract.Calendars.ACCOUNT_NAME, this.account.name)
                cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, this.account.type)
                cv.put(CalendarContract.Calendars.NAME, name)
                cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name)
                cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
                cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, true)
                cv.put(CalendarContract.Calendars.VISIBLE, 1)
                cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
                val uri = this.contentResolver.insert(asSyncAdapter(this.uri)!!, cv)
                return ContentUris.parseId(uri!!)
            }
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
        }

        return id
    }

    private fun asSyncAdapter(baseUri: Uri): Uri? {
        return try {
            baseUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, this.account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, this.account.type).build()
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
            null
        }
    }
}