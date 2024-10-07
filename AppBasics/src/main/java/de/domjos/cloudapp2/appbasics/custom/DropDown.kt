package de.domjos.cloudapp2.appbasics.custom

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@Composable
fun DropDown(
    items: List<String>,
    initial: String,
    onSelected: (String) -> Unit,
    label: String,
    colorBackground: Color = Color.White,
    colorForeground: Color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor) {

    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(initial) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorBackground)
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {selectedItem = it},
            label = {Text(label, color = colorForeground)},
            trailingIcon = { IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    stringResource(R.string.sys_dropdown_open),
                    tint = colorForeground
                )
            }},
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colorForeground,
                unfocusedTextColor = colorForeground,
                focusedSupportingTextColor = colorForeground,
                unfocusedSupportingTextColor = colorForeground,
                focusedBorderColor = colorForeground,
                unfocusedBorderColor = colorForeground
            ),
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelected(it)
                        selectedItem = if(it=="") initial else it
                        expanded = !expanded
                    })
            }
        }
    }
}

@Composable
fun <T> DropDown(
    items: List<T>,
    initial: T,
    onSelected: (T) -> Unit,
    propertyLabel: (T) -> String,
    label: String,
    colorBackground: Color = Color.White,
    colorForeground: Color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(initial) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
            .padding(1.dp)
            .height(50.dp)
            .background(colorBackground)
    ) {
        Row {
            Column(
                Modifier
                    .weight(4f)
                    .padding(start = 5.dp)
                    .height(50.dp),
                verticalArrangement = Arrangement.Center) {
                Text(
                    label,
                    fontWeight = FontWeight.Normal,
                    color = colorForeground
                )
            }

            Column(
                Modifier
                    .weight(7f)
                    .height(50.dp)
                    .border(1.dp, colorForeground, shape = RoundedCornerShape(8f))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier.weight(8f),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            selectedItem.toString(),
                            fontWeight = FontWeight.Normal,
                            color = colorForeground
                        )
                    }
                    Column(
                        Modifier.weight(2f),
                        horizontalAlignment = Alignment.End) {

                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.testTag("DropDownIcon")) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "More",
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp),
                                colorForeground
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(propertyLabel(it)) },
                    onClick = {
                        onSelected(it)
                        selectedItem = if(it=="") initial else it
                        expanded = !expanded
                    })
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DropDownPreview() {
    CloudAppTheme {
        DropDown(listOf("Item 1", "Item 2", "Item 3"), initial = "Item 1",  {}, label = "Test")
    }
}