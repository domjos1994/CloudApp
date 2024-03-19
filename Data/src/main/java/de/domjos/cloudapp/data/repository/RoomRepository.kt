package de.domjos.cloudapp.data.repository

import android.graphics.Bitmap
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webrtc.model.room.Room
import de.domjos.cloudapp.webrtc.model.room.RoomInput
import de.domjos.cloudapp.webrtc.requests.AvatarRequest
import de.domjos.cloudapp.webrtc.requests.RoomRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface RoomRepository {
    val rooms: Flow<List<Room>>
    val request: RoomRequest
    val avatarRequest: AvatarRequest
    suspend fun insertRoom(input: RoomInput)
    suspend fun updateRoom(token: String, name: String?, description: String?)
    suspend fun deleteRoom(token: String)
    suspend fun getAvatar(token: String): Bitmap?
}

class DefaultRoomRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : RoomRepository {
    override val request: RoomRequest
        get() = RoomRequest(authenticationDAO.getSelectedItem())
    override val avatarRequest: AvatarRequest
        get() = AvatarRequest(authenticationDAO.getSelectedItem())
    override var rooms: Flow<List<Room>> =
        request.getRooms()

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

    @Throws(Exception::class)
    override suspend fun getAvatar(token: String): Bitmap? {
        return avatarRequest.getAvatar(token)
    }
}