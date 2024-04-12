package de.domjos.cloudapp.features.calendars.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.CalendarRepository
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _events = MutableStateFlow(listOf<CalendarEvent>())
    val events: StateFlow<List<CalendarEvent>> get() = _events
    private val _days = MutableStateFlow(listOf<Int>())
    val days: StateFlow<List<Int>> get() = _days
    private val _calendars = MutableStateFlow(listOf<String>())
    val calendars: StateFlow<List<String>> get() = _calendars

    fun load(calendar: String, startTime: Long, endTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.value = calendarRepository.loadData(calendar, startTime, endTime)
        }
    }

    fun getCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            _calendars.value = calendarRepository.getCalendars()
        }
    }

    fun count(calendar: String, event: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            _days.value = calendarRepository.countData(calendar, event)
        }
    }

    fun reload(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, progressLabel: String, saveLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.reload(updateProgress, progressLabel, saveLabel)
            onFinish()
        }
    }

    fun insertCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.insert(calendarEvent)
        }
    }

    fun deleteCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.delete(calendarEvent)
        }
    }

    fun hasAuthentications(): Boolean {
        return calendarRepository.hasAuthentications()
    }
}