package de.domjos.cloudapp.features.chats.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp.appbasics.R

@Composable
fun ChatScreen() {
    Row {
        Column {
            Text(stringResource(id = R.string.chats))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CloudAppTheme {
        ChatScreen()
    }
}