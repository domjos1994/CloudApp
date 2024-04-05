package de.domjos.cloudapp.features.data.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.webdav.model.Item
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.helper.execCatch
import de.domjos.cloudapp.appbasics.helper.execCatchItem
import de.domjos.cloudapp.appbasics.helper.Separator
import de.domjos.cloudapp.appbasics.helper.Validator
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    viewModel.init()

    val context = LocalContext.current

    DataScreen(items,
        {item: Item -> execCatchItem<Item?>({
            if(it!!.directory) {
                if(it.name == "..") {
                    viewModel.back()
                } else {
                    viewModel.openFolder(it)
                }
            } else {
                viewModel.loadFile(it, context)
            }
        }, item, context)},
        { execCatch<String?>({viewModel.path.value}, context)!!},
        { item: Item -> execCatchItem<Item?, Boolean?>({viewModel.exists(it!!)}, item, context)!! },
        { item: Item -> execCatchItem<Item?>({viewModel.deleteFolder(it!!)}, item, context)},
        { data: String -> execCatchItem<String?>({viewModel.createFolder(it!!)}, data, context)},
        { item: Item -> execCatchItem({viewModel.setFolderToMove(it)}, item, context)},
        { item: Item -> execCatchItem<Item?>({viewModel.moveFolder(it!!)}, item, context)})
    { execCatch<Boolean>({viewModel.hasFolderToMove()}, context)!! }
}

@Composable
fun DataScreen(items: List<Item>, onClick: (Item) -> Unit, onPath: ()->String, onExists: (Item) -> Boolean, onDelete: (Item) -> Unit, onCreateFolder: (String) -> Unit, onSetCutElement: (Item) -> Unit, onMoveFolder: (Item)->Unit, hasCutElement: () -> Boolean) {
    var showDialog by remember { mutableStateOf(false) }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (breadCrumb, list, controls) = createRefs()

        BreadCrumb(onPath, Modifier.constrainAs(breadCrumb) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        })
        Row(Modifier.constrainAs(list) {
            top.linkTo(breadCrumb.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(controls.top)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }) {
            Column(modifier = Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState())) {
                items.forEach { item -> DataItem(item, onClick, onExists, onDelete, onSetCutElement, onMoveFolder, hasCutElement) }
            }
        }
        if(showDialog) {
            CreateFolderDialog(showDialog = {showDialog=it}, onCreateFolder)
        }
        Row(Modifier.constrainAs(controls) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
        }) {
            Column {
                Separator()
                Row {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(painterResource(R.drawable.baseline_upload_file_24), stringResource(R.string.data_file_add))
                        }
                    }
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(painterResource(R.drawable.baseline_create_new_folder_24), stringResource(R.string.data_folder_add))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreadCrumb(onPath: ()->String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Separator()
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(MaterialTheme.colorScheme.primaryContainer)) {
            Image(
                painterResource(R.drawable.baseline_arrow_forward_24),
                stringResource(R.string.data_breadcrumb),
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f),
                contentScale = ContentScale.Crop
            )

            Column(
                Modifier
                    .padding(5.dp)
                    .weight(9f)) {
                Text(
                    onPath(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }
        }
        Separator()
    }
}

@Composable
fun DataItem(item: Item, onClick: (Item) -> Unit, onExists: (Item) -> Boolean, onDelete: (Item) -> Unit, onSetCutElement: (Item) -> Unit, onMoveFolder: (Item)->Unit, hasCutElement: () -> Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick(item) }) {

        var id = R.drawable.baseline_folder_24
        if(!item.directory) {
            id = R.drawable.baseline_insert_drive_file_24

            val file = item.name.trim().lowercase()
            if(file.endsWith(".pdf")) {
                id = R.drawable.baseline_picture_as_pdf_24
            }
            if(file.endsWith(".mp4") || file.endsWith(".avi") || file.endsWith(".3gp")) {
                id = R.drawable.baseline_video_file_24
            }
            if(file.endsWith(".mp3") || file.endsWith(".wav") || file.endsWith("orf")) {
                id = R.drawable.baseline_audio_file_24
            }
            if(file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith("png") || file.endsWith(".gif") || file.endsWith(".ico")) {
                id = R.drawable.baseline_image_24
            }
        }

        Image(
            painterResource(id),
            item.name,
            modifier = Modifier
                .padding(5.dp)
                .weight(1f),
            contentScale = ContentScale.Crop
        )


        Column(modifier = Modifier
            .padding(5.dp)
            .weight(9f)) {
            Text(item.name, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp))
        }
        Column(
            Modifier
                .padding(5.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(item.name != "..") {
                IconButton(onClick = { onSetCutElement(item) }) {
                    Icon(
                        painterResource(R.drawable.baseline_content_cut_24),
                        stringResource(R.string.data_folder_cut)
                    )
                }
            }
        }
        Column(
            Modifier
                .padding(5.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(item.name != "..") {
                IconButton(onClick = { onMoveFolder(item) }, enabled = hasCutElement()) {
                    Icon(
                        painterResource(R.drawable.baseline_content_paste_24),
                        stringResource(R.string.data_folder_cut)
                    )
                }
            }
        }
        Column(modifier = Modifier
            .padding(5.dp)
            .weight(1f)) {
            if(item.name != "..") {
                IconButton(onClick = { onDelete(item) }) {
                    Icon(Icons.Rounded.Delete, stringResource(R.string.calendar_delete))
                }
            }
        }
        Column(modifier = Modifier
            .padding(5.dp)
            .weight(1f)) {
            if(onExists(item)) {
                Image(
                    painterResource(R.drawable.baseline_sync_24),
                    item.name,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Composable
fun CreateFolderDialog(showDialog: (Boolean) -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var isNameValid by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { showDialog(false) }) {
        Surface(
            Modifier
                .padding(5.dp)
                .wrapContentHeight()) {
            Column {
                Row(
                    Modifier
                        .padding(5.dp)
                        .wrapContentHeight()) {
                    Column(
                        Modifier.weight(2f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                isNameValid = Validator.check(false, 2, 255, it.text)
                            },
                            label = {Text(stringResource(R.string.data_folder_name))},
                            isError = !isNameValid
                        )
                    }
                }
                Row(
                    Modifier
                        .padding(5.dp)
                        .wrapContentHeight()) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { showDialog(false) }) {
                            Icon(Icons.Rounded.Clear, stringResource(R.string.login_close))
                        }
                    }
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = {
                            onSave(name.text)
                            showDialog(false)
                        }, enabled = isNameValid) {
                            Icon(Icons.Rounded.Check, stringResource(R.string.login_close))
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    DataScreen(listOf(fake(1L), fake(2L), fake(3L)), {}, {"cxgygf"}, {true}, {}, {}, {}, {}) {true}
}

@Preview(showBackground = true)
@Composable
fun DataItemPreview() {
    DataItem(fake(1L), {}, {true}, {}, {}, {}) {true}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CreateFolderDialogPreview() {
    CloudAppTheme {
        CreateFolderDialog({}) {}
    }
}


fun fake(id: Long): Item {
    return Item("Test $id", true, "Test", "")
}