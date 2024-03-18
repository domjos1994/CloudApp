package de.domjos.cloudapp.webrtc.model.msg

import de.domjos.cloudapp.webrtc.model.ocs.Meta
import de.domjos.cloudapp.webrtc.model.msg.Message
import kotlinx.serialization.Serializable

@Serializable
class OCS(val meta: Meta, val data: Array<Message>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as OCS

        if (meta != other.meta) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + meta.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

@Serializable
data class OCSObject(val ocs: OCS)