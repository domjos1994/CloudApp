package de.domjos.cloudapp.webrtc.model.room

import de.domjos.cloudapp.webrtc.model.msg.Message
import kotlinx.serialization.Serializable

@Serializable
data class Room(
    var id: Long,
    var token: String,
    var type: Int,
    var name: String?,
    var displayName: String,
    var description: String?,
    var readonly: Int,
    var unreadMessages: Int,
    var avatarVersion: String?,
    var lastMessage: Message)  {
}

@Serializable
data class RoomInput(var roomType: Int, var invite: String, var source: String?, var roomName: String?, var objectType: String?, var objectId: String)

enum class Type(val value: Int) {
    OneToOne(1),
    Group(2),
    Public(3),
    Changelog(4),
    FormerOneToOne(5),
    NoteToSelf(6);

    companion object {
        fun fromInt(value: Int) = entries.first { it.value == value }
    }
}

