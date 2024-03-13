package de.domjos.cloudapp.features.calendars.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp.appbasics.R

@Composable
fun CalendarScreen() {
    Row {
        Column {
            Text(stringResource(id = R.string.calendars))
        }
    }
}

@Composable
fun Day(text: String) {
    Row(modifier = Modifier.wrapContentWidth()) {
        Column(modifier = Modifier.wrapContentHeight()) {
            Text(text=text, modifier = Modifier.wrapContentHeight().wrapContentWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CloudAppTheme {
        CalendarScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun DayPreview() {
    CloudAppTheme {
        Day("1")
    }
}