package de.domjos.cloudapp.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.domjos.cloudapp.data.repository.AuthenticationRepository
import de.domjos.cloudapp.data.repository.CalendarRepository
import de.domjos.cloudapp.data.repository.ChatRepository
import de.domjos.cloudapp.data.repository.DataRepository
import de.domjos.cloudapp.data.repository.DefaultAuthenticationRepository
import de.domjos.cloudapp.data.repository.DefaultCalendarRepository
import de.domjos.cloudapp.data.repository.DefaultChatRepository
import de.domjos.cloudapp.data.repository.DefaultDataRepository
import de.domjos.cloudapp.data.repository.DefaultNotificationsRepository
import de.domjos.cloudapp.data.repository.DefaultRoomRepository
import de.domjos.cloudapp.data.repository.NotificationsRepository
import de.domjos.cloudapp.data.repository.RoomRepository
import javax.inject.Singleton

@Module
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
    fun bindsDataRepository(
        dataRepository: DefaultDataRepository
    ): DataRepository

    @Singleton
    @Binds
    fun bindsCalendarRepository(
        calendarRepository: DefaultCalendarRepository
    ): CalendarRepository
}