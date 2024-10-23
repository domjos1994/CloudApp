package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.caldav.CalendarCalDav
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.data.syncer.CalendarSync
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import javax.inject.Inject

interface CalendarRepository {
    fun loadData(calendar: String, startTime: Long, endTime:Long): List<CalendarEvent>
    fun getCalendars(): List<CalendarModel>
    fun countData(): LinkedList<Int>
    fun import(updateProgress: (Float, String) -> Unit,
               onFinish: ()->Unit,
               loadingLabel: String,
               deleteLabel: String,
               insertLabel: String,
               updateLabel: String)
    fun importCalendar(name: String, updateProgress: (Float, String) -> Unit, onFinish: ()->Unit,
                       loadingLabel: String, deleteLabel: String, insertLabel: String, updateLabel: String)
    fun insert(calendarEvent: CalendarEvent)
    fun update(calendarEvent: CalendarEvent)
    fun delete(calendarEvent: CalendarEvent)
    fun hasAuthentications(): Boolean
}

class DefaultCalendarRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val calendarEventDAO: CalendarEventDAO
) : CalendarRepository {
    private val calendarCalDav = CalendarCalDav(authenticationDAO.getSelectedItem())
    private val days = LinkedList<Int>()

    override fun loadData(calendar: String, startTime: Long, endTime: Long): List<CalendarEvent> {
        return if(authenticationDAO.getSelectedItem()!=null) {
            getRepeatedItems(calendar, startTime, endTime)
        } else {
            listOf()
        }
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override fun getCalendars(): List<CalendarModel> {
        val lst = mutableListOf<CalendarModel>()
        lst.add(CalendarModel("", "", ""))
        lst.addAll(calendarCalDav.getCalendars())
        return lst
    }

    override fun countData(): LinkedList<Int> {
        return days
    }

    override fun import(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit,
                        loadingLabel: String, deleteLabel: String, insertLabel: String, updateLabel: String) {
        val syncer = CalendarSync(this.calendarEventDAO, this.authenticationDAO)
        syncer.sync(updateProgress, onFinish, loadingLabel, deleteLabel, insertLabel, updateLabel)
    }

    override fun importCalendar(
        name: String,
        updateProgress: (Float, String) -> Unit,
        onFinish: ()->Unit,
        loadingLabel: String,
        deleteLabel: String,
        insertLabel: String,
        updateLabel: String
    ) {
        val syncer = CalendarSync(this.calendarEventDAO, this.authenticationDAO)
        syncer.sync(updateProgress, onFinish, loadingLabel, deleteLabel, insertLabel, updateLabel)
    }

    override fun insert(calendarEvent: CalendarEvent) {
        calendarEvent.authId = authenticationDAO.getSelectedItem()?.id!!
        val item = this.calendarCalDav.getCalendars().find { it.name == calendarEvent.calendar }

        if(item != null) {
            calendarEvent.authId = authenticationDAO.getSelectedItem()!!.id
            calendarEvent.lastUpdatedEventApp = Date().time
            this.calendarEventDAO.insertCalendarEvent(calendarEvent)
        }
    }

    override fun update(calendarEvent: CalendarEvent) {
        calendarEvent.authId = authenticationDAO.getSelectedItem()?.id!!
        val item = this.calendarCalDav.getCalendars().find { it.name == calendarEvent.calendar }

        if(item != null) {
            calendarEvent.lastUpdatedEventApp = Date().time
            this.calendarEventDAO.updateCalendarEvent(calendarEvent)
        }
    }

    override fun delete(calendarEvent: CalendarEvent) {
        this.calendarEventDAO.deleteCalendarEvent(calendarEvent)
    }

    private fun getRepeatedItems(calendar: String, startTime: Long, endTime: Long): List<CalendarEvent> {
        val events = calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id!!)
        val filteredEvents = mutableListOf<CalendarEvent>()
        if(calendar.isEmpty()) {
            filteredEvents.addAll(events)
        } else {
            filteredEvents.addAll(events.filter { it.calendar == calendar })
        }
        val allEvents = mutableListOf<CalendarEvent>()
        filteredEvents.forEach { event ->
            allEvents.addAll(addRecurrences(event, startTime, endTime))
        }
        val tmp =  allEvents.filter {
            val start = stringToDate(it.string_from)
            val end = stringToDate(it.string_to)
            val fStart = Date(startTime)
            val fEnd = Date(endTime)

            start.after(fStart) && end.before(fEnd)
        }
        days.clear()
        tmp.forEach { event ->
            val start = stringToDate(event.string_from)
            val end = stringToDate(event.string_to)
            val cStart = Calendar.getInstance()
            cStart.time = start
            val cEnd = Calendar.getInstance()
            cEnd.time = end

            val endDay =
                if(cEnd.get(Calendar.HOUR) == 0 && cEnd.get(Calendar.MINUTE) == 0 && cEnd.get(Calendar.DAY_OF_MONTH) != cStart.get(Calendar.DAY_OF_MONTH))
                    cEnd.get(Calendar.DAY_OF_MONTH) - 1
                else cEnd.get(Calendar.DAY_OF_MONTH)

            for(day in cStart.get(Calendar.DAY_OF_MONTH)..endDay) {
                if(!days.contains(day)) {
                    days.add(day)
                }
            }
        }
        return tmp
    }

    private fun addRecurrences(event: CalendarEvent, startTime: Long, endTime: Long): List<CalendarEvent> {
        val recurrences = mutableListOf<CalendarEvent>()
        if(event.recurrence.isEmpty()) {
            recurrences.add(event)
        } else {
            try {
                val req = event.recurrence
                val type = req.split("(")[0].lowercase()
                val items = req.split("),")[1]
                val index = req.indexOf("(") + 1
                val length = req.indexOf(")")
                val recurItems = req.substring(index, length)
                var interval = items.split(",")[0].trim().toInt()
                val repeats = items.split(",")[1].trim().toInt()
                var until = items.split(",")[2].trim().toLong()
                val lst = try {
                    recurItems.split(",").toList().map { it.trim().toInt() }
                } catch (_: Exception) {
                    listOf()
                }



                interval = if(interval == -1) 1 else interval
                until = if(until == 0L) endTime else until
                val calStart = Calendar.getInstance()
                calStart.timeInMillis = startTime

                val calFrom = Calendar.getInstance()
                calFrom.time = stringToDate(event.string_from)
                val calTo = Calendar.getInstance()
                calTo.time = stringToDate(event.string_to)
                var currentFrom: Calendar = calFrom.clone() as Calendar

                var count = 0
                when(type) {
                    "yearly" -> {
                        while(currentFrom.time.before(Date(until))) {
                            val currentTo = calTo.clone() as Calendar
                            currentFrom = calFrom.clone() as Calendar
                            currentTo.add(Calendar.YEAR, interval * count)
                            currentFrom.add(Calendar.YEAR, interval * count)
                            if (lst.isEmpty()) {
                                val tmpEvent = event.copy()
                                tmpEvent.string_from = dateToString(currentFrom.time)
                                tmpEvent.string_to = dateToString(currentTo.time)

                                if(currentFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                    recurrences.add(tmpEvent)
                                }
                                count++
                            } else {
                                lst.forEach { month ->
                                    val tmpTo = currentTo.clone() as Calendar
                                    val tmpFrom = currentFrom.clone() as Calendar
                                    tmpTo.set(Calendar.MONTH, month)
                                    tmpFrom.set(Calendar.MONTH, month)
                                    val tmpEvent = event.copy()
                                    tmpEvent.string_from = dateToString(currentFrom.time)
                                    tmpEvent.string_to = dateToString(currentTo.time)

                                    if(tmpFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                        recurrences.add(tmpEvent)
                                    }

                                }
                                count++
                            }
                            if(repeats != -1 && repeats <= count) {
                                break
                            }
                        }
                    }
                    "monthly" -> {
                        while(currentFrom.time.before(Date(until))) {
                            val currentTo = calTo.clone() as Calendar
                            currentFrom = calFrom.clone() as Calendar
                            currentTo.add(Calendar.MONTH, interval * count)
                            currentFrom.add(Calendar.MONTH, interval * count)
                            if (lst.isEmpty()) {
                                val tmpEvent = event.copy()
                                tmpEvent.string_from = dateToString(currentFrom.time)
                                tmpEvent.string_to = dateToString(currentTo.time)

                                if(currentFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                    recurrences.add(tmpEvent)
                                }
                                count++
                            } else {
                                lst.forEach { day ->
                                    val tmpTo = currentTo.clone() as Calendar
                                    val tmpFrom = currentFrom.clone() as Calendar
                                    tmpTo.set(Calendar.DAY_OF_MONTH, day)
                                    tmpFrom.set(Calendar.DAY_OF_MONTH, day)
                                    val tmpEvent = event.copy()
                                    tmpEvent.string_from = dateToString(currentFrom.time)
                                    tmpEvent.string_to = dateToString(currentTo.time)

                                    if(tmpFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                        recurrences.add(tmpEvent)
                                    }
                                }
                                count++
                            }
                            if(repeats != -1 && repeats <= count) {
                                break
                            }
                        }
                    }
                    "weekly" -> {
                        while(currentFrom.time.before(Date(until))) {
                            val currentTo = calTo.clone() as Calendar
                            currentFrom = calFrom.clone() as Calendar
                            currentTo.add(Calendar.WEEK_OF_YEAR, interval * count)
                            currentFrom.add(Calendar.WEEK_OF_YEAR, interval * count)
                            if (lst.isEmpty()) {
                                val tmpEvent = event.copy()
                                tmpEvent.string_from = dateToString(currentFrom.time)
                                tmpEvent.string_to = dateToString(currentTo.time)

                                if(currentFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                    recurrences.add(tmpEvent)
                                }
                                count++
                            } else {
                                lst.forEach { day ->
                                    val tmpTo = currentTo.clone() as Calendar
                                    val tmpFrom = currentFrom.clone() as Calendar
                                    tmpTo.set(Calendar.DAY_OF_WEEK, day)
                                    tmpFrom.set(Calendar.DAY_OF_WEEK, day)
                                    val tmpEvent = event.copy()
                                    tmpEvent.string_from = dateToString(currentFrom.time)
                                    tmpEvent.string_to = dateToString(currentTo.time)

                                    if(tmpFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                        recurrences.add(tmpEvent)
                                    }
                                }
                                count++
                            }
                            if(repeats != -1 && repeats <= count) {
                                break
                            }
                        }
                    }
                    "daily" -> {
                        while(currentFrom.time.before(Date(until))) {
                            val currentTo = calTo.clone() as Calendar
                            currentFrom = calFrom.clone() as Calendar
                            currentTo.add(Calendar.DAY_OF_MONTH, interval * count)
                            currentFrom.add(Calendar.DAY_OF_MONTH, interval * count)
                            if (lst.isEmpty()) {
                                val tmpEvent = event.copy()
                                tmpEvent.string_from = dateToString(currentFrom.time)
                                tmpEvent.string_to = dateToString(currentTo.time)

                                if(currentFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                    recurrences.add(tmpEvent)
                                }
                                count++
                            } else {
                                lst.forEach { day ->
                                    val tmpTo = currentTo.clone() as Calendar
                                    val tmpFrom = currentFrom.clone() as Calendar
                                    tmpTo.set(Calendar.DAY_OF_WEEK, day)
                                    tmpFrom.set(Calendar.DAY_OF_WEEK, day)
                                    val tmpEvent = event.copy()
                                    tmpEvent.string_from = dateToString(currentFrom.time)
                                    tmpEvent.string_to = dateToString(currentTo.time)

                                    if(tmpFrom.get(Calendar.YEAR) == calStart.get(Calendar.YEAR)) {
                                        recurrences.add(tmpEvent)
                                    }
                                }
                                count++
                            }
                            if(repeats != -1 && repeats <= count) {
                                break
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                recurrences.add(event)
            }
        }
        return recurrences
    }
}

fun stringToOtherFormat(string: String, format: String): String {
    try {
        val sdfOutput = SimpleDateFormat(format, Locale.getDefault())
        val dt = stringToDate(string)
        return try {
            sdfOutput.format(dt)
        } catch (_: Exception) { "" }
    } catch (_: Exception) {return ""}
}

fun stringToDate(string: String): Date {
    try {
        val sdfDt = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return try {
            sdfDt.parse(string)!!
        } catch (_: Exception) {sdf.parse(string) ?: Date() }
    } catch (_:Exception) {return Date()}
}

fun dateToString(date: Date): String {
    try {
        val sdfDt = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return try {
            sdfDt.format(date)
        } catch (_: Exception) {sdf.format(date) ?: ""}
    } catch (_: Exception) {return ""}
}