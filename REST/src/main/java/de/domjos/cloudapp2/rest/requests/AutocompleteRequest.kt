package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.ocs.Meta
import de.domjos.cloudapp2.rest.model.shares.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class AutocompleteRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/core/autocomplete/") {

    @Throws(Exception::class)
    fun getItem(type: Types, text: String): Flow<List<String>> {
        val request = super.buildRequest("get?search=$text&shareTypes[]=${type.value}&format=json", "get", null)

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

    @Serializable
    private data class Item(val label: String)

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

    @Serializable
    private data class OCSObject(val ocs: OCS)
}