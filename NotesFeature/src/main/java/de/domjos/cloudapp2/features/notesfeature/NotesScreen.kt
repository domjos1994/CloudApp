package de.domjos.cloudapp2.features.notesfeature

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoEntryItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(), toAuths: () -> Unit, colorBackground: Color, colorForeground: Color
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val connection by connectivityState()
    val isConnected = connection === ConnectionState.Available

    if(isConnected) {
        viewModel.reload()
    }

    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    NotesScreen(notes,
        onSaveClick = {viewModel.save(it)},
        onDeleteClick = {viewModel.delete(it)},
        hasAuths = viewModel.hasAuthentications(),
        toAuths = toAuths,
        isConnected = isConnected,
        colorBackground = colorBackground,
        colorForeground = colorForeground
    )
}


@Composable
fun NotesScreen(
    items: List<Note>,
    onSaveClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    hasAuths: Boolean,
    toAuths: () -> Unit,
    isConnected: Boolean,
    colorBackground: Color,
    colorForeground: Color) {



    val showDialog =  remember { mutableStateOf(false) }
    val showBottomSheet = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<Note?>(null) }

    if(showDialog.value) {
        NotesDialog(
            note = selectedItem.value,
            setShowDialog = { showDialog.value = it },
            {
                selectedItem.value = it
                onSaveClick(selectedItem.value!!)
                showDialog.value = false
            },
            {
                selectedItem.value = null
                onDeleteClick(it)
                showDialog.value = false
            }
        )
    }
    if(showBottomSheet.value) {
        NotesBottomSheet(selectedItem.value) {
            showBottomSheet.value = it
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (list, control) = createRefs()

        Column(modifier = Modifier
            .constrainAs(list) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            }
            .padding(5.dp)
            .verticalScroll(rememberScrollState())) {
            NotesList(
                items,
                isConnected,
                hasAuths,
                toAuths,
                {
                    selectedItem.value = it
                    showDialog.value = true
                },
                {
                    selectedItem.value = it
                    showBottomSheet.value = true
                },
                colorBackground,
                colorForeground
            )
        }
        if(isConnected) {
            FloatingActionButton(
                onClick = {
                    selectedItem.value = Note(0, "", "", "", false, 0)
                    showDialog.value = true
                },
                modifier = Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .padding(5.dp), containerColor = colorBackground) {
                Icon(Icons.Filled.Add, stringResource(R.string.login_add), tint = colorForeground)
            }
        }
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    isConnected: Boolean,
    hasAuths: Boolean,
    toAuths: () -> Unit,
    onDialog: (Note) -> Unit,
    onBottomSheet: (Note) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {

    if(hasAuths) {
        if(isConnected) {
            if(notes.isEmpty()) {
                Column {
                    NoEntryItem(colorForeground, colorBackground)
                }
            } else {
                notes.forEach { item ->
                    NotesItem(item, onDialog, onBottomSheet, colorBackground, colorForeground)
                }
            }
        } else {
            Column {
                NoInternetItem(colorForeground, colorBackground)
            }
        }
    } else {
        Column {
            NoAuthenticationItem(colorForeground, colorBackground, toAuths)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesItem(note: Note, onDialog: (Note) -> Unit, onBottomSheet: (Note) -> Unit, colorBackground: Color, colorForeground: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorBackground)
            .combinedClickable(
                onClick = { onBottomSheet(note) },
                onLongClick = { onDialog(note) }
            )
            .height(60.dp)) {
        Column(
            Modifier
                .weight(3f)
                .height(60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(R.drawable.baseline_note_24),
                contentDescription = note.title,
                colorFilter = ColorFilter.tint(colorForeground))
        }
        Column(
            Modifier
                .weight(17f)
                .height(60.dp)
                .padding(5.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(25.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorForeground)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)) {}
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if(note.category != "") {
                    Column(Modifier.weight(2f)) {
                        Image(
                            painterResource(R.drawable.baseline_category_24),
                            contentDescription = "Category ${note.category}",
                            colorFilter = ColorFilter.tint(colorForeground))
                    }
                    Column(Modifier.weight(28f)) {
                        Text(note.category, fontWeight = FontWeight.Normal, fontSize = 14.sp, color = colorForeground)
                    }
                }
            }
        }
    }
    Separator(colorForeground)
}

@Composable
fun NotesDialog(
    note: Note?,
    setShowDialog: (Boolean) -> Unit,
    onSaveClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit) {

    var id by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var isValidTitle by remember { mutableStateOf(note?.title?.isNotEmpty() ?: false) }
    var category by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isValidContent by remember { mutableStateOf(note?.content?.isNotEmpty() ?: false) }
    var favorite by remember { mutableStateOf(false) }
    var readOnly by remember { mutableStateOf(false) }

    if(note != null) {
        id = note.id
        title = note.title
        category = note.category
        content = note.content
        favorite = note.favorite
        readOnly = note.readonly
    }

    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            isValidTitle = Validator.check(false, 3, 255, it)
                        },
                        label = {Text(stringResource(id = R.string.notes_title))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidTitle,
                        enabled = !readOnly
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                        },
                        label = {Text(stringResource(id = R.string.notes_category))},
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !readOnly
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = content,
                        onValueChange = {
                            content = it
                            isValidContent = Validator.check(false, 3, 50000, it)
                        },
                        label = {Text(stringResource(id = R.string.notes_content))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidContent,
                        enabled = !readOnly
                    )
                }
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 2.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primaryContainer)) {
                    Markdown(
                        content = content,
                        modifier = Modifier.padding(2.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = favorite,
                        onCheckedChange = {
                            favorite = it
                        },
                        enabled = !readOnly
                    )
                    Text(stringResource(R.string.notes_favorite))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        if(id != 0) {
                            IconButton(onClick = {
                                val tmp = Note(id, content, title, category, favorite, 0)
                                onDeleteClick(tmp)
                            }) {
                                Icon(Icons.Filled.Delete, "Delete $title")
                            }
                        }
                    }
                    Column(Modifier.weight(7f)) {}
                    Column(Modifier.weight(1f)) {
                        IconButton(onClick = {
                            setShowDialog(false)
                        }) {
                            Icon(Icons.Filled.Close, "Cancel $title")
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        IconButton(onClick = {
                            val tmp = Note(id, content, title, category, favorite, 0)
                            onSaveClick(tmp)
                        }, enabled = isValidTitle && isValidContent) {
                            Icon(Icons.Filled.Done, "Save $title")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesBottomSheet(note: Note?, setShowSheet: (Boolean) -> Unit) {
    if(note != null) {
        ModalBottomSheet(
            onDismissRequest = { setShowSheet(false) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)) {

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {

                    Text(note.title, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {

                    Markdown(note.content)
                }
            }
        }
    }
}

@Composable
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun NotesScreenPreview() {
    CloudAppTheme {
        val items = listOf(fake(1), fake(2), fake(3))

        NotesScreen(items, {}, {}, false, {}, true, colorBackground = Color.Blue, colorForeground = Color.White)
    }
}


@Composable
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun NotesItemPreview() {
    CloudAppTheme {
        NotesItem(fake(1), {}, {}, colorBackground = Color.Blue, colorForeground = Color.White)
    }
}

@Composable
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun NotesDialogPreview() {
    CloudAppTheme {
        NotesDialog(fake(1), {}, {}, {})
    }
}

fun fake(id: Int): Note {
    return Note(
        id, "Hello, this is a very long test $id",
        "This is a test! $id", "test $id", false, 0
    )
}