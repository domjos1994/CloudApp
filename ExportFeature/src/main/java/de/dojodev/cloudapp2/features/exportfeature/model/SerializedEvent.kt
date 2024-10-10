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
data class SerializedEvent(
    @XmlAttribute val id: Long,
    @XmlAttribute val title: String,
    @XmlAttribute val from: String,
    @XmlAttribute val to: String,
    @XmlAttribute val categories: String,
    @XmlAttribute val confirmation: String,
    @XmlAttribute val calendar: String,
    @XmlAttribute val path: String,
    @XmlContent val description: String
)