package de.domjos.cloudapp2.features.data.screens

import android.content.Context
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.mikepenz.markdown.m3.Markdown
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.LoadingDialog
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val parentItem by viewModel.item.collectAsStateWithLifecycle()

    val connection by connectivityState()
    val isConnected = connection === ConnectionState.Available
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showFile by remember { mutableStateOf(false) }
    var path by remember { mutableStateOf("") }
    var currentItem by remember { mutableStateOf<Item?>(null) }

    if(isConnected) {
        viewModel.init()
    }

    viewModel.message.observe(LocalLifecycleOwner.current) { msg ->
        msg?.let {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }
    viewModel.resId.observe(LocalLifecycleOwner.current) { res ->
        res?.let {
            Toast.makeText(context, res, Toast.LENGTH_LONG).show()
            viewModel.resId.value = null
        }
    }

    if(showFile) {
        ShowFile(path, {showFile = it}, {
            viewModel.loadElement(it, currentItem!!, context)
        }, viewModel)
    }

    if(showDialog) {
        LoadingDialog { showDialog = it }
    }

    DataScreen(items, parentItem, isConnected, viewModel.hasAuthentications(), toAuths, colorBackground, colorForeground,
        {item: Item, o: () -> Unit ->
            showDialog = true
            viewModel.openElement(item) {
                path = it
                currentItem = item
                if(!item.directory) {
                    showFile = true
                }
                showDialog = false
                o()
            }
        },
        { viewModel.path.value},
        { item: Item -> viewModel.exists(item) },
        { item: Item -> viewModel.deleteFolder(item)},
        { data: String -> viewModel.createFolder(data)},
        { item: Item -> viewModel.setFolderToMove(item)},
        { item: Item -> viewModel.moveFolder(item)},
        {viewModel.hasFolderToMove()}, {viewModel.getFolderToMove()})
    {name, stream ->
        showDialog = true
        viewModel.createFile(name, stream) { showDialog = false }
    }
}

@Composable
fun DataScreen(items: List<Item>, parentItem: Item?, isConnected: Boolean, hasAuths: Boolean, toAuths: () -> Unit, colorBackground: Color, colorForeground: Color, onClick: (Item, () -> Unit) -> Unit, onPath: ()->String, onExists: (Item) -> Boolean, onDelete: (Item) -> Unit, onCreateFolder: (String) -> Unit, onSetCutElement: (Item) -> Unit, onMoveFolder: (Item)->Unit, hasCutElement: () -> Boolean, getCutElement: () -> String, uploadFile: (String, InputStream) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val can_edit = parentItem?.share?.can_edit ?: true

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (breadCrumb, list, controls) = createRefs()

        BreadCrumb(onPath, colorBackground, colorForeground, Modifier.constrainAs(breadCrumb) {
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
                var hasCut by remember { mutableStateOf(hasCutElement()) }
                var getCut by remember { mutableStateOf(getCutElement()) }
                var download by remember { mutableStateOf(false) }

                if(hasAuths) {
                    if(isConnected) {

                        items.forEach { item -> DataItem(item, parentItem, colorBackground, colorForeground, onClick, {
                            download = onExists(it)
                            download
                        }, onDelete, {
                            onSetCutElement(it)
                            hasCut = true
                            getCut = it.path
                        }, {
                            onMoveFolder(it)
                            hasCut = false
                            getCut = ""
                        }, hasCut, getCut)}
                    } else {
                        NoInternetItem(colorForeground, colorBackground)
                    }
                } else {
                    NoAuthenticationItem(colorForeground, colorBackground, toAuths)
                }
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
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {item ->
                if(item != null) {
                    val inputStream = context.contentResolver.openInputStream(item)
                    if(inputStream!=null) {
                        uploadFile(getFileName(item, context)!!, inputStream)
                    }
                }
            }
            Column {
                if(isConnected && hasAuths && can_edit) {
                    Separator(colorBackground)
                    Row {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                launcher.launch(arrayOf("text/*", "image/*", "audio/*", "video/*", "application/*"))
                            }) {
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
}

fun getFileName(uri: Uri, context: Context): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
        cursor.use { curs ->
            if (curs.moveToFirst()) {
                val index = curs.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index >= 0) {
                    result = curs.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result!!.substring(cut + 1)
        }
    }
    return result
}

@Composable
fun BreadCrumb(onPath: ()->String, colorBackground: Color, colorForeground: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Separator(colorForeground)
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(colorBackground)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                stringResource(R.string.data_breadcrumb),
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f),
                tint = colorForeground
            )

            Column(
                Modifier
                    .padding(5.dp)
                    .weight(9f)) {
                Text(
                    onPath(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = colorForeground
                )
            }
        }
        Separator(colorForeground)
    }
}

