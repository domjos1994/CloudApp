package de.domjos.cloudapp.features.contacts.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.ContactRepository
import de.domjos.cloudapp.database.model.contacts.Contact
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
    private val _addressBooks = MutableStateFlow(listOf<String>())
    val addressBooks: StateFlow<List<String>> get() = _addressBooks
    private val _contacts = MutableStateFlow(listOf<Contact>())
    val contacts: StateFlow<List<Contact>> get() = _contacts


    fun getAddressBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val lst = LinkedList<String>()
            contactRepository.loadAddressBooks().forEach { lst.add(it) }
            lst.add("")
            _addressBooks.value = lst

        }
    }

    fun importAddresses(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.importContacts(updateProgress, onFinish)
            _contacts.value = contactRepository.contacts
        }
    }

    fun loadAddresses(addressBook: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            if (addressBook.isEmpty()) {
                val list = LinkedList<Contact>()
                contactRepository.loadAddressBooks().forEach { address ->
                    list.addAll(contactRepository.loadContacts(address))
                }
                _contacts.value = list
            } else {
                contactRepository.loadContacts(addressBook)
                _contacts.value = contactRepository.contacts
            }
        }
    }

    fun addOrUpdateAddress(hasInternet: Boolean, contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.insertOrUpdateContact(hasInternet, contact)
            _contacts.value = contactRepository.contacts
        }
    }

    fun deleteAddress(hasInternet: Boolean, contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.deleteContact(hasInternet, contact)
            _contacts.value = contactRepository.contacts
        }
    }

    fun hasAuthentications(): Boolean {
        return contactRepository.hasAuthentications()
    }
}