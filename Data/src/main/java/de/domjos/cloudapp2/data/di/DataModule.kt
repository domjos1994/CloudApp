package de.domjos.cloudapp2.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.data.repository.AuthenticationRepository
import de.domjos.cloudapp2.data.repository.CalendarRepository
import de.domjos.cloudapp2.data.repository.ChatRepository
import de.domjos.cloudapp2.data.repository.ContactRepository
import de.domjos.cloudapp2.data.repository.DataRepository
import de.domjos.cloudapp2.data.repository.DefaultAuthenticationRepository
import de.domjos.cloudapp2.data.repository.DefaultCalendarRepository
import de.domjos.cloudapp2.data.repository.DefaultChatRepository
import de.domjos.cloudapp2.data.repository.DefaultContactRepository
import de.domjos.cloudapp2.data.repository.DefaultDataRepository
import de.domjos.cloudapp2.data.repository.DefaultNoteRepository
import de.domjos.cloudapp2.data.repository.DefaultNotificationsRepository
import de.domjos.cloudapp2.data.repository.DefaultRoomRepository
import de.domjos.cloudapp2.data.repository.NoteRepository
import de.domjos.cloudapp2.data.repository.NotificationsRepository
import de.domjos.cloudapp2.data.repository.RoomRepository
import javax.inject.Singleton

@Module(includes = [SettingsProvider::class])
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsAuthenticationRepository(
        authenticationRepository: DefaultAuthenticationRepository
    ): AuthenticationRepository

    @Singleton
    @Binds
    fun bindsRoomRepository(
        roomRepository: DefaultRoomRepository
    ): RoomRepository

    @Singleton
    @Binds
    fun bindsChatRepository(
        chatRepository: DefaultChatRepository
    ): ChatRepository

    @Singleton
    @Binds
    fun bindsNotificationsRepository(
        notificationsRepository: DefaultNotificationsRepository
    ): NotificationsRepository

    @Singleton
    @Binds
    fun bindsNoteRepository(
        noteRepository: DefaultNoteRepository
    ): NoteRepository

    @Singleton
    @Binds
    fun bindsDataRepository(
        dataRepository: DefaultDataRepository
    ): DataRepository

    @Singleton
    @Binds
    fun bindsCalendarRepository(
        calendarRepository: DefaultCalendarRepository
    ): CalendarRepository

    @Singleton
    @Binds
    fun bindsContactRepository(
        contactRepository: DefaultContactRepository
    ): ContactRepository


}

@Module
@InstallIn(SingletonComponent::class)
class SettingsProvider {
    @Provides
    @Singleton
    fun provideSettings(@ApplicationContext appContext: Context): Settings {
        return Settings(appContext)
    }
}