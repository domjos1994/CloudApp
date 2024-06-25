/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.capabilities.Data
import de.domjos.cloudapp2.rest.model.ocs.Meta
import de.domjos.cloudapp2.rest.model.user.Quota
import de.domjos.cloudapp2.rest.model.user.OCSObject as UOCSObject
import de.domjos.cloudapp2.rest.model.capabilities.OCSObject as COCSObject
import de.domjos.cloudapp2.rest.model.user.User
import kotlinx.serialization.Serializable

/**
 * Class to get user-data
 * @see de.domjos.cloudapp2.rest.model.user.User
 */
class UserRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v1.php/cloud/") {

    /**
     * Class to check right connection data
     * @param user the username
     * @return the user if user exists
     */
    fun checkConnection(user: String = authentication?.userName?:""): User? {

        // build request
        val request = super.buildRequest("users/${user}?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {

                // string to object
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<UOCSObject>(content)

                // check status
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                } else {
                    if(ocs.ocs.meta.message != "") {
                        throw Exception(ocs.ocs.meta.message)
                    }
                }
            } catch (ex: Exception) {
                return null
            }
            return null
        }
    }

    /**
     * Get the capabilities by the users
     * @return the capabilities
     */
    @Throws(Exception::class)
    fun getCapabilities(): Data? {

        // build request
        val request = super.buildRequest("capabilities?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {

                // string to object
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<COCSObject>(content)

                // check status
                if(ocs.ocs.meta.statuscode==100) {
                    return ocs.ocs.data
                } else {
                    if(ocs.ocs.meta.message != "") {
                        throw Exception(ocs.ocs.meta.message)
                    }
                }
            } catch (ex: Exception) {
                throw ex
            }
            return null
        }
    }

    /**
     * Get a list of users
     * @return the list of users
     */
    @Throws(Exception::class)
    fun getUsers(): List<User?> {

        // build request
        val request = super.buildRequest("users?format=json", "get", null)

        client.newCall(request!!).execute().use { response ->
            try {

                // string to object
                val content = response.body!!.string()
                val ocs =  super.json.decodeFromString<OCSObject>(content)

                // check statue
                if(ocs.ocs.meta.statuscode==100) {

                    // return list
                    val lst = mutableListOf<User?>()
                    ocs.ocs.data.users.forEach {
                        val user = User(Quota(0,0,0,0.0, 0), it, it)
                        lst.add(user)
                    }
                    return lst
                }  else {
                    if(ocs.ocs.meta.message != "") {
                        throw Exception(ocs.ocs.meta.message)
                    }
                }
            } catch (ex: Exception) {
                throw ex
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