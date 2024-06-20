package de.domjos.cloudapp2.rest.model.shares

import kotlinx.serialization.Serializable

@Serializable
data class Share(
    var id: Long,
    var displayname_owner: String,
    var permissions: Int,
    var can_edit: Boolean,
    var can_delete: Boolean,
    var path: String,
    var item_type: String,
    var file_target: String
) {
    override fun toString(): String {
        return file_target
    }
}
