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
data class SerializedNotifications(
    val items: Array<SerializedNotification>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedNotifications

        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedNotification(
    @XmlAttribute val notificationId: Long,
    @XmlAttribute val app: String,
    @XmlAttribute val icon: String,
    @XmlAttribute val user: String,
    @XmlAttribute val datetime: String,
    @XmlAttribute val subject: String,
    @XmlContent val message: String,
    @XmlAttribute val link: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedNotification

        if (notificationId != other.notificationId) return false
        if (app != other.app) return false
        if (user != other.user) return false
        if (datetime != other.datetime) return false
        if (subject != other.subject) return false
        if (message != other.message) return false
        if (link != other.link) return false

        return true
    }

    override fun hashCode(): Int {
        var result = notificationId.hashCode()
        result = 31 * result + app.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + datetime.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + link.hashCode()
        return result
    }
}

