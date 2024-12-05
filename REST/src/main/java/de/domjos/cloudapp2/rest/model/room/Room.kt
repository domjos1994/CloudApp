/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.room

import android.graphics.Bitmap
import de.domjos.cloudapp2.rest.model.msg.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Room(
    var id: Long,
    var token: String,
    var type: Int,
    var invite: String = "",
    var name: String?,
    var displayName: String?,
    var description: String?,
    var readOnly: Int,
    var unreadMessages: Int,
    var avatarVersion: String?,
    var lastMessage: Message,
    @Transient var icon: Bitmap? = null)  {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (id != other.id) return false
        if (token != other.token) return false
        if (type != other.type) return false
        if (invite != other.invite) return false
        if (name != other.name) return false
        if (displayName != other.displayName) return false
        if (description != other.description) return false
        if (readOnly != other.readOnly) return false
        if (unreadMessages != other.unreadMessages) return false
        if (avatarVersion != other.avatarVersion) return false
        if (lastMessage != other.lastMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + type
        result = 31 * result + invite.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + readOnly
        result = 31 * result + unreadMessages
        result = 31 * result + (avatarVersion?.hashCode() ?: 0)
        result = 31 * result + lastMessage.hashCode()
        return result
    }
}

@Serializable
data class RoomInput(
    var roomType: Int,
    var invite: String,
    val source: String = "",
    var roomName: String?,
    val objectType: String = "room",
    val objectId: String = ""
)

enum class Type(val value: Int) {
    OneToOne(1),
    Group(2),
    Public(3),
    Changelog(4),
    FormerOneToOne(5),
    NoteToSelf(6);

    companion object {
        fun fromInt(value: Int, default: Type) = entries.find { it.value == value } ?: default
    }
}

