package de.domjos.cloudapp.appbasics.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class Dialogs {

    companion object {
        @Composable
        fun ProgressDialog(title: String, onReload: (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit) {
            var currentProgress by remember { mutableFloatStateOf(0f) }
            var currentText by remember { mutableStateOf("") }
            var loading by remember { mutableStateOf(false) }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    loading = true
                    onReload({ progress, text ->
                        currentProgress = progress
                        currentText = text
                    }, {loading = false})
                }, enabled = !loading) {
                    Text(title)
                }

                if (loading) {
                    Text(currentText, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        @Composable
        @Preview(showBackground = true)
        fun DialogPreview() {
            val progress: (Float, String) -> Unit = {state, item ->

            }
            val finish: () -> Unit = {}
            ProgressDialog(title = "Test") {progress, finish -> progress}
        }
    }
}