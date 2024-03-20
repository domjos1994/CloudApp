package de.domjos.cloudapp.features.data.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.webdav.model.Item
import de.domjos.cloudapp.appbasics.R

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    viewModel.init()

    val context = LocalContext.current

    DataScreen(items, {
        if(it.directory) {
            if(it.name == "..") {
                viewModel.back()
            } else {
                viewModel.openFolder(it)
            }
        } else {
            viewModel.loadFile(it, context)
        }
    }, {viewModel.path.value}) {viewModel.exists(it)}
}

@Composable
fun DataScreen(items: List<Item>, onClick: (Item) -> Unit, onPath: ()->String, onExists: (Item) -> Boolean) {
    Column(Modifier.fillMaxSize()) {
        BreadCrumb(onPath)
        Row {
            Column(modifier = Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState())) {
                items.forEach { item -> DataItem(item, onClick, onExists) }
            }
        }
    }
}

@Composable
fun BreadCrumb(onPath: ()->String) {
    Row(Modifier.fillMaxWidth().height(1.dp).background(Color.Black)) {}
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

        Column(Modifier.padding(5.dp).weight(9f)) {
            Text(
                onPath(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
    Row(Modifier.fillMaxWidth().height(1.dp).background(Color.Black)) {}
}

@Composable
fun DataItem(item: Item, onClick: (Item) -> Unit, onExists: (Item) -> Boolean) {
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

@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    DataScreen(listOf(fake(1L), fake(2L), fake(3L)), {}, {"cxgygf"}) {true}
}

@Preview(showBackground = true)
@Composable
fun DataItemPreview() {
    DataItem(fake(1L), {}) {true}
}

fun fake(id: Long): Item {
    return Item("Test $id", true, "Test", "")
}