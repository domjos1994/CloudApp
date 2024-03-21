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

    @Query("SELECT * FROM calendarEvents")
    fun getAll(): Flow<List<CalendarEvent>>

    @Query("SELECT count(uid) FROM calendarEvents")
    fun count(): Flow<Long>
    @Query("SELECT * FROM calendarEvents WHERE title=:title")
    fun getItemByTitle(title: String): CalendarEvent?

    @Insert
    fun insertCalendarEvent(calendarEvent: CalendarEvent)

    @Update
    fun updateCalendarEvent(calendarEvent: CalendarEvent)

    @Delete
    fun deleteCalendarEvent(calendarEvent: CalendarEvent)

    @Query("SELECT distinct calendar FROM calendarEvents")
    fun getCalendars(): Flow<List<String>>

    @Query("DELETE FROM calendarEvents")
    fun clearAll()

    @Query("DELETE FROM calendarEvents WHERE calendar=:calendar")
    fun clearCalendar(calendar: String)
}