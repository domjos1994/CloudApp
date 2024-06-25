/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.capabilities

import de.domjos.cloudapp2.rest.model.ocs.Meta
import kotlinx.serialization.Serializable

@Serializable
data class OCS(val meta: Meta, val data: Data)

@Serializable
data class OCSObject(val ocs: OCS)