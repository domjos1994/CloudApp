package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import de.domjos.cloudapp.webrtc.model.notifications.OCSObject

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NotificationRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/notifications/api/v2/")  {

    @Throws(Exception::class)
    fun getNotifications(): Flow<List<Notification>> {
        val request = super.buildRequest("notifications?format=json", "get", null)



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
}