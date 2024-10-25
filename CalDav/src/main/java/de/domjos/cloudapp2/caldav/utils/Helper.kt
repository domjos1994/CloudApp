package de.domjos.cloudapp2.caldav.utils

import com.thegrizzlylabs.sardineandroid.DavResource
import de.domjos.cloudapp2.database.model.Authentication
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import okhttp3.Headers
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
class Helper {

    companion object {
        private val formats = listOf("yyyyMMdd'T'HHmmss'Z'", "yyyyMMdd'T'HHmmss", "yyyyMMdd'T'", "yyyyMMdd")

        fun isDeleted(resource: DavResource): Boolean {
            try {
                val items = resource.customProps.filter { it.key == "deleted-at" }
                if (items.isEmpty()) {
                    return false
                }
                return (items["deleted-at"] ?: "").isNotEmpty()
            } catch (_: Exception) {
                return false
            }
        }

        fun getString(date: Date?): String {
            try {
                if(date == null) {
                    return ""
                }

                return format(formats, date)
            } catch (_: Exception) {
                return ""
            }
        }

        fun getDate(dtString: String?): Date? {
            try {
                if(dtString == null) {
                    return null
                }
                if(dtString.isEmpty()) {
                    return null
                }

                return format(formats, dtString)
            } catch (_: Exception) {
                return null
            }
        }

        inline fun <reified T: Property> readPropertyToString(component: Component): String {
            try {
                val name = T::class.simpleName?.uppercase()
                return (component.getProperty<T>(name).value.toString())
            } catch (_: Exception) {}
            return ""
        }

        fun buildHeaders(authentication: Authentication): Headers {
            val builder = Headers.Builder()
            builder.add("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(
                    "${authentication.userName}:${authentication.password}".toByteArray()
                )
            )
            return builder.build()
        }

        fun getData(calendar: Calendar): ByteArray {
            val fOut = ByteArrayOutputStream()
            val output = CalendarOutputter()
            output.isValidating = false
            output.output(calendar, fOut)
            val result = fOut.toByteArray()
            fOut.close()
            return result
        }

        fun format(formats: List<String>, value: String): Date? {
            var result: Date? = null
            var i = 0
            while (result == null) {
                try {
                    if(i < formats.size) {
                        val format = SimpleDateFormat(formats[i], Locale.getDefault())
                        result = format.parse(value)
                    } else {
                        return null
                    }
                } catch (_: Exception) {}
                i++
            }
            return result
        }

        fun format(formats: List<String>, value: Date): String {
            var result = ""
            var i = 0
            while (result.isEmpty()) {
                try {
                    if(i < formats.size) {
                        val format = SimpleDateFormat(formats[i], Locale.getDefault())
                        result = format.format(value)
                    } else {
                        return ""
                    }
                } catch (_: Exception) {}
                i++
            }
            return result
        }
    }
}