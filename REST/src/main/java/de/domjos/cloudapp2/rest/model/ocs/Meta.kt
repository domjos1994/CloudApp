package de.domjos.cloudapp2.rest.model.ocs

import kotlinx.serialization.Serializable

@Serializable
data class Meta(val status: String, val statuscode: Int, val message: String)