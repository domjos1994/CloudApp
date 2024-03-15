package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.room.Room
import de.domjos.cloudapp.webrtc.requests.RoomRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

interface RoomRepository {
    val rooms: Flow<List<Room>>

}

class DefaultRoomRepository @Inject constructor(
    authenticationDAO: AuthenticationDAO
) : RoomRepository {
    override var rooms: Flow<List<Room>> =
        RoomRequest(authenticationDAO.getSelectedItem()).getRooms()
}