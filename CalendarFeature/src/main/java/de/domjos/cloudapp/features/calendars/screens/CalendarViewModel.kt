package de.domjos.cloudapp.features.calendars.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.CalendarRepository
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    var uiState: StateFlow<CalendarUiState> = calendarRepository
        .calendarEvents.map<List<CalendarEvent>, CalendarUiState> {CalendarUiState.Success(data = it)}
        .catch { emit(CalendarUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState.Loading)

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

    fun updateCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.update(calendarEvent)
        }
    }

    fun deleteCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            calendarRepository.delete(calendarEvent)
        }
    }
}

sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Error(val throwable: Throwable) : CalendarUiState
    data class Success(val data: List<CalendarEvent>) : CalendarUiState
}