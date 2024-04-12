package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.capabilities.Data
import de.domjos.cloudapp.webrtc.model.user.OCSObject as UOCSObject
import de.domjos.cloudapp.webrtc.model.capabilities.OCSObject as COCSObject
import de.domjos.cloudapp.webrtc.model.user.User

class UserRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v1.php/cloud/") {

    fun checkConnection(): User? {
        val request = super.buildRequest("users/${authentication?.userName?:""}?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<UOCSObject>(content)
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                }
            } catch (_: Exception) {}
            return null
        }
    }

    fun getCapabilities(): Data? {
        val request = super.buildRequest("capabilities?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<COCSObject>(content)
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                }
            } catch (ex: Exception) {
                println(ex.message)
            }
            return null
        }
    }
}