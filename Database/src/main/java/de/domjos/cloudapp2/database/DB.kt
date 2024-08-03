/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import de.domjos.cloudapp2.database.converters.AddressTypeConverter
import de.domjos.cloudapp2.database.converters.DateConverter
import de.domjos.cloudapp2.database.converters.PhoneTypeConverter
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone

@Database(
    entities = [
        Authentication::class, CalendarEvent::class,
        Contact::class, Address::class, Phone::class, Email::class
   ],
    version = 14,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5),
        AutoMigration(5, 6),
        AutoMigration(6, 7),
        AutoMigration(7, 8),
        AutoMigration(8, 9),
        AutoMigration(9, 10),
        AutoMigration(10, 11),
        AutoMigration(11, 12),
        AutoMigration(12, 13, DeleteDataItem::class),
        AutoMigration(13, 14)
    ]
)
@TypeConverters(DateConverter::class, AddressTypeConverter::class, PhoneTypeConverter::class)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
    abstract fun calendarEventDao(): CalendarEventDAO
    abstract fun contactDao(): ContactDAO

    companion object {
        fun newInstance(context: Context): DB {
            return Room.databaseBuilder(
                context,
                DB::class.java,
                "CloudApp"
            ).allowMainThreadQueries().build()
        }
    }
}

@DeleteTable("dataItems")
class DeleteDataItem : AutoMigrationSpec