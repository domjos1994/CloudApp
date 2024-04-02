package de.domjos.cloudapp.appbasics.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
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
            .padding(5.dp)) {
        Column(
            Modifier.weight(1f).height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Info, "No Entry!")
        }
        Column(
            Modifier.weight(9f).height(50.dp),
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
            .padding(5.dp)) {
        Column(
            Modifier.weight(1f).height(50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(Icons.Rounded.Clear, "No Entry!")
        }
        Column(
            Modifier.weight(9f).height(50.dp),
            verticalArrangement = Arrangement.Center) {

            Text(
                stringResource(id = R.string.sys_no_internet),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
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