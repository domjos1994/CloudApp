package de.domjos.cloudapp2.features.calendars.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.data.repository.CalendarRepository
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _events = MutableStateFlow(listOf<CalendarEvent>())
    val events: StateFlow<List<CalendarEvent>> get() = _events
    private val _days = MutableStateFlow(listOf<Int>())
    val days: StateFlow<List<Int>> get() = _days
    private val _calendars = MutableStateFlow(listOf<CalendarModel>())
    val calendars: StateFlow<List<CalendarModel>> get() = _calendars
    val message = MutableLiveData<String?>()

    private val _date = MutableStateFlow(Date())
    val date: StateFlow<Date> get() = _date

    private var calendar: String = ""
    private var start: Long = 0L
    private var end: Long = 0L

    fun load(calendar: String, startTime: Long, endTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                this@CalendarViewModel.calendar = calendar
                this@CalendarViewModel.start = startTime
                this@CalendarViewModel.end = endTime
                _events.value = calendarRepository.loadData(calendar, startTime, endTime)
                _date.value = Date(startTime)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun getCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _calendars.value = calendarRepository.getCalendars()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun count(calendar: String, event: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _days.value = calendarRepository.countData(calendar, event)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun import(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, progressLabel: String, saveLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                calendarRepository.reload(updateProgress, progressLabel, saveLabel)
                _events.value = calendarRepository.loadData(calendar, start, end)
                _date.value = Date(start)
                onFinish()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun insertCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(calendarEvent.uid.isNotEmpty() && calendarEvent.id != 0L) {
                    calendarRepository.update(calendarEvent)
                } else {
                    calendarRepository.insert(calendarEvent)
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                calendarRepository.delete(calendarEvent)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return calendarRepository.hasAuthentications()
    }
}