package de.domjos.cloudapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.domjos.cloudapp.database.converters.AddressTypeConverter
import de.domjos.cloudapp.database.converters.DateConverter
import de.domjos.cloudapp.database.converters.PhoneTypeConverter
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.dao.CalendarEventDAO
import de.domjos.cloudapp.database.dao.ContactDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.Contact
import de.domjos.cloudapp.database.model.contacts.Email
import de.domjos.cloudapp.database.model.contacts.Phone

@Database(
    entities = [
        Authentication::class, CalendarEvent::class,
        Contact::class, Address::class, Phone::class, Email::class
   ],
    version = 5,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4),
        AutoMigration(4, 5)
    ]
)
@TypeConverters(DateConverter::class, AddressTypeConverter::class, PhoneTypeConverter::class)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
    abstract fun calendarEventDao(): CalendarEventDAO
    abstract fun contactDao(): ContactDAO
}