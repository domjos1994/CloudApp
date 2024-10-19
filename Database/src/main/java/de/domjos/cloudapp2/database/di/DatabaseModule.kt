/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.dao.LogDAO
import de.domjos.cloudapp2.database.dao.ToDoItemDAO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideProductDao(db: DB): AuthenticationDAO {
        return db.authenticationDao()
    }

    @Provides
    fun provideCalendarEventDao(db: DB): CalendarEventDAO {
        return db.calendarEventDao()
    }

    @Provides
    fun provideContactDao(db: DB): ContactDAO {
        return db.contactDao()
    }

    @Provides
    fun provideToDoItemDao(db: DB): ToDoItemDAO {
        return db.todoItemDao()
    }

    @Provides
    fun provideLogDao(db: DB): LogDAO {
        return db.logDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): DB {
        return Room.databaseBuilder(
            appContext,
            DB::class.java,
            "CloudApp"
        ).allowMainThreadQueries().build()
    }
}