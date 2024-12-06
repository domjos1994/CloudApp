/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import java.io.ByteArrayOutputStream

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {
    private val calMimeType = "text/calendar"
    private val conMimeType = "text/x-vcard"

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val intent = this.intent
            val action = intent.action
            val clipData = intent.clipData
            val files = mutableListOf<FileObject>()
            if(clipData != null) {
                for(i in 0..<clipData.itemCount) {
                    val obj = this.getFileMetaData(clipData.getItemAt(i).uri)
                    if(obj != null) {
                        files.add(obj)
                    }
                }
            }

            when(action) {
                Intent.ACTION_SEND -> {Content(files)}
                Intent.ACTION_SEND_MULTIPLE -> {Content(files)}
            }
        }
    }

    @Composable
    fun Content(files: List<FileObject>) {
        CloudAppTheme {
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (header, content) = createRefs()
                PrintHeader(Modifier.constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                })

                Row(Modifier.constrainAs(content) {
                    top.linkTo(header.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }) {
                    val calFiltered = files.filter { it.mimeType == calMimeType }
                    val conFiltered = files.filter { it.mimeType == conMimeType }
                    val type = if(calFiltered.size + conFiltered.size != files.size) {
                        0
                    } else if(calFiltered.isNotEmpty() && conFiltered.isNotEmpty()) {
                        0
                    } else if(calFiltered.isNotEmpty()) {
                        1
                    } else {
                        2
                    }

                    when(type) {
                        0 -> DataShareView(files)
                        1 -> CalendarShareView(files)
                        2 -> ContactShareView(files)
                    }
                }
            }
        }
    }

    @Composable
    fun PrintHeader(modifier: Modifier) {
        Row(modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary)) {
            Text(
                stringResource(R.string.share_activity),
                fontSize = 30.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(10.dp))
        }
        HorizontalDivider(color = Color.Black)
    }

    @Composable
    fun ContactShareView(
        files: List<FileObject>,
        viewModel: ShareActivityViewModel = hiltViewModel()) {
        val contactLists by viewModel.contactLists.collectAsStateWithLifecycle()
        val success = stringResource(R.string.share_save_success)

        LogViewModel.Init(viewModel)

        LaunchedEffect(true) {
            viewModel.initContacts()
        }

        var contactList by remember { mutableStateOf(TextFieldValue("")) }
        var showDropDown by remember { mutableStateOf(false) }

        Column(Modifier.padding(5.dp)) {
            Row(Modifier.padding(5.dp)) {
                OutlinedTextField(
                    value = contactList,
                    onValueChange = {contactList = it},
                    label = {Text(stringResource(R.string.share_contact_list))},
                    trailingIcon = {
                        IconButton(onClick = {
                            showDropDown = !showDropDown
                        }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                stringResource(R.string.share_contact_list)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(showDropDown, {showDropDown = false}) {
                    contactLists.forEach { item ->
                        DropdownMenuItem(
                            text = {Text(item)},
                            onClick = {
                                contactList = TextFieldValue(item)
                                showDropDown = false
                            }
                        )
                    }
                }
            }
            Row(Modifier.padding(5.dp)) {
                val context = LocalContext.current
                Button(onClick = {viewModel.saveContact(contactList.text, files, success, context)}) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Column(Modifier.weight(1f)) {
                            Icon(Icons.Default.Save, stringResource(R.string.share_save))
                        }
                        Column(Modifier.weight(9f)) {
                            Text(stringResource(R.string.share_save))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CalendarShareView(
        files: List<FileObject>,
        viewModel: ShareActivityViewModel = hiltViewModel()) {
        val calenders by viewModel.calendars.collectAsStateWithLifecycle()
        val success = stringResource(R.string.share_save_success)

        LogViewModel.Init(viewModel)

        LaunchedEffect(true) {
            viewModel.initCalendars()
        }

        var calendar by remember { mutableStateOf(TextFieldValue("")) }
        var showDropDown by remember { mutableStateOf(false) }

        Column(Modifier.padding(5.dp)) {
            Row(Modifier.padding(5.dp)) {
                OutlinedTextField(
                    value = calendar,
                    onValueChange = {calendar = it},
                    label = {Text(stringResource(R.string.share_calendars))},
                    trailingIcon = {
                        IconButton(onClick = {
                            showDropDown = !showDropDown
                        }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                stringResource(R.string.share_calendars)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(showDropDown, {showDropDown = false}) {
                    calenders.forEach { item ->
                        DropdownMenuItem(
                            text = {Text(item)},
                            onClick = {
                                calendar = TextFieldValue(item)
                                showDropDown = false
                            }
                        )
                    }
                }
            }
            Row(Modifier.padding(5.dp)) {
                Button(onClick = {viewModel.saveCalendar(calendar.text, files, success)}) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Column(Modifier.weight(1f)) {
                            Icon(Icons.Default.Save, stringResource(R.string.share_save))
                        }
                        Column(Modifier.weight(9f)) {
                            Text(stringResource(R.string.share_save))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DataShareView(
        files: List<FileObject>,
        viewModel: ShareActivityViewModel = hiltViewModel()) {
        val items by viewModel.dataItems.collectAsStateWithLifecycle()
        val success = stringResource(R.string.share_save_success)

        LogViewModel.Init(viewModel)

        LaunchedEffect(true) {
            viewModel.initDataItems()
        }

        Column(Modifier
            .padding(5.dp)
            .verticalScroll(rememberScrollState())) {

            items.filter { it.directory }.forEach { item ->
                Row(Modifier.padding(5.dp).clickable { viewModel.loadData(item) }) {
                    Column(Modifier.weight(9f)) {
                        Text(
                            item.name,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if(item.name != "..") {
                        Column(Modifier.weight(1f)) {
                            IconButton({viewModel.saveData(item, files, success)}) {
                                Icon(Icons.Default.Save, stringResource(R.string.share_data_upload))
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }

    private fun getFileMetaData(uri: Uri): FileObject? {
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.RELATIVE_PATH
        )

        try {
            val c = contentResolver.query(uri, projection, null, null, null)
            c?.use { cursor ->
                if(cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                    val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                    val pathIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.RELATIVE_PATH)

                    val fileName = cursor.getString(nameIndex)
                    val fileSize = cursor.getLong(sizeIndex)
                    val path = cursor.getString(pathIndex)
                    val mimeType = cursor.getString(mimeTypeIndex)
                    val stream = contentResolver.openInputStream(uri)
                    val baOs = ByteArrayOutputStream()
                    var ba: ByteArray? = null
                    stream?.use { st ->
                        baOs.use { b ->
                            st.copyTo(b)
                            ba = b.toByteArray()
                        }
                    }
                    return FileObject(fileName ?: "", fileSize, mimeType ?: "", path ?: "", ba!!)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

data class FileObject(val name: String, val size: Long, val mimeType: String, val path: String, val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileObject

        if (name != other.name) return false
        if (size != other.size) return false
        if (mimeType != other.mimeType) return false
        if (path != other.path) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}