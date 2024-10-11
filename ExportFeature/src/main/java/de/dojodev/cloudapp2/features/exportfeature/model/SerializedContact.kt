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
data class SerializedContacts(
    val items: Array<SerializedContact>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedContacts

        return items.contentEquals(other.items)
    }

    override fun hashCode(): Int {
        return items.contentHashCode()
    }
}

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedContact(
    @XmlAttribute val id: Long,
    @XmlAttribute val prefix: String,
    @XmlAttribute val suffix: String,
    @XmlAttribute val givenName: String,
    @XmlAttribute val familyName: String,
    @XmlAttribute val organization: String,
    @XmlAttribute val additional: String,
    @XmlAttribute val birthDay: String,
    @XmlAttribute val categories: String,
    @XmlAttribute val addressBook: String,
    val addresses: List<SerializedAddress>,
    val emailAddresses: List<SerializedEmailAddress>,
    val phoneNumbers: List<SerializedPhoneNumber>
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedAddress(
    @XmlAttribute val street: String,
    @XmlAttribute val postalCode: String,
    @XmlAttribute val locality: String,
    @XmlAttribute val country: String,
    @XmlAttribute val additional: String,
    @XmlAttribute val types: List<String>
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedEmailAddress(
    @XmlAttribute val value: String
)

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class SerializedPhoneNumber(
    @XmlAttribute val value: String,
    @XmlAttribute val types: List<String>
)