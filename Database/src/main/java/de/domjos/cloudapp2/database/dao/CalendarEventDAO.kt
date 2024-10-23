/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import kotlinx.coroutines.flow.Flow

/**
 * DAO to work with calendarEvents
 * getting:
 * @property getAll
 * @property getAllWithoutFlow
 * @property getCalendars
 * counting:
 * @property count
 * adding:
 * @property insertCalendarEvent
 * updating:
 * @property updateCalendarEvent
 * deleting:
 * @property deleteCalendarEvent
 * @property clear
 * @property clearCalendar
 * @see de.domjos.cloudapp2.database.model.Authentication
 * @author Dominic Joas
 */
@Dao
interface CalendarEventDAO {

    /**
     * Get List of Calendars
     * @param authId id of Authentication
     * @return List with calendar-names
     */
    @Query("SELECT DISTINCT calendar FROM calendarEvents WHERE authId=:authId AND deleted=0")
    fun getCalendars(authId: Long): List<String>

    @Query("SELECT * FROM calendarEvents WHERE authId=:authId AND deleted=1")
    fun getDeletedItems(authId: Long): List<CalendarEvent>

    /**
     * Get Flow with List of Calendars
     * @return Flow with List with calendar-names
     */
    @Query("SELECT distinct calendar FROM calendarEvents WHERE deleted=0")
    fun getCalendars(): Flow<List<String>>

    /**
     * Get a Flow with CalendarEvents auf authentication
     * @param authId id of authentication
     * @return flow with a list of Events
     */
    @Query("SELECT * FROM calendarEvents WHERE authId=:authId AND deleted=0")
    fun getAll(authId: Long): List<CalendarEvent>

    /**
     * Get CalendarEvents auf authentication
     * @param authId id of authentication
     * @return a list of Events
     */
    @Query("SELECT * FROM calendarEvents WHERE authId=:authId AND deleted=0")
    fun getAllWithoutFlow(authId: Long): List<CalendarEvent>

    /**
     * Get Calendar-Event by authid and uid
     * @param authId id of authentication
     * @param uid id of event
     * @return the calendar-event or null
     */
    @Query("SELECT * FROM calendarEvents WHERE authId=:authId and uid=:uid AND deleted=0")
    fun getAll(authId: Long, uid: String): CalendarEvent?

    /**
     * Update the synced event id
     * @param eventId id of synced event
     * @param lastUpdated timestamp
     * @param id id of event
     */
    @Query("UPDATE calendarEvents SET eventId=:eventId, lastUpdatedEventPhone=:lastUpdated WHERE id=:id AND deleted=0")
    fun updateEventSync(eventId: String, lastUpdated: Long, id: Long)

    /**
     * Insert new Event
     * @param calendarEvent event
     */
    @Insert
    fun insertCalendarEvent(calendarEvent: CalendarEvent): Long

    /**
     * Update existing event
     * @param calendarEvent updated event
     */
    @Update
    fun updateCalendarEvent(calendarEvent: CalendarEvent)

    /**
     * Delete existing event
     * @param calendarEvent existing event
     */
    @Delete
    fun deleteCalendarEvent(calendarEvent: CalendarEvent)

    @Query("UPDATE calendarEvents SET deleted=1 WHERE id=:id")
    fun deleteCalendarEvent(id: Long)

    @Query("UPDATE calendarEvents SET deleted=1 WHERE uid=:uid")
    fun deleteCalendarEvent(uid: String)

    /**
     * Clear data by authentication and uid
     * @param authId id of authentication
     * @param uid uid of event
     */
    @Query("DELETE FROM calendarEvents WHERE authId=:authId and uid=:uid")
    fun clear(authId: Long, uid: String)

    /**
     * clear data by calendar
     * @param calendar name of calendar
     */
    @Query("DELETE FROM calendarEvents WHERE calendar=:calendar")
    fun clearCalendar(calendar: String)
}