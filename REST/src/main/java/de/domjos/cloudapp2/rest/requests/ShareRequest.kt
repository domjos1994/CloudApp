package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.shares.InsertShare
import de.domjos.cloudapp2.rest.model.shares.Share
import de.domjos.cloudapp2.rest.model.shares.OCSObject
import de.domjos.cloudapp2.rest.model.shares.OCSObject2
import de.domjos.cloudapp2.rest.model.shares.UpdateShare
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString

class ShareRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/files_sharing/api/v1/") {

    @Throws(Exception::class)
    fun getShares(own: Boolean): Flow<List<Share>> {
        val request = super.buildRequest("shares?format=json${if(own) "&shared_with_me=true" else ""}", "GET", null)

        return flow {
            if(request!=null) {
                client.newCall(request).execute().use { response ->
                    val content = response.body!!.string()
                    val ocsObject =  super.json.decodeFromString<OCSObject>(content)
                    if(ocsObject.ocs.meta.statuscode == 200) {
                        emit(ocsObject.ocs.data.toList())
                    } else {
                        emit(listOf())
                    }
                }
            } else {
                emit(listOf())
            }
        }
    }

    @Throws(Exception::class)
    fun addShare(share: InsertShare): Flow<String> {
        share.path = share.path.split(authentication?.userName ?: "")[1]
        val content = super.json.encodeToString<InsertShare>(share)
        val request = super.buildRequest("shares?format=json", "POST", content)

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->
                    if(response.code == 200) {
                        val res = response.body!!.string()
                        val ocsObject = super.json.decodeFromString<OCSObject2>(res)
                        emit(ocsObject.ocs.data.url)
                    } else {
                        val res = response.body!!.string()
                        val ocsObject = super.json.decodeFromString<OCSObject>(res)
                        emit(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit("")
            }
        }
    }

    @Throws(Exception::class)
    fun deleteShare(id: Int): Flow<String> {
        val request = super.buildRequest("shares/${id}?format=json", "DELETE", null)

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->
                    if(response.code == 200) {
                        emit("")
                    } else {
                        val ocsObject = super.json.decodeFromString<OCSObject>(response.body!!.toString())
                        emit(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit("")
            }
        }
    }

    @Throws(Exception::class)
    fun updateShare(id: Int, updateShare: UpdateShare): Flow<String> {
        val request = super.buildRequest("shares/${id}?format=json", "PUT", super.json.encodeToString<UpdateShare>(updateShare))

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->
                    if(response.code == 200) {
                        emit("")
                    } else {
                        val ocsObject = super.json.decodeFromString<OCSObject>(response.body!!.toString())
                        emit(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit("")
            }
        }
    }
}