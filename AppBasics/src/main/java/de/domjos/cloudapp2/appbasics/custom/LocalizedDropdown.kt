
package de.domjos.cloudapp2.appbasics.custom

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
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
fun LocalizedDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    list: List<DropDownItem>,
    modifier: Modifier = Modifier,
    label: String = "",
    colorBackground: Color = Color.Transparent,
    colorForeground: Color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
    isError: Boolean = false

) {
    var showDropDown by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(list.find { it.value == value }?.label ?: "") }

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
            label = { Text(label) },
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
                unfocusedBorderColor = colorForeground
            ),
            isError = isError
        )
        ExposedDropdownMenu(
            expanded = showDropDown,
            onDismissRequest = {showDropDown = false},
            modifier = Modifier.background(colorBackground)
        ) {
            list.forEach { item ->
                DropdownMenuItem(text = {
                    Text(
                        item.label,
                        color = colorForeground
                    )
                }, onClick = {
                    onValueChange(item.value)
                    selected = item.label
                    showDropDown = false
                })
            }
        }
    }
}

data class DropDownItem(val value: String, val label: String)

@Composable
@Preview(showBackground = true)
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun LocalizedDropDownPreview() {
    val items = listOf(
        DropDownItem("test-1", "Test-1"),
        DropDownItem("test-2","Test-2"),
        DropDownItem("test-3","Test-3"),
        DropDownItem("test-4", "Test-4")
    )

    CloudAppTheme {
        LocalizedDropdown(
            value = "Test-1",
            onValueChange = {},
            list = items,
            label = "Test"
        )
    }
}