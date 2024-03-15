package de.domjos.cloudapp.features.chats.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.RoomRepository
import de.domjos.cloudapp.webrtc.model.room.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    var uiState: StateFlow<RoomUiState> = roomRepository
        .rooms.map<List<Room>, RoomUiState> {RoomUiState.Success(data = it)}
        .catch { emit(RoomUiState.Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RoomUiState.Loading)

}

sealed interface RoomUiState {
    data object Loading : RoomUiState
    data class Error(val throwable: Throwable) : RoomUiState
    data class Success(val data: List<Room>) : RoomUiState
}