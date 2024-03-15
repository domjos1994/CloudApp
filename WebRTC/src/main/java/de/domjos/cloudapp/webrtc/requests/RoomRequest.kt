package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.room.OCS
import de.domjos.cloudapp.webrtc.model.room.OCSObject
import de.domjos.cloudapp.webrtc.model.room.Room
import de.domjos.cloudapp.webrtc.model.room.RoomInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import okhttp3.Callback


class RoomRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v4/") {



    @Throws(Exception::class)
    fun getRooms(): Flow<List<Room>> {
        val request = super.buildRequest("room?format=json", "get", null)



        return flow {
            if(request!=null) {
                client.newCall(request).execute().use { response ->
                    val content = response.body!!.string()
                    val ocs =  super.json.decodeFromString<OCSObject>(content)
                    if(ocs.ocs.meta.statuscode==200) {
                        emit(ocs.ocs.data.toList())
                    } else {
                        throw Exception(ocs.ocs.meta.message)
                    }
                }
            } else {
                emit(listOf())
            }
        }
    }

    @Throws(Exception::class)
    fun putRoom(input: RoomInput) {
        val request = super.buildRequest("room", "post", super.json.encodeToString(input))

        client.newCall(request!!).execute().use { response ->
            val content = response.code
            if(content != 200) {
                throw Exception(response.message)
            }
        }
    }
}