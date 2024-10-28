package de.domjos.cloudapp2.screens

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context

import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.adapter.getOrCreateSyncAccount
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val settings: Settings,
    private val authenticationDAO: AuthenticationDAO
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

    fun initContactSync(context: Context, contactRegularity: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val authentications = authenticationDAO.getAll().first()
            authentications.forEach { authentication ->
                val account = getOrCreateSyncAccount(context, authentication)
                addContactSync(account, contactRegularity)
            }
        }
    }

    fun initCalendarSync(context: Context, contactRegularity: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val authentications = authenticationDAO.getAll().first()
            authentications.forEach { authentication ->
                val account = getOrCreateSyncAccount(context, authentication)
                addCalendarSync(account, contactRegularity)
            }
        }
    }

    private fun addContactSync(account: Account, contactRegularity: Float) {
        try {
            // contact
            ContentResolver.removePeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
            ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY, (contactRegularity * 60).toLong())
            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
        } catch (ex: Exception) {
            this.message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }

    private fun addCalendarSync(account: Account, calendarRegularity: Float) {
        try {
            // calendar
            ContentResolver.removePeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY)
            ContentResolver.setIsSyncable(account, CalendarContract.AUTHORITY, 1)
            ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)
            ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY, (calendarRegularity * 60 * 1000).toLong())
            ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
        } catch (ex: Exception) {
            this.message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }
}