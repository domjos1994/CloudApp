/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme


@Composable
fun ProgressDialog(
    onShowDialog: (Boolean) -> Unit,
    currentText: String,
    currentProgress: Float,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer) {

    Dialog(
        onDismissRequest = {onShowDialog(false)},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(5.dp)
                .border(1.dp, foregroundColor)
                .background(backgroundColor)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(backgroundColor)) {
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(currentText, fontWeight = FontWeight.Bold, color = foregroundColor)
                }
                Row(
                    Modifier
                        .height(150.dp)
                        .background(backgroundColor),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .padding(5.dp),
                        color = foregroundColor
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingDialog(
    onShowDialog: (Boolean) -> Unit,
    foregroundColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer) {

    Dialog(
        onDismissRequest = { onShowDialog(false) },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, foregroundColor)
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .background(backgroundColor),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier
                    .width(100.dp)
                    .height(100.dp),
                    color = foregroundColor)
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun LoadingDialogPreview() {
    CloudAppTheme {
        LoadingDialog({}, Color.Yellow, Color.Red)
    }
}

@Composable
@Preview(showBackground = true)
fun ProgressDialogPreview() {
    CloudAppTheme {
        ProgressDialog(
            onShowDialog = {},
            currentText = "This is a Test!",
            currentProgress = 50.0f,
            foregroundColor = Color.Yellow,
            backgroundColor = Color.Red
        )
    }
}