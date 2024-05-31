package de.domjos.cloudapp2.features.chats.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.RoomRepository
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.rest.model.room.RoomInput
import de.domjos.cloudapp2.rest.model.user.User
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

    private val _users = MutableStateFlow(listOf<User?>())
    val users: StateFlow<List<User?>> get() = _users

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

    fun insertRoom(room: Room, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomInput = RoomInput(room.type, room.invite, roomName = room.displayName)
                roomRepository.insertRoom(roomInput)
                message.postValue(context.getString(R.string.chats_rooms_created))
            } catch (ex: Exception) {
                if(ex.message == "200") {
                    message.postValue(context.getString(R.string.chats_rooms_exists))
                } else if(ex.message?.toIntOrNull() != null) {
                    message.postValue(context.getString(R.string.chats_rooms_error))
                } else {
                    message.postValue(ex.message)
                    Log.e(this.javaClass.name, ex.message, ex)
                }
            }
        }
    }

    fun updateRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
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

    fun loadUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _users.value = roomRepository.getUsers()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }
}