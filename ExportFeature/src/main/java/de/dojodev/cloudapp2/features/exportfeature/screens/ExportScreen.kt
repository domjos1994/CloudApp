/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.appbasics.R

@Composable
fun ExportScreen(viewModel: ExportViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, id: Long? = null) {
    val context = LocalContext.current
    val extensions by viewModel.extensions.collectAsStateWithLifecycle()
    val types by viewModel.types.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.initBuilder(context)
    }

    LogViewModel.Init(viewModel)

    ExportScreen(
        extensions,
        types,
        {type -> viewModel.getTypes(type)},
        {type, name, open -> viewModel.doExport(type, name, open, id)},
        viewModel,
        colorBackground,
        colorForeground
    )
}

@Composable
fun ExportScreen(
    extensions: List<String>,
    types: List<String>,
    getTypes: (String) -> Unit,
    doExport: (String, String, Boolean) -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
    colorBackground: Color,
    colorForeground: Color) {

    ConstraintLayout(
        Modifier.fillMaxSize().background(colorBackground)) {
        val (nameAndExt, type, open, label, export) = createRefs()

        var valName by remember { mutableStateOf(TextFieldValue("")) }
        var valExtension by remember { mutableStateOf(TextFieldValue("")) }
        var valType by remember { mutableStateOf(TextFieldValue("")) }
        var valOpen by remember { mutableStateOf(false) }
        var valLabel by remember { mutableStateOf("") }

        viewModel.progress.observe(LocalLifecycleOwner.current) { msg ->
            if(msg != null) {
                valLabel = msg
                viewModel.progress.value = null
            }
        }

        Row(Modifier.constrainAs(nameAndExt) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(1.dp)) {
            var showExtensionDropDown by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = valName,
                onValueChange = {va -> valName = va},
                label = {Text(text = stringResource(R.string.export_name), color = colorForeground)},
                modifier = Modifier.weight(5f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorForeground,
                    unfocusedTextColor = colorForeground,
                    focusedSupportingTextColor = colorForeground,
                    unfocusedSupportingTextColor = colorForeground,
                    focusedBorderColor = colorForeground,
                    unfocusedBorderColor = colorForeground
                )
            )
            OutlinedTextField(
                value = valExtension,
                onValueChange = {va -> valExtension = va},
                label = {Text(text = stringResource(R.string.export_extensions), color = colorForeground)},
                modifier = Modifier.weight(5f),
                trailingIcon = {
                    IconButton({showExtensionDropDown=!showExtensionDropDown}) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            stringResource(R.string.export_extensions),
                            tint = colorForeground
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorForeground,
                    unfocusedTextColor = colorForeground,
                    focusedSupportingTextColor = colorForeground,
                    unfocusedSupportingTextColor = colorForeground,
                    focusedBorderColor = colorForeground,
                    unfocusedBorderColor = colorForeground
                )
            )
            DropdownMenu(showExtensionDropDown, {showExtensionDropDown=false}) {
                extensions.forEach { extension ->
                    DropdownMenuItem({Text(extension)}, onClick = {
                        valExtension = TextFieldValue(extension)
                        showExtensionDropDown = false
                        getTypes(extension)
                    })
                }
            }
        }
        Row(Modifier.constrainAs(type) {
            top.linkTo(nameAndExt.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(1.dp)) {
            var showTypeDropDown by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = valType,
                onValueChange = {va -> valType = va},
                label = {Text(text = stringResource(R.string.export_types), color = colorForeground)},
                modifier = Modifier.weight(10f),
                trailingIcon = {
                    IconButton({showTypeDropDown=!showTypeDropDown}) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            stringResource(R.string.export_types),
                            tint = colorForeground
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colorForeground,
                    unfocusedTextColor = colorForeground,
                    focusedSupportingTextColor = colorForeground,
                    unfocusedSupportingTextColor = colorForeground,
                    focusedBorderColor = colorForeground,
                    unfocusedBorderColor = colorForeground
                )
            )
            DropdownMenu(showTypeDropDown, {showTypeDropDown=false}) {
                types.forEach { type ->
                    DropdownMenuItem({Text(type)}, onClick = {
                        valType = TextFieldValue(type)
                        showTypeDropDown = false
                    })
                }
            }
        }
        Row(Modifier.constrainAs(open) {
            top.linkTo(type.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(1.dp).height(40.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                valOpen,
                {valOpen = it}
            )
            Text(stringResource(R.string.export_open), color = colorForeground)
        }
        Row(Modifier.constrainAs(label){
            bottom.linkTo(export.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(1.dp),
            horizontalArrangement = Arrangement.Center) {
            Text(valLabel)
        }
        Row(Modifier.constrainAs(export) {
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }.padding(1.dp)) {
            Button({doExport(valType.text, valName.text, valOpen)}, Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.export_do), color = colorForeground)
            }
        }
    }
}

@Preview
@Composable
fun ExportScreenPreview() {
    CloudAppTheme {
        val extensions = listOf<String>("txt", "pdf", "vcf", "ics")
        val types = listOf<String>("Notifications", "Notes", "Chats")

        ExportScreen(extensions, types, {}, {_,_,_->}, colorBackground = Color.Blue, colorForeground = Color.White)
    }
}