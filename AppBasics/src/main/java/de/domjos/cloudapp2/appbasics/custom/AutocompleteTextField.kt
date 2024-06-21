package de.domjos.cloudapp2.appbasics.custom

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@Composable
fun AutocompleteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: @Composable (() -> Unit),
    onAutoCompleteChange: (TextFieldValue) -> List<String>,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    multi: Boolean = false,
    separator: String = ",") {

    var expanded by remember { mutableStateOf(true) }
    val selectedItem = remember { mutableStateOf(value) }
    var autoCompleteItems by remember { mutableStateOf(listOf<String>()) }

    Row(modifier.wrapContentHeight()) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {
                onValueChange(it)
                if(!expanded) {
                    var text = it.text
                    if(multi) {
                        if(text.contains(separator)) {
                            val items = it.text.split(separator)
                            text = items[items.size - 1]
                        }
                    }
                    selectedItem.value = TextFieldValue(it.text, TextRange(it.text.length))

                    autoCompleteItems = onAutoCompleteChange(TextFieldValue(text))
                    autoCompleteItems = autoCompleteItems.filter { item ->
                        if(multi) {
                            !selectedItem.value.text.split(separator).toList().contains(item)
                        } else {
                            !selectedItem.value.text.contains(item)
                        }
                    }
                }
            },
            label = label,
            modifier = modifier,
            trailingIcon = {
                if(autoCompleteItems.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(imageVector = Icons.Filled.ArrowDropDown, "Autocomplete")
                    }
                } else {
                    expanded = false
                }
            },
            isError = isError
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        autoCompleteItems.forEach {
            DropdownMenuItem(
                text = { Text(it) },
                onClick = {
                    selectedItem.value = if(multi) {
                        val items = value.text.split(separator)
                        val text = value.text.replace(items[items.size - 1], it)
                        TextFieldValue(text, TextRange(text.length))
                    } else {
                        TextFieldValue(it, TextRange(it.length))
                    }
                    onValueChange(selectedItem.value)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AutoCompletePreview() {
    CloudAppTheme {
        var field by remember { mutableStateOf(TextFieldValue("")) }

        AutocompleteTextField(
            value = field,
            onValueChange = {
                field = it
                            },
            label = { Text("Test") },
            onAutoCompleteChange = {
                val lst = mutableListOf<String>()
                for(i in 1..10) {
                    lst.add("${it.text}$i")
                }
                lst
            },
            Modifier.padding(5.dp),
            true
        )
    }
}