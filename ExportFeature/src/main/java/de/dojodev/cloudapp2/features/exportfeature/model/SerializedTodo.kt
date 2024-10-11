/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.model

import com.ryanharter.kotlinx.serialization.xml.XmlAttribute
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
data class SerializedTodos(
    val items: Array<SerializedTodo>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedTodos

        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedTodo(
    @XmlAttribute val id: Long,
    @XmlAttribute val summary: String,
    @XmlAttribute val categories: String,
    @XmlAttribute val start: String,
    @XmlAttribute val end: String,
    @XmlAttribute val status: String,
    @XmlAttribute val completed: Int,
    @XmlAttribute val priority: Int,
    @XmlAttribute val list: String
)