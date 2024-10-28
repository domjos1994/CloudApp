package de.domjos.cloudapp2.appbasics.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract.Events
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun Separator(color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)) {}
}

fun openUrl(context: Context, url: String) {
    val callIntent = Intent(Intent.ACTION_VIEW)
    callIntent.data = Uri.parse(url)
    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(callIntent)
}

@Throws(Exception::class)
fun openPhone(context: Context, phone: String) {
    val intent = createPhoneIntent(phone)
    context.startActivity(intent)
}

@Throws(Exception::class)
fun openEmail(context: Context, email: String) {
    val intent = createMailIntent(email)
    context.startActivity(intent)
}

@Throws(Exception::class)
fun hasEmail(context: Context, email: String): Boolean {
    val intent = createMailIntent(email)
    return hasIntent(intent, context)
}

@Throws(Exception::class)
fun hasPhone(context: Context, phone: String): Boolean {
    val intent = createPhoneIntent(phone)
    return hasIntent(intent, context)
}

fun openEvent(context: Context, uid: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = Events.CONTENT_URI.buildUpon()
    uri.appendPath(uid)
    intent.data = uri.build()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openContact(context: Context, uid: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
    uri.appendPath(uid)
    intent.data = uri.build()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private fun createPhoneIntent(phone: String): Intent {
    val intent = Intent(Intent.ACTION_CALL)
    intent.data = Uri.parse("tel:$phone")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
}

private fun createMailIntent(email: String): Intent {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:$email")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return intent
}

private fun hasIntent(intent: Intent, context: Context): Boolean {
    return intent.resolveActivity(context.packageManager) != null
}