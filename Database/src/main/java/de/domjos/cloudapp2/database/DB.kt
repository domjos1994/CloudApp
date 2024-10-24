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
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import de.domjos.cloudapp2.database.converters.AddressTypeConverter
import de.domjos.cloudapp2.database.converters.DateConverter
import de.domjos.cloudapp2.database.converters.PhoneTypeConverter
import de.domjos.cloudapp2.database.converters.ToDoStatusConverter
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.dao.LogDAO
import de.domjos.cloudapp2.database.dao.ToDoItemDAO
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.Log
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import de.domjos.cloudapp2.database.model.todo.ToDoItem

@Database(
    entities = [
        Authentication::class, CalendarEvent::class,
        Contact::class, Address::class, Phone::class, Email::class,
        ToDoItem::class, Log::class
   ],
    version = 27,
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
        AutoMigration(13, 14),
        AutoMigration(14, 15),
        AutoMigration(15, 16),
        AutoMigration(16, 17),
        AutoMigration(17, 18),
        AutoMigration(18, 19),
        AutoMigration(19, 20, DeleteEventFromTo::class),
        AutoMigration(20, 21),
        AutoMigration(21, 22),
        AutoMigration(22, 23),
        AutoMigration(23, 24),
        AutoMigration(24, 25),
        AutoMigration(25, 26),
        AutoMigration(26, 27)
    ]
)
@TypeConverters(DateConverter::class, AddressTypeConverter::class, PhoneTypeConverter::class, ToDoStatusConverter::class)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
    abstract fun calendarEventDao(): CalendarEventDAO
    abstract fun contactDao(): ContactDAO
    abstract fun todoItemDao(): ToDoItemDAO
    abstract fun logDao(): LogDAO

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

@DeleteColumn("calendarEvents", "from")
@DeleteColumn("calendarEvents", "to")
class DeleteEventFromTo : AutoMigrationSpec