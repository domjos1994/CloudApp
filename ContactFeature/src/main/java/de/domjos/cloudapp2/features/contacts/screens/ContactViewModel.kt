package de.domjos.cloudapp2.features.contacts.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.ContactRepository
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.appbasics.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList
import javax.inject.Inject

import de.domjos.cloudapp2.appbasics.helper.openEmail as oe
import de.domjos.cloudapp2.appbasics.helper.openPhone as op


@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository
): ViewModel() {
    private val _addressBooks = MutableStateFlow(mapOf<String, String>())
    val addressBooks: StateFlow<Map<String, String>> get() = _addressBooks
    private val _contacts = MutableStateFlow(listOf<Contact>())
    val contacts: StateFlow<List<Contact>> get() = _contacts
    private val _addressBook = MutableStateFlow("")
    private val _canInsert = MutableStateFlow(false)
    val canInsert: StateFlow<Boolean> get() = _canInsert
    val message = MutableLiveData<String?>()

    fun getAddressBooks(hasInternet: Boolean, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lst = LinkedHashMap<String, String>()
                contactRepository.loadAddressBooks(hasInternet).forEach {
                    lst[it.name] = if(it.label != null) it.label!! else it.name
                }
                lst[""] = context.getString(R.string.contacts_all)
                _addressBooks.value = lst
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun import(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, context: Context, hasInternet: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contactRepository.importContacts(
                    updateProgress,
                    onFinish,
                    context.getString(R.string.import_delete),
                    context.getString(R.string.import_insert),
                    context.getString(R.string.import_item)
                )
                loadAddresses(hasInternet)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun selectAddressBook(hasInternet: Boolean, addressBook: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _addressBook.value = addressBook
                if(_addressBook.value.isEmpty()) {
                    _canInsert.value = false
                } else if(_addressBook.value.trim().lowercase().startsWith("z-server")) {
                    _canInsert.value = false
                } else {
                    _canInsert.value = true
                }
                loadAddresses(hasInternet)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun loadAddresses(hasInternet: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_addressBook.value.isEmpty()) {
                    val list = LinkedList<Contact>()
                    contactRepository.loadAddressBooks(hasInternet).forEach { address ->
                        list.addAll(contactRepository.loadContacts(address.name))
                    }
                    _contacts.value = list
                } else {
                    contactRepository.loadContacts(_addressBook.value)
                    _contacts.value = contactRepository.contacts
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun addOrUpdateAddress(hasInternet: Boolean, contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contact.addressBook = _addressBook.value
                contactRepository.insertOrUpdateContact(hasInternet, contact)
                contactRepository.loadContacts(_addressBook.value)
                _contacts.value = contactRepository.contacts
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteAddress(hasInternet: Boolean, contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contact.addressBook = _addressBook.value
                contactRepository.deleteContact(hasInternet, contact)
                contactRepository.loadContacts(_addressBook.value)
                _contacts.value = contactRepository.contacts
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return contactRepository.hasAuthentications()
    }

    fun openPhone(phone: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                op(context, phone)
            } catch (ex: Exception) {
                message.postValue(ex.message)
            }
        }
    }

    fun hasPhoneFeature(context: Context): Boolean {
        return hasPermission(Manifest.permission.CALL_PHONE, context)
    }

    fun openEmail(email: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                oe(context, email)
            } catch (ex: Exception) {
                message.postValue(ex.message)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun hasPermission(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}