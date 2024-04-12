package de.domjos.cloudapp.features.chats.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.RoomRepository
import de.domjos.cloudapp.webrtc.model.room.Room
import de.domjos.cloudapp.webrtc.model.room.RoomInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _rooms = MutableStateFlow(listOf<Room>())
    val rooms: StateFlow<List<Room>> get() = _rooms

    fun reload() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                roomRepository.reload().collect {
                    _rooms.value = it
                }
            }
        } catch (ex: Exception) {
            println(ex.message)
        }
    }

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
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteRoom(room.token)
        }
    }

    fun hasAuthentications(): Boolean {
        return roomRepository.hasAuthentications()
    }
}