package de.domjos.cloudapp.appbasics.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.domjos.cloudapp.appbasics.R

@Composable
fun NoEntryItem() {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.inversePrimary)) {
        Column(
            Modifier
                .weight(1f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Info, "No Entry!")
        }
        Column(
            Modifier
                .weight(9f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center) {

            Text(
                stringResource(id = R.string.sys_no_entry),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun NoInternetItem() {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.inversePrimary)) {
        Column(
            Modifier
                .weight(1f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Clear, "No Entry!")
        }
        Column(
            Modifier
                .weight(9f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center) {

            Text(
                stringResource(id = R.string.sys_no_internet),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun NoAuthenticationItem(toAuths: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.inversePrimary)) {
        Column(
            Modifier
                .weight(1f)
                .height(100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.AccountCircle, "No Authentication!")
        }
        Column(
            Modifier
                .weight(9f)
                .height(100.dp))  {
            Row(Modifier.height(35.dp)) {
                Column(verticalArrangement = Arrangement.Center) {

                    Text(
                        stringResource(id = R.string.sys_no_connection),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Row(Modifier.height(15.dp)) {
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        stringResource(id = R.string.sys_no_connection_description),
                        fontSize = 9.sp
                    )
                }
            }
            Row(Modifier.height(49.dp).padding(2.dp)) {
                Column(verticalArrangement = Arrangement.Center) {
                    Button(onClick = {toAuths()}) {
                        Text(stringResource(id = R.string.sys_no_connection_button), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoEntryItemPreview() {
    NoEntryItem()
}

@Preview(showBackground = true)
@Composable
fun NoInternetItemPreview() {
    NoInternetItem()
}

@Preview(showBackground = true)
@Composable
fun NoAuthenticationItemPreview() {
    NoAuthenticationItem {}
}