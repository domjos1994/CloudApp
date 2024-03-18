package de.domjos.cloudapp.webrtc.model.notifications

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date

@Serializable
data class Notification(
    val notification_id: Long,
    val app: String,
    val user: String,
    val datetime: String,
    val object_type: String,
    val object_id: String,
    val subject: String,
    val message: String,
    val link: String,
    val icon: String,
    val shouldNotify: Boolean,
    val actions: Array<Action>) {

    @SuppressLint("SimpleDateFormat")
    fun getDate(): Date? {
        val format = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss")
        return format.parse(datetime)
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notification

        if (notification_id != other.notification_id) return false
        if (app != other.app) return false
        if (user != other.user) return false
        if (datetime != other.datetime) return false
        if (object_type != other.object_type) return false
        if (object_id != other.object_id) return false
        if (subject != other.subject) return false
        if (message != other.message) return false
        if (link != other.link) return false
        if (icon != other.icon) return false
        if (shouldNotify != other.shouldNotify) return false
        if (!actions.contentEquals(other.actions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = notification_id.hashCode()
        result = 31 * result + app.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + datetime.hashCode()
        result = 31 * result + object_type.hashCode()
        result = 31 * result + object_id.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + link.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + shouldNotify.hashCode()
        result = 31 * result + actions.contentHashCode()
        return result
    }

}