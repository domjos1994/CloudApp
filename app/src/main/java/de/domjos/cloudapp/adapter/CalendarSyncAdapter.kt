package de.domjos.cloudapp.adapter

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
import de.domjos.cloudapp.R
import de.domjos.cloudapp.database.DB
import java.util.TimeZone


class CalendarSyncAdapter @JvmOverloads constructor(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    override fun onPerformSync(
        account: Account?,
        bundle: Bundle?,
        authority: String?,
        contentProviderClient: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        val db = DB.newInstance(this.context)
        val id = db.authenticationDao().getSelectedItem()?.id

        if (id != null) {
            val calendars = db.calendarEventDao().getCalendars(id)
            calendars.forEach { calendar ->
                val cid = addCalendar(calendar, account!!)
                val events =
                    db.calendarEventDao().getItemsByTimeAndCalendar(calendar, 0, Long.MAX_VALUE, id)

                events.forEach { event ->
                    try {
                        val values = ContentValues().apply {
                            put(CalendarContract.Events.DTSTART, event.from)
                            put(CalendarContract.Events.DTEND, event.to)
                            put(CalendarContract.Events.TITLE, event.title)
                            put(CalendarContract.Events.DESCRIPTION, event.description)
                            put(CalendarContract.Events.CALENDAR_ID, cid)
                            put(
                                CalendarContract.Events.EVENT_TIMEZONE,
                                TimeZone.getDefault().toString()
                            )
                            put(CalendarContract.Events.EVENT_LOCATION, event.location)
                            put(CalendarContract.Events.EVENT_COLOR, event.color)
                        }
                        this.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    private fun addCalendar(name: String, account: Account): Long {
        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, this.context.getString(R.string.app_name))
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, this.context.getString(R.string.sys_account_type))
        contentValues.put(CalendarContract.Calendars.NAME, name)
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name)
        contentValues.put(CalendarContract.Calendars.VISIBLE, "true")
        val uri = this.contentResolver.insert(
            asSyncAdapter(
                account.name,
                account.type
            ), contentValues
        )
        return ContentUris.parseId(uri!!)
    }

    private fun asSyncAdapter(account: String, accountType: String): Uri {
        return this.uri.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType).build()
    }
}