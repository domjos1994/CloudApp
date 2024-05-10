package de.domjos.cloudapp.appbasics.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract.Events
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun Separator(color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)) {}
}

fun execCatch(exec: ()->Unit, context: Context) {
    try {
        exec()
    } catch (ex: Exception) {
        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
    }
}

fun execCatch(exec: ()->Unit, msg: String, context: Context) {
    try {
        exec()
    } catch (_: Exception) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}

fun <T> execCatch(exec: ()->T, context: Context): T? {
    try {
        return exec()
    } catch (ex: Exception) {
        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
    }
    return null
}

fun <T> execCatchItem(exec: (T)->Unit, item: T, context: Context) {
    try {
        exec(item)
    } catch (ex: Exception) {
        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
    }
}

fun <T, V> execCatchItem(exec: (T)->V, item: T, context: Context): V? {
    try {
        return exec(item)
    } catch (ex: Exception) {
        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
    }
    return null
}

@Composable
fun LoadingDialog(onShowDialog: (Boolean) -> Unit) {
    Dialog(onDismissRequest = { onShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.padding(5.dp).fillMaxWidth().wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.width(100.dp).height(100.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.width(100.dp).height(100.dp))
            }
        }
    }
}

fun openUrl(context: Context, url: String) {
    val callIntent = Intent(Intent.ACTION_VIEW)
    callIntent.setData(Uri.parse(url))
    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(callIntent)
}

fun openPhone(context: Context, phone: String) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.setData(Uri.parse("tel:$phone"))
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