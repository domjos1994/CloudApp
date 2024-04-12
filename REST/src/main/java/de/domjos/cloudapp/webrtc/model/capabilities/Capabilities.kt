package de.domjos.cloudapp.webrtc.model.capabilities

import kotlinx.serialization.Serializable

@Serializable
data class Data(val version: Version, val capabilities: Capabilities)

@Serializable
data class Capabilities(val notifications: Notifications?, val spreed: Spreed?, val theming: Theming)

@Serializable
data class Version(val major: Int, val minor: Int, val micro: Int, val string: String, val edition: String, val extendedSupport: Boolean)

@Serializable
data class Notifications(val `ocs-endpoints`: Array<String>, val push: Array<String>, val `admin-notifications`: Array<String>?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notifications

        if (!`ocs-endpoints`.contentEquals(other.`ocs-endpoints`)) return false
        if (!push.contentEquals(other.push)) return false
        if (!`admin-notifications`.contentEquals(other.`admin-notifications`)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = `ocs-endpoints`.contentHashCode()
        result = 31 * result + push.contentHashCode()
        result = 31 * result + `admin-notifications`.contentHashCode()
        return result
    }

}

@Serializable
data class Spreed(val features: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Spreed

        return features.contentEquals(other.features)
    }

    override fun hashCode(): Int {
        return features.contentHashCode()
    }
}

@Serializable
data class Theming(
    val name: String,
    var url: String,
    val slogan: String,
    val color: String,
    val `color-text`: String,
    val `color-element`: String,
    val `color-element-bright`: String,
    val `color-element-dark`: String,
    val logo: String,
    val background: String)
