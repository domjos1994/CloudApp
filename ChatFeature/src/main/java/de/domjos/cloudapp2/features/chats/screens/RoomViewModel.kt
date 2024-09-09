package de.domjos.cloudapp2.features.chats.screens

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.RoomRepository
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.ConnectivityViewModel
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
) : ConnectivityViewModel() {
    private val _rooms = MutableStateFlow(listOf<Room>())
    val rooms: StateFlow<List<Room>> get() = _rooms
    private val _users = MutableStateFlow(listOf<User?>())
    val users: StateFlow<List<User?>> get() = _users

    override fun init() {
        loadUsers()
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                roomRepository.reload().collect {
                    _rooms.value = it
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun insertRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val roomInput = RoomInput(room.type, room.invite, roomName = room.displayName)
                roomRepository.insertRoom(roomInput)
                printMessage(R.string.chats_rooms_created, this)
            } catch (ex: Exception) {
                if(ex.message == "200") {
                    printException(ex, R.string.chats_rooms_exists, this)
                } else if(ex.message?.toIntOrNull() != null) {
                    printException(ex, R.string.chats_rooms_error, this)
                } else {
                    printException(ex, this)
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
                printException(ex, this)
            }
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                roomRepository.deleteRoom(room.token)
            } catch (ex: Exception) {
                printException(ex, this)
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
                printException(ex, this)
            }
        }
    }
}