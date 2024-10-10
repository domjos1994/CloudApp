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
data class SerializedNote(
    @XmlAttribute val id: Long,
    @XmlAttribute val title: String,
    @XmlAttribute val category: String,
    @XmlAttribute val readonly: Boolean,
    @XmlAttribute val favorite: Boolean,
    @XmlAttribute val modified: Int,
    @XmlContent val content: String
)