package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.msg.InputMessage
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.msg.OCSObject
import kotlinx.serialization.encodeToString

/**
 * Request to Chat with a User by Room-Token
 * @param authentication the Authentication
 * @author Dominic Joas
 */
class ChatRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v1/") {

    /**
     * Gets a list of chats
     * @param lookIntoFuture 1 Poll and wait for new message or 0 get history of a conversation
     * @return List of chats
     */
    @Throws(Exception::class)
    fun getChats(lookIntoFuture: Int = 0, token: String): List<Message> {

        // build request
        val request = super.buildRequest("chat/$token?lookIntoFuture=$lookIntoFuture&format=json", "get", null)

        client.newCall(request!!).execute().use { response ->

            // string to object
            val content = response.body!!.string()
            val ocs =  super.json.decodeFromString<OCSObject>(content)

            // check status
            if(ocs.ocs.meta.statuscode==200) {
                return ocs.ocs.data.toList()
            } else {
                throw Exception(ocs.ocs.meta.message)
            }
        }
    }

    /**
     * Write a new chat message
     * @param token Token of the Room
     * @param message message
     * @return List of Messages
     */
    @Throws(Exception::class)
    fun insertChats(token: String, message: String): List<Message> {

        // build request
        val inputMessage = InputMessage(message)
        val request = super.buildRequest("chat/$token?format=json", "post", super.json.encodeToString(inputMessage))

        client.newCall(request!!).execute().use { response ->

            // check status
            if(response.code == 200) {
                return getChats(1, token)
            }
            return listOf()
        }
    }
}