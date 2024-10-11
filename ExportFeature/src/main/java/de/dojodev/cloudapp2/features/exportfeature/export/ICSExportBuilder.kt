/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import de.domjos.cloudapp2.appbasics.R
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.FluentCalendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.property.Completed
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.PercentComplete
import net.fortuna.ical4j.model.property.Priority
import net.fortuna.ical4j.model.property.Status
import net.fortuna.ical4j.model.property.Summary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import net.fortuna.ical4j.model.Date as dt

class ICSExportBuilder(private val context: Context): BaseExportBuilder(context) {
    override suspend fun exportNotifications(): String {
        return this.path
    }

    override suspend fun exportData(): String {
        return this.path
    }

    override suspend fun exportNotes(): String {
        return this.path
    }

    override suspend fun exportCalendars(): String {
        update(context.getString(R.string.export_fetch))

        val calendarEvents = super.calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val events = calendarEvents.map { calendarEvent ->
            VEvent()
                .withProperty(DtStart(dt(stringToDate(calendarEvent.string_from))))
                .withProperty(DtEnd(dt(stringToDate(calendarEvent.string_to))))
                .withProperty(Summary(calendarEvent.title))
                .withProperty(Description(calendarEvent.description))
                .withProperty(Location(calendarEvent.location))
        }

        var fluent = createCalendar()
        events.forEach { event -> fluent = fluent.withComponent(event.getFluentTarget()) }
        val cal = fluent.fluentTarget
        val content = cal.toString()
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportContacts(): String {
        return this.path
    }

    override suspend fun exportToDos(): String {
        update(context.getString(R.string.export_fetch))

        val todos = super.toDoItemDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val vToDos = todos.map { todo ->
            var tmp = VToDo()
                .withProperty(Summary(todo.summary))
                .withProperty(Description(todo.listName))
                .withProperty(PercentComplete(todo.completed))
                .withProperty(Priority(todo.priority))
                .withProperty(Status(todo.status.name))
            if(todo.start != null) {
                tmp = tmp.withProperty(DtStart(dt(todo.start!!)))
            }
            if(todo.end != null) {
                tmp = tmp.withProperty(Completed(DateTime(todo.end!!)))
            }
            tmp
        }

        var fluent = createCalendar()
        vToDos.forEach { event -> fluent = fluent.withComponent(event.getFluentTarget()) }
        val cal = fluent.fluentTarget
        val content = cal.toString()
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportChats(): String {
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.calendars, super.todos)
    }

    override fun getExtension(): List<String> {
        return listOf("ics")
    }

    private fun createCalendar(): FluentCalendar {
       return Calendar()
                .withProdId("-//Dominic Joas//CloudApp2//DE")
                .withDefaults()
    }

    private fun stringToDate(string: String): Date {
        try {
            val sdfDt = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return try {
                sdfDt.parse(string)!!
            } catch (_: Exception) {sdf.parse(string) ?: Date() }
        } catch (_:Exception) {return Date()
        }
    }
}