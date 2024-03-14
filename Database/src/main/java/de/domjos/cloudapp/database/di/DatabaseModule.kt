package de.domjos.cloudapp.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.domjos.cloudapp.database.DB
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun provideProductDao(db: DB): AuthenticationDAO {
        return db.authenticationDao()
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