package de.domjos.cloudapp.features.chats.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
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
    val message = MutableLiveData<String?>()

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                roomRepository.reload().collect {
                    _rooms.value = it
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun insertRoom(room: Room) {
        viewModelScope.launch {
            try {
                val roomInput = RoomInput("", room.type, room.displayName, room.description)
                roomRepository.insertRoom(roomInput)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch {
            try {
                roomRepository.updateRoom(
                    room.token,
                    room.displayName,
                    room.description
                )
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                roomRepository.deleteRoom(room.token)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return roomRepository.hasAuthentications()
    }
}