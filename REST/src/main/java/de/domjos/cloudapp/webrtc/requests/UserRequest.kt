package de.domjos.cloudapp.webrtc.requests

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.user.OCSObject
import de.domjos.cloudapp.webrtc.model.user.User

class UserRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v1.php/cloud/users/") {

    fun checkConnection(): User? {
        val request = super.buildRequest("${authentication?.userName?:""}?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<OCSObject>(content)
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                }
            } catch (_: Exception) {}
            return null
        }
    }
}