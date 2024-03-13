package de.domjos.cloudapp.features.notifications.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme

@Composable
fun NotificationScreen() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.wrapContentHeight()) {
            Text(stringResource(id = R.string.notifications))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CloudAppTheme {
        NotificationScreen()
    }
}