package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDAO {

    @Query("SELECT DISTINCT calendar FROM calendarEvents WHERE authId=:authId")
    fun getCalendars(authId: Long): List<String>

    @Query("SELECT * FROM calendarEvents WHERE authId=:authId")
    fun getAll(authId: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendarEvents WHERE authId=:authId and uid=:uid")
    fun getAll(authId: Long, uid: String): CalendarEvent?

    @Query("SELECT count(uid) FROM calendarEvents WHERE `to`>:startTime and `from`<:endTime and authId=:authId order by `from`")
    fun count(startTime: Long, endTime: Long, authId: Long): Long
    @Query("SELECT * FROM calendarEvents WHERE `to`>:startTime and `from`<:endTime and authId=:authId order by `from`")
    fun getItemsByTime(startTime: Long, endTime: Long, authId: Long): List<CalendarEvent>

    @Query("SELECT * FROM calendarEvents WHERE calendar=:calendar and `to`>:startTime and `from`<:endTime and authId=:authId order by `from`")
    fun getItemsByTimeAndCalendar(calendar: String, startTime: Long, endTime: Long, authId: Long): List<CalendarEvent>

    @Query("SELECT count(uid) FROM calendarEvents WHERE calendar=:calendar and `to`>:startTime and `from`<:endTime and authId=:authId order by `from`")
    fun count(calendar: String, startTime: Long, endTime: Long, authId: Long): Long

    @Query("UPDATE calendarEvents SET eventId=:eventId, lastUpdatedEventPhone=:lastUpdated WHERE id=:id")
    fun updateEventSync(eventId: String, lastUpdated: Long, id: Long)

    @Insert
    fun insertCalendarEvent(calendarEvent: CalendarEvent)

    @Update
    fun updateCalendarEvent(calendarEvent: CalendarEvent)

    @Delete
    fun deleteCalendarEvent(calendarEvent: CalendarEvent)

    @Query("SELECT distinct calendar FROM calendarEvents")
    fun getCalendars(): Flow<List<String>>

    @Query("DELETE FROM calendarEvents WHERE authId=:authId and uid=:uid")
    fun clear(authId: Long, uid: String)

    @Query("DELETE FROM calendarEvents WHERE calendar=:calendar")
    fun clearCalendar(calendar: String)
}