package de.domjos.cloudapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDAO {

    @Query("SELECT * FROM calendarEvents WHERE authId=:authId")
    fun getAll(authId: Long): Flow<List<CalendarEvent>>

    @Query("SELECT count(uid) FROM calendarEvents WHERE `to`>:startTime and `from`<:endTime and authId=:authId order by `to`")
    fun count(startTime: Long, endTime: Long, authId: Long): Long
    @Query("SELECT * FROM calendarEvents WHERE `to`>:startTime and `from`<:endTime and authId=:authId order by `to`")
    fun getItemsByTime(startTime: Long, endTime: Long, authId: Long): List<CalendarEvent>

    @Insert
    fun insertCalendarEvent(calendarEvent: CalendarEvent)

    @Update
    fun updateCalendarEvent(calendarEvent: CalendarEvent)

    @Delete
    fun deleteCalendarEvent(calendarEvent: CalendarEvent)

    @Query("SELECT distinct calendar FROM calendarEvents")
    fun getCalendars(): Flow<List<String>>

    @Query("DELETE FROM calendarEvents WHERE authId=:authId")
    fun clearAll(authId: Long)

    @Query("DELETE FROM calendarEvents WHERE calendar=:calendar")
    fun clearCalendar(calendar: String)
}