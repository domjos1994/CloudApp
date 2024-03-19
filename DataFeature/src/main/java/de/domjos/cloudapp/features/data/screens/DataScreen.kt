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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.webdav.model.Item

@Composable
fun DataScreen(viewModel: DataViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    viewModel.init()

    DataScreen(items) {
        if(it.directory) {
            if(it.name == "..") {
                viewModel.back()
            } else {
                viewModel.openFolder(it)
            }
        } else {
            viewModel.loadFile(it, "")
        }
    }
}

@Composable
fun DataScreen(items: List<Item>, onClick: (Item) -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .verticalScroll(rememberScrollState())) {

        items.forEach { item -> DataItem(item, onClick) }
    }
}

@Composable
fun DataItem(item: Item, onClick: (Item) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick(item) }) {
        Image(
            Icons.Outlined.AccountBox,
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
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Preview(showBackground = true)
@Composable
fun DataScreenPreview() {
    DataScreen(listOf(fake(1L), fake(2L), fake(3L))) {}
}

@Preview(showBackground = true)
@Composable
fun DataItemPreview() {
    DataItem(fake(1L)) {}
}

fun fake(id: Long): Item {
    return Item("Test $id", true, "Test", "")
}