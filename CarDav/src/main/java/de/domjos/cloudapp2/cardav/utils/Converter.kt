/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.cardav.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.Temporal
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Converter {

    companion object {
        private val format = "dd.MM.yyyy"

        fun temporalToDate(temporal: Temporal?): Date? {
            val sdf = SimpleDateFormat("$format hh:mm:ss", Locale.getDefault())

            if(temporal != null) {
                var year: Int? = null
                var month: Int? = null
                var day: Int? = null
                var hour: Int? = 0
                var minute: Int? = 0
                var second: Int? = 0
                if(temporal is LocalDateTime) {
                    year = temporal.year
                    month = temporal.monthValue
                    day = temporal.dayOfMonth
                    hour = temporal.hour
                    minute = temporal.minute
                    second = temporal.second
                } else if(temporal is LocalDate) {
                    year = temporal.year
                    month = temporal.monthValue
                    day = temporal.dayOfMonth
                }
                if(temporal is Instant) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        year = LocalDate.ofInstant(temporal, ZoneId.of("UTC"))?.year
                        month = LocalDate.ofInstant(temporal, ZoneId.systemDefault())?.monthValue
                        day = LocalDate.ofInstant(temporal, ZoneId.systemDefault())?.dayOfMonth
                        hour = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault())?.hour ?: 0
                        minute = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault())?.minute ?: 0
                        second = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault())?.second ?: 0
                    } else {
                        val ld = temporal.atZone(ZoneId.systemDefault()).toLocalDateTime()

                        year = ld.year
                        month = ld.monthValue
                        day = ld.dayOfMonth
                        hour = ld.hour
                        minute = ld.minute
                        second = ld.second
                    }
                }
                return sdf.parse("$day.$month.$year $hour:$minute:$second")
            }
            return null
        }

        fun calendarToLocalDate(calendar: Calendar): LocalDate {
            val tz = calendar.timeZone
            val zoneId = tz.toZoneId()
            return LocalDateTime.ofInstant(calendar.toInstant(), zoneId).toLocalDate()
        }


        fun dateToString(date: Date): String {
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            return sdf.format(date)
        }
    }
}