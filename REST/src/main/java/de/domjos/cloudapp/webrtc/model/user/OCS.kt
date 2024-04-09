package de.domjos.cloudapp.webrtc.model.user

import de.domjos.cloudapp.webrtc.model.ocs.Meta
import kotlinx.serialization.Serializable

@Serializable
data class OCS(val meta: Meta, val data: User) {}

@Serializable
data class OCSObject(val ocs: OCS)