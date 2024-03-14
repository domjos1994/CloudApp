package de.domjos.cloudapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication

@Database(entities = [Authentication::class], version = 1)
abstract class DB : RoomDatabase() {
    abstract fun authenticationDao(): AuthenticationDAO
}