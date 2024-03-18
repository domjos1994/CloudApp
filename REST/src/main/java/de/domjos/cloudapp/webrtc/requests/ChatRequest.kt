package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.msg.Message
import de.domjos.cloudapp.webrtc.model.msg.OCSObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v1/") {

    @Throws(Exception::class)
    fun getChats(lookIntoFuture: Int = 0, token: String): Flow<List<Message>> {
        val request = super.buildRequest("chat/$token?lookIntoFuture=$lookIntoFuture", "get", null)

        return flow {
            while(true) {
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
                delay(20000L)
            }
        }
    }
}