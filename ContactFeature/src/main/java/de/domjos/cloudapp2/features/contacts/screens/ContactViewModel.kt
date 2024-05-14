package de.domjos.cloudapp2.features.contacts.screens

import android.content.Context
import android.util.Log
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

    fun getAddressBooks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lst = LinkedHashMap<String, String>()
                contactRepository.loadAddressBooks().forEach {
                    if(it.startsWith("z-server")) {
                        lst[it] = context.getString(R.string.contacts_system)
                    } else if(it.startsWith("z-app")) {
                        lst[it] = context.getString(R.string.contacts_app)
                    } else {
                        lst[it] = it.replaceFirstChar(Char::titlecase)
                    }
                }
                lst[""] = context.getString(R.string.contacts_all)
                _addressBooks.value = lst
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun importAddresses(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contactRepository.importContacts(updateProgress, onFinish)
                _contacts.value = contactRepository.contacts
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun selectAddressBook(addressBook: String = "") {
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
                loadAddresses()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun loadAddresses() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_addressBook.value.isEmpty()) {
                    val list = LinkedList<Contact>()
                    contactRepository.loadAddressBooks().forEach { address ->
                        list.addAll(contactRepository.loadContacts(address))
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
}