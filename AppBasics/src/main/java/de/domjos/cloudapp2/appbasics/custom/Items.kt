package de.domjos.cloudapp2.appbasics.custom

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import de.domjos.cloudapp2.appbasics.R

@Composable
fun NoEntryItem(colorForeground: Color, colorBackground: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Info, "No Entry!", tint = colorForeground)
        }
        Column(
            Modifier
                .weight(9f)
                .height(50.dp),
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
            .height(50.dp)
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Clear, "No Entry!", tint = colorForeground)
        }
        Column(
            Modifier
                .weight(9f)
                .height(50.dp),
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
            .height(100.dp)
            .padding(5.dp)
            .background(colorBackground)) {
        Column(
            Modifier
                .weight(1f)
                .height(100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.AccountCircle, "No Authentication!", tint = colorForeground)
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
                        fontSize = 16.sp,
                        color = colorForeground
                    )
                }
            }
            Row(Modifier.height(15.dp)) {
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
                    .height(49.dp)
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