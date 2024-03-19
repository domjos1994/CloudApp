package de.domjos.cloudapp.features.chats.screens

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.RoomRepository
import de.domjos.cloudapp.webrtc.model.room.Room
import de.domjos.cloudapp.webrtc.model.room.RoomInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    var uiState: StateFlow<RoomUiState> = roomRepository
        .rooms.map<List<Room>, RoomUiState> {RoomUiState.Success(data = it)}
        .catch { emit(RoomUiState.Error(it)) }
        .stateIn(viewModelScope.plus(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), RoomUiState.Loading)

    @Throws(Exception::class)
    fun insertRoom(room: Room) {
        viewModelScope.launch {
            val roomInput = RoomInput("", room.type, room.displayName, room.description)

            roomRepository.insertRoom(roomInput)
        }
    }

    @Throws(Exception::class)
    fun updateRoom(room: Room) {
        viewModelScope.launch {
            roomRepository.updateRoom(
                room.token,
                room.displayName,
                room.description
            )
        }
    }

    @Throws(Exception::class)
    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            roomRepository.deleteRoom(room.token)
        }
    }

    @Throws(Exception::class)
    fun getAvatar(room: Room, onGet: (Bitmap?)->Unit) {
        viewModelScope.launch {
            onGet(roomRepository.getAvatar(room.token))
        }
    }
}

sealed interface RoomUiState {
    data object Loading : RoomUiState
    data class Error(val throwable: Throwable) : RoomUiState
    data class Success(val data: List<Room>) : RoomUiState
}