package de.domjos.cloudapp2.features.contacts.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.ContactRepository
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
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
): LogViewModel() {
    private val _addressBooks = MutableStateFlow(mapOf<String, String>())
    val addressBooks: StateFlow<Map<String, String>> get() = _addressBooks
    private val _contacts = MutableStateFlow(listOf<Contact>())
    val contacts: StateFlow<List<Contact>> get() = _contacts
    private val _addressBook = MutableStateFlow("")
    private val _canInsert = MutableStateFlow(false)
    val canInsert: StateFlow<Boolean> get() = _canInsert

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
                printException(ex, this)
            }
        }
    }

    fun import(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, context: Context, hasInternet: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contactRepository.importContacts(
                    updateProgress,
                    onFinish,
                    context.getString(R.string.import_loading),
                    context.getString(R.string.import_delete),
                    context.getString(R.string.import_insert),
                    context.getString(R.string.import_update)
                )
                loadAddresses(hasInternet)
            } catch (ex: Exception) {
                printException(ex, this)
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
                printException(ex, this)
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
                printException(ex, this)
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
                printException(ex, this)
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
                printException(ex, this)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return contactRepository.hasAuthentications()
    }

    fun openPhone(phone: String, toPermissions: () -> Unit, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                op(context, phone)
            } catch (ex: SecurityException) {
                viewModelScope.launch(Dispatchers.Main) { toPermissions() }
            } catch (ex: Exception) {
                printException(ex, this)
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
                printException(ex, this)
            }
        }
    }

    fun openChat(contact: Contact, onOpen: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = contactRepository.getOrCreateChat(contact)
                viewModelScope.launch(Dispatchers.Main) {
                    onOpen(token)
                }
            } catch (ex: Exception) {
                printException(ex, this)
                onOpen("")
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun hasPermission(permission: String, context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}