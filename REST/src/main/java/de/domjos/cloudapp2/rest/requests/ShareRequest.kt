/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

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

/**
 * Class to getting your own shares and getting files that are shared for you.
 * Creating, updating and deleting shares
 * @see de.domjos.cloudapp2.rest.model.shares.Share
 * @author Dominic Joas
 */
class ShareRequest(private val authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/files_sharing/api/v1/") {

    /**
     * Getting a list of shares
     * @param sharedWithMe if true show files that are shared with you
     * if false get your own shares
     * @return flow with a list of shares
     */
    @Throws(Exception::class)
    fun getShares(sharedWithMe: Boolean): Flow<List<Share>> {

        // build request
        val request = super.buildRequest("shares?format=json${if(sharedWithMe) "&shared_with_me=true" else ""}", "GET", null)

        return flow {
            if(request!=null) {
                client.newCall(request).execute().use { response ->

                    // string to object
                    val content = response.body!!.string()
                    val ocsObject =  super.json.decodeFromString<OCSObject>(content)

                    // check status
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

    /**
     * Add a new share
     * @param share the new share to add
     * @see de.domjos.cloudapp2.rest.model.shares.InsertShare
     * @return the flow with the new share
     */
    @Throws(Exception::class)
    fun addShare(share: InsertShare): Flow<Share?> {

        // edit path and build request
        share.path = share.path.split(authentication?.userName ?: "")[1]
        val content = super.json.encodeToString<InsertShare>(share)
        val request = super.buildRequest("shares?format=json", "POST", content)

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->

                    // check status
                    if(response.code == 200) {

                        // create object with data
                        val res = response.body!!.string()
                        val ocsObject = super.json.decodeFromString<OCSObject2>(res)
                        emit(ocsObject.ocs.data)
                    } else {

                        // create object with message
                        val res = response.body!!.string()
                        val ocsObject = super.json.decodeFromString<OCSObject>(res)
                        throw Exception(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit(null)
            }
        }
    }

    /**
     * Delete an existing share
     * @param id the id of the share
     * @return flow with message if something is wrong
     */
    @Throws(Exception::class)
    fun deleteShare(id: Int): Flow<String> {

        // create request
        val request = super.buildRequest("shares/${id}?format=json", "DELETE", null)

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->

                    // check status
                    if(response.code == 200) {
                        emit("")
                    } else {

                        // strong to object
                        val ocsObject = super.json.decodeFromString<OCSObject>(response.body!!.string())
                        emit(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit("")
            }
        }
    }

    /**
     * Update an existing share
     * @see de.domjos.cloudapp2.rest.model.shares.UpdateShare
     * @param id the id of the share
     * @param updateShare the updated data
     * @return flow with updated share
     */
    @Throws(Exception::class)
    fun updateShare(id: Int, updateShare: UpdateShare): Flow<Share?> {

        // create request
        val request = super.buildRequest("shares/${id}?format=json", "PUT", super.json.encodeToString<UpdateShare>(updateShare))

        return flow {
            if(request != null) {
                client.newCall(request).execute().use { response ->

                    // check status
                    if(response.code == 200) {

                        // string to object
                        val res = response.body!!.string()
                        val ocsObject = super.json.decodeFromString<OCSObject2>(res)
                        emit(ocsObject.ocs.data)
                    } else {

                        // string to message
                        val ocsObject = super.json.decodeFromString<OCSObject>(response.body!!.string())
                        throw Exception(ocsObject.ocs.meta.message)
                    }
                }
            } else {
                emit(null)
            }
        }
    }
}