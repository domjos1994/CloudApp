/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.shares

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
data class Share(
    var id: Long,
    val share_type: Int,
    val share_with: String? = "",
    var displayname_owner: String,
    var permissions: Int,
    var can_edit: Boolean,
    var can_delete: Boolean,
    var path: String,
    var item_type: String,
    var file_target: String,
    var note: String
) {
    var url: String = ""
    override fun toString(): String {
        return file_target
    }
}

@Serializable
data class InsertShare(
    var path: String,
    var shareType: Int,
    var shareWith: String,
    var publicUpload: String,
    var password: String,
    var permissions: Int,
    var expireDate: String,
    var note: String
) {
    override fun toString(): String {
        return path
    }
}

@Serializable
data class UpdateShare(
    var permissions: Int,
    var password: String,
    var publicUpload: String,
    var expireDate: String,
    var note: String
)

enum class Types(val value: Int) {
    User(0),
    Group(1),
    Public(3),
    Federated(6),
    Circle(7),
    Talk(10);

    companion object {
        fun fromInt(value: Int): Types = try {
            entries.first { it.value == value }
        } catch (_: Exception) {
            User
        }
        fun toInt(value: Types): Int = try {
            entries.first { it == value }.value
        } catch (_: Exception) {
            0
        }

        fun fromString(value: String): Types = try {
            entries.first { it.name == value }
        } catch (_: Exception) {
            User
        }
    }
}

enum class Permissions(val value: Int) {
    Read(1),
    Update(2),
    Create(4),
    Delete(8),
    Share(16),
    All(31);
}
