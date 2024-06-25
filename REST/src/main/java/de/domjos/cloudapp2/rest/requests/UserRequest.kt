package de.domjos.cloudapp2.rest.requests

import android.util.Log
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.capabilities.Data
import de.domjos.cloudapp2.rest.model.ocs.Meta
import de.domjos.cloudapp2.rest.model.user.Quota
import de.domjos.cloudapp2.rest.model.user.OCSObject as UOCSObject
import de.domjos.cloudapp2.rest.model.capabilities.OCSObject as COCSObject
import de.domjos.cloudapp2.rest.model.user.User
import kotlinx.serialization.Serializable

class UserRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v1.php/cloud/") {

    fun checkConnection(user: String = authentication?.userName?:""): User? {
        val request = super.buildRequest("users/${user}?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<UOCSObject>(content)
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
            }
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

    fun getUsers(): List<User?> {
        val request = super.buildRequest("users?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<OCSObject>(content)
                if(ocs.ocs.meta.statuscode==100) {
                    val lst = mutableListOf<User?>()
                    ocs.ocs.data.users.forEach {
                        val user = User(Quota(0,0,0,0.0, 0), it, it)
                        lst.add(user)
                    }
                    return lst
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
            }
            return listOf()
        }
    }
}

@Serializable
data class Data(val users: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as de.domjos.cloudapp2.rest.requests.Data

        return users.contentEquals(other.users)
    }

    override fun hashCode(): Int {
        return users.contentHashCode()
    }
}

@Serializable
data class Users(val meta: Meta, val data: de.domjos.cloudapp2.rest.requests.Data)

@Serializable
data class OCSObject(val ocs: Users)