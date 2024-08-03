package de.domjos.cloudapp2.receiver

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp2.data.repository.ContactRepository
import de.domjos.cloudapp2.widgets.ContactsWidget
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class ContactsWidgetReceiver : AbstractWidgetReceiver(ContactsWidget()) {

    @Inject
    lateinit var contactsRepository: ContactRepository

    override suspend fun observe(context: Context) {
        try {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(ContactsWidget::class.java).firstOrNull()

            val items = mutableListOf<WidgetContact>()
            contactsRepository.loadAddressBooks(false).forEach {
                contactsRepository.loadContacts(it.name).forEach {contact ->
                    var phone = ""
                    if(contact.phoneNumbers.isNotEmpty()) {
                        phone = contact.phoneNumbers[0].value
                    }
                    items.add(WidgetContact("${contact.givenName} ${contact.familyName}".trim(), phone, contact.addressBook, contact.photo))
                }
            }

            glanceId?.let {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                    pref.toMutablePreferences().apply {
                        val data = Json.encodeToString(items)
                        this[currentData] = data
                    }
                }
                glanceAppWidget.update(context, it)
            }
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }
}

@Serializable
data class WidgetContact(val name: String, val phone: String, val addressBook: String, val photo: ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WidgetContact

        if (name != other.name) return false
        if (phone != other.phone) return false
        if (addressBook != other.addressBook) return false
        if (photo != null) {
            if (other.photo == null) return false
            if (!photo.contentEquals(other.photo)) return false
        } else if (other.photo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + phone.hashCode()
        result = 31 * result + addressBook.hashCode()
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        return result
    }
}