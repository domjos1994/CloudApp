/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.notes

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    var id: Int, var content: String, var title: String,
    var category: String, var favorite: Boolean, var modified: Int) {
    var readonly: Boolean = false
}