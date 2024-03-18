package de.domjos.cloudapp.webrtc.model.notifications

import kotlinx.serialization.Serializable

@Serializable
data class Action(val label: String, val link: String, val type: String, val primary: Boolean) {
}