@Composable
fun DataItem(item: Item, parentItem: Item?, colorBackground: Color, colorForeground: Color, onClick: (Item, () -> Unit) -> Unit, onExists: (Item) -> Boolean, onDelete: (Item) -> Unit, onSetCutElement: (Item) -> Unit, onMoveFolder: (Item)->Unit, hasCutElement: Boolean, cutPath: String) {
    var downloaded by remember { mutableStateOf(item.exists) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val canDelete = parentItem?.share?.can_delete ?: true

    if(showDeleteDialog) {
        ShowDeleteDialog(onShowDialog = {showDeleteDialog = it}, {onDelete(item)})
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colorBackground)
            .clickable {
                onClick(item) {
                    if (!item.directory && onExists(item)) {
                        downloaded = true
                    }
                }
            }) {

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
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(colorForeground)
        )


        Column(modifier = Modifier
            .padding(5.dp)
            .weight(9f)) {
            Text(item.name, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp), color = colorForeground)
        }
        Column(
            Modifier
                .padding(5.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(item.share != null) {
                val context = LocalContext.current
                val msg = stringResource(id = R.string.data_shared_toast)
                IconButton(onClick = {
                    val text = String.format(msg, item.name, item.share?.displayname_owner)
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground)) {
                    if(cutPath != item.path) {
                        Icon(
                            Icons.Rounded.AccountBox,
                            stringResource(R.string.data_shared),
                            tint = colorForeground
                        )
                    }
                }
            }
        }
        Column(
            Modifier
                .padding(5.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(item.name != ".." && item.share == null) {
                IconButton(onClick = {
                    onSetCutElement(item)
                },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground)) {
                    if(cutPath != item.path) {
                        Icon(
                            painterResource(R.drawable.baseline_content_cut_24),
                            stringResource(R.string.data_folder_cut),
                            tint = colorForeground
                        )
                    }
                }
            }
        }
        Column(
            Modifier
                .padding(5.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(item.name != ".." && hasCutElement) {
                IconButton(onClick = {
                    onMoveFolder(item)
                },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground)) {
                    Icon(
                        painterResource(R.drawable.baseline_content_paste_24),
                        stringResource(R.string.data_folder_cut),
                        tint = colorForeground
                    )
                }
            }
        }
        Column(modifier = Modifier
            .padding(5.dp)
            .weight(1f)) {
            if(item.name != ".." && canDelete) {
                if(item.share != null) {
                    if(item.share!!.can_delete) {
                        IconButton(onClick = { showDeleteDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground)) {
                            Icon(Icons.Rounded.Delete, stringResource(R.string.calendar_delete), tint = colorForeground)
                        }
                    }
                } else {
                    IconButton(onClick = { showDeleteDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground)) {
                        Icon(Icons.Rounded.Delete, stringResource(R.string.calendar_delete), tint = colorForeground)
                    }
                }
            }
        }
        Column(modifier = Modifier
            .padding(5.dp)
            .weight(1f)) {
            if(downloaded) {
                Image(
                    painterResource(R.drawable.baseline_sync_24),
                    item.name,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(colorForeground)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowFile(path: String, onShowFile: (Boolean) -> Unit, loadUnknownFile: (String) -> Unit, viewModel: DataViewModel) {
    val file = File(path)
    val pdf by produceState(initialValue = true) {
        this.value = viewModel.getSetting(Settings.dataShowPdfInInternalViewer, true)
    }
    val img by produceState(initialValue = true) {
        this.value = viewModel.getSetting(Settings.dataShowImageInInternalViewer, true)
    }
    val txt by produceState(initialValue = true) {
        this.value = viewModel.getSetting(Settings.dataShowTextInInternalViewer, true)
    }
    val md by produceState(initialValue = true) {
        this.value = viewModel.getSetting(Settings.dataShowMarkDownInInternalViewer, true)
    }
    val all by produceState(initialValue = true) {
        this.value = viewModel.getSetting(Settings.dataShowInInternalViewer, true)
    }


    when(file.extension.lowercase()) {
        "pdf" -> {
            if(pdf && all) {
                ModalBottomSheet(onDismissRequest = { onShowFile(false) }) {PdfViewer(path = path)}
            } else {
                loadUnknownFile(path)
            }
        }
        "png", "jpg", "jpeg", "gif", "svg" -> {
            if(img && all) {
                ModalBottomSheet(onDismissRequest = { onShowFile(false) }) { ImageViewer(path = path) }
            } else {
                loadUnknownFile(path)
            }
        }
        "txt", "csv", "rtf", "xml" -> {
            if(txt && all) {
                ModalBottomSheet(onDismissRequest = { onShowFile(false) }) { FileViewer(path = path) }
            } else {
                loadUnknownFile(path)
            }
        }
        "md" -> {
            if(md && all) {
                ModalBottomSheet(onDismissRequest = { onShowFile(false) }) { MarkDownViewer(path = path) }
            } else {
                loadUnknownFile(path)
            }
        }
        else -> loadUnknownFile(path)
    }
}

@Composable
fun MarkDownViewer(path: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)) {
        val file = File(path)
        val fis = FileInputStream(file)
        var result = ""
        var current: Char
        while (fis.available() > 0) {
            current = fis.read().toChar()
            result += current.toString()
        }
        fis.close()
        Markdown(result)
    }
}

@Composable
fun FileViewer(path: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)) {
        val file = File(path)
        val fis = FileInputStream(file)
        var result = ""
        var current: Char
        while (fis.available() > 0) {
            current = fis.read().toChar()
            result += current.toString()
        }
        fis.close()
        Text(result)
    }
}

