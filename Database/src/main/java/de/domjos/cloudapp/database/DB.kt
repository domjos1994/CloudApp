package de.domjos.cloudapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication

@Database(entities = [Authentication::class], version = 1)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
}
