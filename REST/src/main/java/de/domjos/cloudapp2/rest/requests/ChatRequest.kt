package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.msg.InputMessage
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.msg.OCSObject
import kotlinx.serialization.encodeToString

class ChatRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v1/") {

    @Throws(Exception::class)
    fun getChats(lookIntoFuture: Int = 0, token: String): List<Message> {
        val request = super.buildRequest("chat/$token?lookIntoFuture=$lookIntoFuture&format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            val content = response.body!!.string()
            val ocs =  super.json.decodeFromString<OCSObject>(content)
            if(ocs.ocs.meta.statuscode==200) {
                return ocs.ocs.data.toList()
            } else {
                throw Exception(ocs.ocs.meta.message)
            }
        }
    }

    @Throws(Exception::class)
    fun insertChats(token: String, message: String): List<Message> {
        val inputMessage = InputMessage(message)
        val request = super.buildRequest("chat/$token?format=json", "post", super.json.encodeToString(inputMessage))

        client.newCall(request!!).execute().use { response ->
            if(response.code == 200) {
                return getChats(1, token)
            }
            return listOf()
        }
    }
}