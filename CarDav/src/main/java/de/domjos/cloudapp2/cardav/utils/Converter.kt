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
            val sdf = SimpleDateFormat(format, Locale.getDefault())

            if(temporal != null) {
                var year: Int? = null
                var month: Int? = null
                var day: Int? = null
                if(temporal is LocalDate) {
                    year = temporal.year
                    month = temporal.monthValue
                    day = temporal.dayOfMonth
                }
                if(temporal is Instant) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        year = LocalDate.ofInstant(temporal, ZoneId.systemDefault())?.year
                        month = LocalDate.ofInstant(temporal, ZoneId.systemDefault())?.monthValue
                        day = LocalDate.ofInstant(temporal, ZoneId.systemDefault())?.dayOfMonth
                    } else {
                        val ld = temporal.atZone(ZoneId.systemDefault()).toLocalDate()

                        year = ld.year
                        month = ld.monthValue
                        day = ld.dayOfMonth
                    }
                }
                return sdf.parse("$day.$month.$year")
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