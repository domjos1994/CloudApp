package de.domjos.cloudapp2.rest.model.msg

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class Message(var id: Int, var token: String, var actorType: String, var actorId: String, var actorDisplayName: String, var timestamp: Long, var message: String) {
    var messageParameters: JsonElement? = null

    fun getParameterizedMessage(msg: String): String {
        return try {
            var content: String = msg
            messageParameters!!.jsonObject.forEach {
                content = content.replace("{${it.key}}", it.value.jsonObject["name"].toString(), true)
            }
            content
        } catch (_: Exception) {
            msg
        }
    }

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
        if (messageParameters != other.messageParameters) return false

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
        result = 31 * result + messageParameters.hashCode()
        return result
    }
}

@Serializable
data class InputMessage(var message: String)

@Serializable
data class Parameter(var type: String, var id: String, var name: String)

@Serializable
data class ParameterArray(var actor: Parameter?, var user: Parameter?)