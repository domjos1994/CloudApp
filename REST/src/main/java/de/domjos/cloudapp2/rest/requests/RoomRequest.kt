package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.room.OCSObject
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.rest.model.room.RoomInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString

/**
 * Class to getting, adding, updating and deleting rooms
 * @param authentication The Authentication-Item
 * @author Dominic Joas
 */
class RoomRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v4/") {

    /**
     * Returns a Flow with a list of Rooms
     * @return Flow with list of Rooms
     */
    @Throws(Exception::class)
    fun getRooms(): Flow<List<Room>> {

        // send request
        val request = super.buildRequest("room?format=json", "get", null)

        return flow {
            try {
                if(request!=null) {
                    client.newCall(request).execute().use { response ->
                        try {

                            // body to object
                            val content = response.body!!.string()
                            val ocs =  super.json.decodeFromString<OCSObject>(content)

                            // check status
                            if(ocs.ocs.meta.statuscode==200) {

                                // get avatar
                                val req = AvatarRequest(authentication)
                                val lst = ocs.ocs.data.toList()
                                for(i in 0..<lst.count()) {
                                    lst[i].icon = req.getAvatar(lst[i].token)
                                }
                                emit(ocs.ocs.data.toList())

                            } else {
                                throw Exception(ocs.ocs.meta.message)
                            }
                        } catch (ex:Exception) {
                            throw ex
                        }
                    }
                } else {
                    emit(listOf())
                }
            } catch (ex:Exception) {
                throw ex
            }
        }
    }

    /**
     * Adds a new Room
     * @param input Room-Item
     */
    @Throws(Exception::class)
    fun addRoom(input: RoomInput) {

        // send request
        val request = super.buildRequest("room?format=json", "post", super.json.encodeToString(input))

        if(request != null) {
            client.newCall(request).execute().use { response ->

                // body to object
                val result = super.json.decodeFromString<JSONResult>(response.body?.string()!!)

                // check status
                if(result.ocs.meta.statuscode != 201) {
                    throw Exception(result.ocs.meta.message)
                }
            }
        } else {
            throw Exception("No Request")
        }
    }

    /**
     * Rename a Room
     * @param token Token of Room
     * @param name new Name
     */
    @Throws(Exception::class)
    fun renameRoom(token: String, name: String) {

        // send body
        val body = "{\"roomName\": \"$name\"}"
        val request = super.buildRequest("room/$token?format=json", "put", body)

        if(request != null) {
            client.newCall(request).execute().use { response ->

                // body to object
                val content = super.json.decodeFromString<JSONResult>(response.body?.string()!!)

                // check status
                if(content.ocs.meta.statuscode != 200) {
                    throw Exception(content.ocs.meta.message)
                }
            }
        } else {
            throw Exception("Something went wrong!")
        }
    }

    /**
     * Update description of a Room
     * @param token Token of Room
     * @param description new Description
     */
    @Throws(Exception::class)
    fun updateDescription(token: String, description: String) {

        // send body
        val body = "{\"description\": \"$description\"}"
        val request = super.buildRequest("room/$token/description?format=json", "put", body)

        if(request != null) {
            client.newCall(request).execute().use { response ->

                // body to object
                val content = super.json.decodeFromString<JSONResult>(response.body?.string()!!)

                // check status
                if(content.ocs.meta.statuscode != 200) {
                    throw Exception(content.ocs.meta.message)
                }
            }
        } else {
            throw Exception("Something went wrong!")
        }
    }

    /**
     * Delete Room
     * @param token Token of Room
     */
    @Throws(Exception::class)
    fun deleteRoom(token: String) {

        // send request
        val request = super.buildRequest("room/$token?format=json", "delete", null)

        if(request != null) {
            client.newCall(request).execute().use { response ->

                // body to object
                val content = super.json.decodeFromString<JSONResult>(response.body?.string()!!)

                // check status
                if(content.ocs.meta.statuscode != 200) {
                    throw Exception(content.ocs.meta.message)
                }
            }
        } else {
            throw Exception("Something went wrong!")
        }
    }
}