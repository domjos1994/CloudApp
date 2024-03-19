package de.domjos.cloudapp.webrtc.model.msg

import kotlinx.serialization.Serializable

@Serializable
data class Message(var id: Int, var token: String, var actorType: String, var actorId: String, var actorDisplayName: String, var timestamp: Long, var message: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (token != other.token) return false
        if (actorType != other.actorType) return false
        if (actorId != other.actorId) return false
        if (actorDisplayName != other.actorDisplayName) return false
        if (timestamp != other.timestamp) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + token.hashCode()
        result = 31 * result + actorType.hashCode()
        result = 31 * result + actorId.hashCode()
        result = 31 * result + actorDisplayName.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }
}

@Serializable
data class InputMessage(var message: String)