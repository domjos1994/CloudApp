package de.domjos.cloudapp.webrtc.model.capabilities

import de.domjos.cloudapp.webrtc.model.ocs.Meta
import kotlinx.serialization.Serializable

@Serializable
data class OCS(val meta: Meta, val data: Data)

@Serializable
data class OCSObject(val ocs: OCS)