/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.user

import kotlinx.serialization.Serializable

@Serializable
data class User(val quota: Quota, val id: String = "", val displayname: String = "")

@Serializable
data class Quota(val free: Long, val used: Long, val total: Long, val relative: Double, val quota: Long)