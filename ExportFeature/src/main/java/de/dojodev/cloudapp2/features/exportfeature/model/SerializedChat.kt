/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.model

import com.ryanharter.kotlinx.serialization.xml.XmlAttribute
import com.ryanharter.kotlinx.serialization.xml.XmlContent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedChat(
    @XmlAttribute val id: Long,
    @XmlAttribute val token: String,
    @XmlAttribute val timestamp: Long,
    @XmlAttribute val actor: String,
    @XmlAttribute val roomName: String,
    @XmlAttribute val roomType: Int,
    @XmlAttribute val roomDescription: String,
    @XmlContent val message: String
)
