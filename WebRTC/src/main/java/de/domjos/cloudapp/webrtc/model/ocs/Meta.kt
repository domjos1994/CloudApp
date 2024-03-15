package de.domjos.cloudapp.webrtc.model.ocs

import kotlinx.serialization.Serializable

@Serializable
data class Meta(val status: String, val statuscode: Int, val message: String)