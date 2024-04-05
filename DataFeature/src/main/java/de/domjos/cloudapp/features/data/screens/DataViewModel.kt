package de.domjos.cloudapp.features.data.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.DataRepository
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    private val _path = MutableStateFlow("")
    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> get() = _items
    val path: StateFlow<String> get() = _path

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.init()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun openFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.openFolder(item)
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun back() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.back()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun loadFile(item: Item, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = dataRepository.createDirs()
                if(!exists(item)) {
                    dataRepository.openResource(item, dir)
                }
                dataRepository.openFile("$dir/${item.name.trim().replace(" ", "_")}", item, context)
            } catch (_: Exception) {}
        }
    }

    fun exists(item: Item): Boolean {
        return dataRepository.exists(item)
    }

    fun hasFolderToMove(): Boolean {
        return dataRepository.hasFolderToMove()
    }

    fun setFolderToMove(item: Item) {
        this.dataRepository.setFolderToMove(item)
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.reload()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun moveFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.moveFolder(item)
            dataRepository.reload()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.createFolder(name)
            dataRepository.reload()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }

    fun deleteFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.deleteFolder(item)
            dataRepository.reload()
            _items.value = dataRepository.getList()
            _path.value = dataRepository.path
        }
    }
}