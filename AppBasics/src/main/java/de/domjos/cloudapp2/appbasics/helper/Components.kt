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
    callIntent.setData(Uri.parse(url))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(callIntent)
}

@Throws(Exception::class)
fun openPhone(context: Context, phone: String) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.setData(Uri.parse("tel:$phone"))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(callIntent)
}

@Throws(Exception::class)
fun openEmail(context: Context, email: String) {
    val callIntent = Intent(Intent.ACTION_SEND)
    callIntent.setData(Uri.parse("mailto:"))
    callIntent.putExtra(Intent.EXTRA_EMAIL, email)
    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(callIntent)
}

fun openEvent(context: Context, uid: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = Events.CONTENT_URI.buildUpon()
    uri.appendPath(uid)
    intent.setData(uri.build())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun openContact(context: Context, uid: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = ContactsContract.Contacts.CONTENT_URI.buildUpon()
    uri.appendPath(uid)
    intent.setData(uri.build())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}