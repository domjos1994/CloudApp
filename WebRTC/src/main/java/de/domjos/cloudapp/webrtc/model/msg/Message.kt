package de.domjos.cloudapp.webrtc.model.msg

import kotlinx.serialization.Serializable

@Serializable
data class Message(var id: Int, var token: String, var actorType: String, var actorId: String, var actorDisplayName: String, var timestamp: Long, var message: String) {
}