package de.domjos.cloudapp.screens

import android.accounts.Account
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.Settings
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val settings: Settings
) : ViewModel() {
    val message = MutableLiveData<String?>()

    fun getContactRegularitySetting(): Float {
        val state = mutableFloatStateOf(1.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.contactRegularityKey, 1.0f)
        }
        return state.floatValue
    }

    fun getCalendarRegularitySetting(): Float {
        val state = mutableFloatStateOf(1.0f)
        viewModelScope.launch {
            state.floatValue = settings.getSetting(Settings.calendarRegularityKey, 1.0f)
        }
        return state.floatValue
    }

    fun addContactSync(account: Account, contactRegularity: Float) {
        try {
            // contact
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, Bundle(), (contactRegularity * 60 * 1000).toLong())
        } catch (ex: Exception) {
            this.message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }

    fun addCalendarSync(account: Account, calendarRegularity: Float) {
        try {
            // calendar
            ContentResolver.setIsSyncable(account, CalendarContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, Bundle(), (calendarRegularity * 60 * 1000).toLong())
        } catch (ex: Exception) {
            this.message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }

    fun createNotificationChannel(context: Context) {
        try {
            val channel = NotificationChannel("cloud_app_notifications", "CloudApp", NotificationManager.IMPORTANCE_NONE)
            channel.setSound(null, null)
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        } catch (ex: Exception) {
            this.message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }
}