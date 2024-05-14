package de.domjos.cloudapp2.rest.model.capabilities

import de.domjos.cloudapp2.rest.model.ocs.Meta
import kotlinx.serialization.Serializable

@Serializable
data class OCS(val meta: Meta, val data: Data)

@Serializable
data class OCSObject(val ocs: OCS)