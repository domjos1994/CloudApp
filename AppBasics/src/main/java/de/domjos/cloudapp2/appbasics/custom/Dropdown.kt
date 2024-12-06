
package de.domjos.cloudapp2.appbasics.custom

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    value: String,
    onValueChange: (String) -> Unit,
    list: List<String>,
    modifier: Modifier = Modifier,
    label: String = "",
    colorBackground: Color = Color.Transparent,
    colorForeground: Color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
    isError: Boolean = false

) {
    var showDropDown by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(value) }

    ExposedDropdownMenuBox(
        expanded = showDropDown,
        onExpandedChange = {
            showDropDown = !showDropDown
        },
        modifier = modifier.background(colorBackground)
    ) {
        OutlinedTextField(
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .background(colorBackground)
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            value = selected,
            onValueChange = {},
            label = { Text(label, color = colorForeground) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = showDropDown
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colorForeground,
                unfocusedTextColor = colorForeground,
                focusedSupportingTextColor = colorForeground,
                unfocusedSupportingTextColor = colorForeground,
                focusedBorderColor = colorForeground,
                unfocusedBorderColor = colorForeground,
                focusedTrailingIconColor = colorForeground,
                unfocusedTrailingIconColor = colorForeground
            ),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = showDropDown,
            onDismissRequest = {showDropDown = false},
            modifier = Modifier.background(colorBackground)
        ) {
            list.forEach { text ->
                DropdownMenuItem(text = {
                    Text(
                        text,
                        color = colorForeground
                    )
                }, onClick = {
                    onValueChange(text)
                    selected = text
                    showDropDown = false
                })
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun Preview() {
    val items = listOf("Test-1", "Test-2", "Test-3", "Test-4")

    CloudAppTheme {
        Row(Modifier.background(Color.Blue)) {
            Dropdown(
                value = "Test-1",
                onValueChange = {},
                list = items,
                label = "Test",
                colorForeground = Color.White,
                colorBackground = Color.Blue
            )
        }
    }
}