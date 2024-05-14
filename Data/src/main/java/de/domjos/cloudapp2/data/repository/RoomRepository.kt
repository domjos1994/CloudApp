package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.rest.model.room.RoomInput
import de.domjos.cloudapp2.rest.requests.RoomRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface RoomRepository {
    val rooms: Flow<List<Room>>

    fun reload(): Flow<List<Room>>
    suspend fun insertRoom(input: RoomInput)
    suspend fun updateRoom(token: String, name: String?, description: String?)
    suspend fun deleteRoom(token: String)
    fun hasAuthentications(): Boolean
}

class DefaultRoomRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : RoomRepository {
    private val request: RoomRequest
        get() = RoomRequest(authenticationDAO.getSelectedItem())
    override var rooms: Flow<List<Room>> = reload()

    override fun reload(): Flow<List<Room>> {
        return this.request.getRooms()
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    @Throws(Exception::class)
    override suspend fun insertRoom(input: RoomInput) {
        request.addRoom(input)
    }

    @Throws(Exception::class)
    override suspend fun updateRoom(token: String, name: String?, description: String?) {
        if(name != null) {
            request.renameRoom(token, name)
        }
        if(description != null) {
            request.updateDescription(token, description)
        }
    }

    @Throws(Exception::class)
    override suspend fun deleteRoom(token: String) {
        request.deleteRoom(token)
    }
}