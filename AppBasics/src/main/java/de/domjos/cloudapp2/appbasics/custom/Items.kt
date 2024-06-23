package de.domjos.cloudapp2.appbasics.custom

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@Composable
fun NoEntryItem(colorForeground: Color, colorBackground: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Info, "No Entry!", tint = colorForeground)
        }
        Column(
            Modifier
                .weight(9f)
                .padding(5.dp),
            verticalArrangement = Arrangement.Center) {

            Text(
                stringResource(id = R.string.sys_no_entry),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colorForeground
            )
        }
    }
}

@Composable
fun NoInternetItem(colorForeground: Color, colorBackground: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Clear, "No Entry!", tint = colorForeground)
        }
        Column(
            Modifier
                .weight(9f)
                .padding(5.dp),
            verticalArrangement = Arrangement.Center) {

            Text(
                stringResource(id = R.string.sys_no_internet),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colorForeground
            )
        }
    }
}

@Composable
fun NoAuthenticationItem(colorForeground: Color, colorBackground: Color, toAuths: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.AccountCircle, "No Authentication!", tint = colorForeground)
        }
        Column(
            Modifier
                .weight(9f)
                .wrapContentHeight()
                .padding(5.dp))  {
            Row(Modifier.wrapContentHeight()) {
                Column(verticalArrangement = Arrangement.Center) {

                    Text(
                        stringResource(id = R.string.sys_no_connection),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorForeground
                    )
                }
            }
            Row(Modifier.wrapContentHeight()) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        stringResource(id = R.string.sys_no_connection_description),
                        fontSize = 9.sp,
                        color = colorForeground
                    )
                }
            }
            Row(
                Modifier
                    .wrapContentHeight()
                    .padding(2.dp)) {
                Column(verticalArrangement = Arrangement.Center) {
                    Button(onClick = {toAuths()}, colors = ButtonDefaults.buttonColors(containerColor = colorForeground, contentColor = colorBackground)) {
                        Text(stringResource(id = R.string.sys_no_connection_button), fontSize = 11.sp, color = colorBackground)
                    }
                }
            }
        }
    }
}

@Composable
fun ShowDeleteDialog(onShowDialog: (Boolean) -> Unit, deleteAction: () -> Unit) {
    ConfirmationDialog(
        onShowDialog,
        deleteAction,
        title = stringResource(R.string.sys_delete_item),
        cancelText = "Cancel deletion item!",
        okayText = "Delete item!"
    )
}

@Composable
fun ConfirmationDialog(onShowDialog: (Boolean) -> Unit, action: () -> Unit, title: String, cancelText: String, okayText: String) {
    Dialog(onDismissRequest = { onShowDialog(false) }) {
        Surface(
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(14f)) {}
                    Column(modifier = Modifier.weight(3f)) {
                        IconButton(
                            onClick = { onShowDialog(false) },
                            modifier = Modifier.padding(3.dp)) {
                            Icon(Icons.Rounded.Clear, contentDescription = cancelText)
                        }
                    }
                    Column(modifier = Modifier.weight(3f)) {
                        IconButton(onClick = {
                            action()
                            onShowDialog(false)
                        },
                            modifier = Modifier.padding(3.dp)) {
                            Icon(Icons.Rounded.Check, contentDescription = okayText)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoEntryItemPreview() {
    NoEntryItem(Color.White, Color.Blue)
}

@Preview(showBackground = true)
@Composable
fun NoInternetItemPreview() {
    NoInternetItem(Color.White, Color.Blue)
}

@Preview(showBackground = true)
@Composable
fun NoAuthenticationItemPreview() {
    NoAuthenticationItem(Color.White, Color.Blue) {}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DeleteDialogPreview() {
    CloudAppTheme {
        ShowDeleteDialog(onShowDialog = {}) {}
    }
}