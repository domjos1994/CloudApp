/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.helper

import android.content.Context
import android.os.Build
import de.domjos.cloudapp2.appbasics.R
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
        private val formats = listOf(
            "yyyyMMdd'T'HHmmss'Z'",
            "yyyyMMdd'T'HHmmss",
            "yyyyMMdd'T'",
            "yyyyMMdd"
        )

        val monthFormat: (Context) -> String = {it.getString(R.string.sys_format_month)}
        val dateFormat: (Context) -> String = {it.getString(R.string.sys_format_date)}
        val dateTimeFormat: (Context) -> String = {it.getString(R.string.sys_format)}
        val getFormat: (Context, Boolean) -> SimpleDateFormat = { context, showTime ->
            val format = if(showTime) {
                this.dateTimeFormat(context)
            } else {
                this.dateFormat(context)
            }
            SimpleDateFormat(format, Locale.getDefault())
        }

        fun toDate(context: Context, dt: String): Date {
            val date = try {
                this.getFormat(context, true).parse(dt)
            } catch (_: Exception) {null}

            return date
                ?: return try {
                    this.getFormat(context, false).parse(dt) ?: Date()
                } catch (_: Exception) {
                    Date()
                }
        }

        fun caldavToDate(data: String): Date? {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
            var date: Date? = null
            try {
                date = dateTimeFormat.parse(data)
            } catch (_: Exception) {
                try {
                    date = dateFormat.parse(data)
                } catch (_: Exception) {}
            }
            return date
        }

        fun toFormattedString(context: Context, dt: Date, showTime: Boolean): String {
            return this.getFormat(context, showTime).format(dt)
        }

        fun toFormattedString(context: Context, millis: Long, showTime: Boolean): String {
            val dt = Date(millis)
            return toFormattedString(context, dt, showTime)
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

        fun temporalToDate(temporal: Temporal?, context: Context): Date? {
            val sdf = this.getFormat(context, true)

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
    }
}