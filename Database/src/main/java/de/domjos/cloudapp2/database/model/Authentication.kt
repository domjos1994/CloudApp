package de.domjos.cloudapp2.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "authentications",
    indices = [
        Index(value = ["title"], orders = [Index.Order.ASC], name = "title_index", unique = true)
    ]
)
data class Authentication(@PrimaryKey(autoGenerate = true) var id: Long, var title: String, var url: String, var userName: String, var password: String, var selected: Boolean, var description: String?, var thumbNail: ByteArray?) {

    override fun toString(): String {
        return "$title($userName)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Authentication

        if (id != other.id) return false
        if (title != other.title) return false
        if (url != other.url) return false
        if (userName != other.userName) return false
        if (password != other.password) return false
        if(selected != other.selected) return false
        if (description != other.description) return false
        if (!thumbNail.contentEquals(other.thumbNail)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.toInt()
        result = 31 * result + title.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + thumbNail.contentHashCode()
        return result
    }
}