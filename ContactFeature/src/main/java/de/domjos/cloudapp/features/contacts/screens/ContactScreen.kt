package de.domjos.cloudapp.features.contacts.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme

@Composable
fun ContactScreen() {
    Row {
        Column {
            Text(stringResource(id = R.string.contacts))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CloudAppTheme {
        ContactScreen()
    }
}