package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

/**
 * Custom Autocomplete-TextField with multiple
 * Autocompletion-Support
 * @param value the current Value of the TextField
 * @param onValueChange function which will be executed when the value has changed
 * @param label the Label of the TextField
 * @param onAutoCompleteChange filters a List when Autocompletion changes
 * @param modifier the Modifiers (min height is 60.dp)
 * @param isError Field contains errors
 * @param multi MultiAutocompletion-Support
 * @param separator separator
 */
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

    // state of autocomplete list expanded
    var expanded by remember { mutableStateOf(true) }
    // the selected item
    val selectedItem = remember { mutableStateOf(value) }
    // list of items
    var autoCompleteItems by remember { mutableStateOf(listOf<String>()) }

    Column {
        Row(modifier.height(60.dp)) {
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
                        selectedItem.value = TextFieldValue(it.text, TextRange(it.text.length + 1))

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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("textField"),
                trailingIcon = {
                    if(autoCompleteItems.isNotEmpty()) {
                        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("icon")) {
                            Icon(imageVector = Icons.Filled.ArrowDropDown, "Autocomplete")
                        }
                    } else {
                        expanded = false
                    }
                },
                isError = isError
            )
        }
        if(expanded) {
            Column(Modifier.border(1.dp, Color.Black).padding(10.dp).testTag("dropDownMenu")) {
                val unit = { element: String ->
                    selectedItem.value = if(multi) {
                        val content = value.text
                        val items = content.split(separator)
                        val current = items[items.size - 1].trim()
                        val text = if(current.isEmpty()) {
                            "$content $element"
                        } else {
                            content.replace(current, element)
                        }
                        TextFieldValue(text, TextRange(text.length))
                    } else {
                        TextFieldValue(element, TextRange(element.length))
                    }
                    onValueChange(selectedItem.value)
                    expanded = false
                }
                autoCompleteItems.forEach { element ->
                    Row(Modifier.fillMaxWidth().wrapContentHeight().padding(top = 10.dp)) {
                        Text(
                            text = element,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { unit(element) }
                                .height(30.dp)
                                .padding(start = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AutoCompletePreview() {
    val test = "Test"
    val test1 = "Test 1"
    val test2 = "Test 2"
    val selected = "Selected"

    CloudAppTheme {
        var selectedItem by remember { mutableStateOf("") }

        val items = mutableListOf<String>()
        items.add(test1)
        items.add(test2)

        Column {
            Row {
                AutocompleteTextField(
                    value = TextFieldValue(selectedItem),
                    onValueChange = {selectedItem = it.text},
                    label = { Text(test) },
                    onAutoCompleteChange = {items},
                    multi = true
                )
            }

            Row {
                Text("$selected: $selectedItem")
            }
        }
    }
}