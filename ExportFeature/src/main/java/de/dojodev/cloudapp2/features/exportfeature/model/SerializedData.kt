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
data class SerializedData(
    @XmlAttribute val name: String,
    @XmlAttribute val directory: Boolean,
    @XmlAttribute val exists: Boolean,
    @XmlAttribute val type: String,
    @XmlAttribute val share: Share,
    @XmlContent val path: String
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Share(
    @XmlAttribute val shareWithMe: String,
    @XmlAttribute val shareByMe: String
)