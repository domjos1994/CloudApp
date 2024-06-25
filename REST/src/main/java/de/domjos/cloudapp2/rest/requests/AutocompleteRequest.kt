/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.ocs.Meta
import de.domjos.cloudapp2.rest.model.shares.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

/**
 * Takes Types and a text and autocompletes it by REST-Server-Data
 * @author Dominic Joas
 */
class AutocompleteRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/core/autocomplete/") {

    /**
     * This function takes an type and a text and autocompletes the data
     * by returning a list of possible items
     * @param type The Type-Enum
     * @param text The text
     * @return a flow with a list of items
     */
    @Throws(Exception::class)
    fun getItem(type: Types, text: String): Flow<List<String>> {

        // create request
        val request = super.buildRequest("get?search=$text&shareTypes[]=${type.value}&format=json", "get", null)

        // create flow with the returning data
        return flow {
            if(request!=null) {
                client.newCall(request).execute().use { response ->
                    val content = response.body!!.string()
                    val ocsObject =  super.json.decodeFromString<OCSObject>(content)
                    if(ocsObject.ocs.meta.statuscode == 200) {
                        val items = mutableListOf<String>()
                        ocsObject.ocs.data.forEach { item ->
                            items.add(item.label)
                        }
                        emit(items)
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
     * This is the model of the JSON-API
     * Autocomplete is only a List of Strings
     */
    @Serializable
    private data class Item(val label: String)

    /**
     * Data and Metadata are always there in the OCS-API
     */
    @Serializable
    private data class OCS(val meta: Meta, val data: Array<Item>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OCS

            if (meta != other.meta) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = meta.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    /**
     * This is the Main-Return-Object of the OCS-API
     */
    @Serializable
    private data class OCSObject(val ocs: OCS)
}