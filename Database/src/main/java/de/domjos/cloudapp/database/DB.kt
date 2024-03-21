package de.domjos.cloudapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.dao.CalendarEventDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.database.model.calendar.CalendarEvent

@Database(
    entities = [Authentication::class, CalendarEvent::class],
    version = 4,
    autoMigrations = [
        AutoMigration(1, 2),
        AutoMigration(2, 3),
        AutoMigration(3, 4)
    ]
)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
    abstract fun calendarEventDao(): CalendarEventDAO
}