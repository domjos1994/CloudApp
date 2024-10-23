/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.data.syncer

import de.domjos.cloudapp2.caldav.CalendarCalDav
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import java.util.Date

class CalendarSync(
    private val calendarDAO: CalendarEventDAO,
    private val authenticationDAO: AuthenticationDAO) {
    private val loader: CalendarCalDav
    private val authId: Long

    init {
        val auth = this.authenticationDAO.getSelectedItem()
        this.authId = auth?.id ?: 0L
        this.loader = CalendarCalDav(auth)
    }

    fun sync(
        updateProgress: ((Float, String) -> Unit) = {_,_->},
        onFinish: (()->Unit) = {},
        loadingLabel: String = "",
        deleteLabel: String = "",
        insertLabel: String = "",
        updateLabel: String = "") {

        try {
            // loading data
            var current = 0.0f
            updateProgress(current, loadingLabel)
            val appEvents = this.calendarDAO.getAll(this.authId)
            val serverEventsMap = this.loader.reloadCalendarEvents(updateProgress, loadingLabel)
            val serverEvents = mutableListOf<CalendarEvent>()
            serverEventsMap.values.forEach {serverEvents.addAll(it)}
            val sizeApp = appEvents.size
            val sizeServer = serverEvents.size
            val factor = 100.0f / (sizeApp + sizeServer)

            appEvents.forEach { appEvent ->
                val find = serverEvents.find { it.uid == appEvent.uid }

                if(find == null) {
                    if(appEvent.lastUpdatedEventServer == -1L) {
                        try {
                            // new contact
                            val ts = Date().time
                            appEvent.uid = this.loader.newCalendarEvent(CalendarModel(appEvent.calendar, ""), appEvent)
                            appEvent.lastUpdatedEventServer = ts
                            calendarDAO.updateCalendarEvent(appEvent)

                            current += factor
                            updateProgress(current, String.format(insertLabel, appEvent.toString(), "Server"))
                        } catch (ex: Exception) {
                            current += factor
                            updateProgress(current, ex.message ?: "")
                        }
                    } else {
                        try {
                            calendarDAO.deleteCalendarEvent(appEvent.id)

                            current += factor
                            updateProgress(current, String.format(deleteLabel, appEvent.toString(), "App"))
                        } catch (ex: Exception) {
                            current += factor
                            updateProgress(current, ex.message ?: "")
                        }
                    }
                } else {
                    if(appEvent.lastUpdatedEventApp != find.lastUpdatedEventServer) {
                        if((appEvent.lastUpdatedEventApp ?: 0L) > (find.lastUpdatedEventServer)) {
                            try {
                                val ts = Date().time
                                appEvent.path = find.path
                                appEvent.lastUpdatedEventApp = ts
                                appEvent.lastUpdatedEventServer = ts

                                this.loader.updateCalendarEvent(appEvent)
                                this.calendarDAO.updateCalendarEvent(appEvent)

                                current += factor
                                updateProgress(current, String.format(updateLabel, appEvent.toString(), "Server"))
                            } catch (ex: Exception) {
                                current += factor
                                updateProgress(current, ex.message ?: "")
                            }
                        } else {
                            try {
                                find.id = appEvent.id
                                find.lastUpdatedEventApp = appEvent.lastUpdatedEventServer
                                this.insertAppEvent(find)

                                current += factor
                                updateProgress(current, String.format(insertLabel, find.toString(), "App"))
                            } catch (ex: Exception) {
                                current += factor
                                updateProgress(current, ex.message ?: "")
                            }
                        }
                    }
                }
            }

            val deletedContacts = calendarDAO.getDeletedItems(this.authId)
            serverEvents.forEach { serverEvent ->
                val find = appEvents.find { it.uid == serverEvent.uid }
                val findDeleted = deletedContacts.find { it.uid == serverEvent.uid }

                if(find == null && findDeleted == null) {
                    try {
                        serverEvent.lastUpdatedEventApp = serverEvent.lastUpdatedEventServer
                        this.insertAppEvent(serverEvent)

                        current += factor
                        updateProgress(current, String.format(updateLabel, serverEvent.toString(), "App"))
                    } catch (ex: Exception) {
                        current += factor
                        updateProgress(current, ex.message ?: "")
                    }
                }
                if(findDeleted != null) {
                    try {
                        this.loader.deleteCalendarEvent(serverEvent)
                        this.calendarDAO.deleteCalendarEvent(findDeleted)

                        current += factor
                        updateProgress(current, String.format(deleteLabel, findDeleted.toString(), "Server"))
                    } catch (ex: Exception) {
                        current += factor
                        updateProgress(current, ex.message ?: "")
                    }
                }
            }
        } finally {
            onFinish()
        }
    }

    private fun insertAppEvent(event: CalendarEvent) {
        val uid = event.uid
        try {
            if(this.authenticationDAO.getSelectedItem() != null) {
                val tmp = this.calendarDAO.getAll(this.authenticationDAO.getSelectedItem()!!.id, uid)
                if(tmp != null) {
                    event.eventId = tmp.eventId
                    event.lastUpdatedEventPhone = tmp.lastUpdatedEventPhone
                }
            }
            calendarDAO.deleteCalendarEvent(uid)
        } catch (_: Exception) {}

        if(event.id != 0L) {
            calendarDAO.updateCalendarEvent(event)
        } else {
            calendarDAO.insertCalendarEvent(event)
        }
    }
}