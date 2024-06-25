package de.domjos.cloudapp2.rest.model.notifications

import kotlinx.serialization.Serializable

@Serializable
data class Action(val label: String, val link: String, val type: String, val primary: Boolean) {
}