package de.domjos.cloudapp.webrtc.model.user

import kotlinx.serialization.Serializable

@Serializable
data class User(val enabled: Boolean, val quota: Quota)

@Serializable
data class Quota(val free: Long, val used: Long, val total: Long, val relative: Double, val quota: Long)