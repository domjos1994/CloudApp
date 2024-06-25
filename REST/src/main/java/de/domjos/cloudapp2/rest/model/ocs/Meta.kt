/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.ocs

import kotlinx.serialization.Serializable

@Serializable
data class Meta(val status: String, val statuscode: Int, val message: String)