package de.domjos.cloudapp.appbasics.helper

import android.content.Context
import android.widget.Toast
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

fun execCatch(exec: ()->Unit, context: Context) {
    try {
        exec()
    } catch (ex: Exception) {
        Toast.makeText(context, ex.message, Toast.LENGTH_LONG).show()
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