@Composable
fun ImageViewer(path: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)) {
        val image = File(path)
        val bmOptions = BitmapFactory.Options()
        var bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
        bitmap = Bitmap.createScaledBitmap(bitmap!!, bitmap.width, bitmap.height, true)
        Image(bitmap.asImageBitmap(), path)
    }
}

@Composable
fun PdfViewer(path: String) {
    var currentPage by remember { mutableIntStateOf(0) }
    var maxPage by remember { mutableIntStateOf(0) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var bitmap by remember { mutableStateOf(
        Bitmap.createBitmap(1920,1080, Bitmap.Config.ARGB_8888)
    ) }
    var page by remember { mutableStateOf<PdfRenderer.Page?>(null) }
    val updatePage = {index: Int ->
        if(maxPage != 0) {
            currentPage = index
        }
        if(page != null) {
            page!!.close()
        }
        page = renderer!!.openPage(currentPage)
        bitmap = Bitmap.createBitmap(page!!.width, page!!.height, Bitmap.Config.ARGB_8888)
        page!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    }

    try {
        if(path.isNotEmpty()) {
            val descriptor = ParcelFileDescriptor.open(
                File(path),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
            renderer = PdfRenderer(descriptor)
            maxPage = renderer!!.pageCount
            updatePage(0)
        }
    } catch (ex: Exception) {
        Toast.makeText(LocalContext.current, ex.message, Toast.LENGTH_LONG).show()
    }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)) {
        Column(
            Modifier
                .weight(4f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            IconButton(onClick = {
                if(currentPage - 1 != -1) {
                    updatePage(currentPage - 1)
                }
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, (currentPage - 1).toString())
            }
        }
        Column(
            Modifier
                .weight(4f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text("${currentPage + 1} / $maxPage")
        }
        Column(
            Modifier
                .weight(4f)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            IconButton(onClick = {
                if(currentPage != maxPage - 1) {
                    updatePage(currentPage + 1)
                }
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, (currentPage + 1).toString())
            }
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Image(bitmap.asImageBitmap(), path,
                modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DataScreenPreview() {
    CloudAppTheme {
        DataScreen(
            listOf(fake(1L), fake(2L), fake(3L)),
            null,
            isConnected = true,
            hasAuths = true,
            colorForeground = Color.White,
            colorBackground = Color.Blue,
            toAuths = {},
            onClick = {_,_->},
            onPath = {"cxgygf"},
            onExists = {true},
            onDelete = {},
            onCreateFolder = {},
            onSetCutElement = {},
            onMoveFolder = {},
            hasCutElement = {true},
            getCutElement = {""}) { _, _->}
    }
}

@Preview(showBackground = true)
@Composable
fun DataItemPreview() {
    DataItem(fake(1L), null,
        Color.Blue,
        Color.White, {_,_->}, {true}, {}, {}, {}, true, "")
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