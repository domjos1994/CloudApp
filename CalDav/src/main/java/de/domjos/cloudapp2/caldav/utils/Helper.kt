/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.caldav.utils

import com.thegrizzlylabs.sardineandroid.DavResource
import de.domjos.cloudapp2.database.model.Authentication
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import okhttp3.Headers
import java.io.ByteArrayOutputStream
import java.util.Base64

@Suppress("MemberVisibilityCanBePrivate")
class Helper {

    companion object {

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
    }
}