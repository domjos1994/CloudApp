package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.shares.Share
import de.domjos.cloudapp2.rest.model.shares.OCSObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ShareRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/files_sharing/api/v1/") {

    @Throws(Exception::class)
    fun getShares(): Flow<List<Share>> {
        val request = super.buildRequest("shares?format=json&shared_with_me=true", "GET", null)

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
}