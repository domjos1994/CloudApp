package de.domjos.cloudapp2.caldav.utils

import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

class Helper {

    companion object {

        fun localDateTimeToLong(ldt: LocalDateTime): Long {
            val zdt = ldt.atZone(TimeZone.getDefault().toZoneId())
            return zdt.toInstant().toEpochMilli()
        }

        fun longTimeToLocalDateTime(ts: Long): LocalDateTime {
            val instant = Instant.ofEpochMilli(ts)
            return LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        }

    }
}