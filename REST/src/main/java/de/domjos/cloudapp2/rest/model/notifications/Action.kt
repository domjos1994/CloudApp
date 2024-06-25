/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.model.notifications

import kotlinx.serialization.Serializable

@Serializable
data class Action(val label: String, val link: String, val type: String, val primary: Boolean) {
}