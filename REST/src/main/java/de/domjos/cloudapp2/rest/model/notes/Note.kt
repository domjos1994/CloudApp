package de.domjos.cloudapp2.rest.model.notes

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    var id: Int, var content: String, var title: String,
    var category: String, var favorite: Boolean, var modified: Int) {



    var etag: String = ""
    var readonly: Boolean = false
}