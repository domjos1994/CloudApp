package de.domjos.cloudapp.features.data.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.DataRepository
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject
import de.domjos.cloudapp.appbasics.R

@HiltViewModel
class DataViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    private val _path = MutableStateFlow("")
    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> get() = _items
    val path: StateFlow<String> get() = _path
    var message = MutableLiveData<String?>()
    var resId = MutableLiveData<Int>()

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.init()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun openFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.openFolder(item)
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun back() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.back()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
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
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun exists(item: Item): Boolean {
        return try {
            dataRepository.exists(item)
        } catch (ex: Exception) {
            message.postValue(ex.message)
            Log.e("Data-Feature-Error", ex.message, ex)
            false
        }
    }

    fun hasFolderToMove(): Boolean {
        return try {
            dataRepository.hasFolderToMove()
        } catch (ex: Exception) {
            message.postValue(ex.message)
            Log.e("Data-Feature-Error", ex.message, ex)
            false
        }
    }

    fun setFolderToMove(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.setToMove(item)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_cut_error)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun moveFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.move(item)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_cut_error)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.createFolder(name)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_add_error)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }


    fun deleteFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.delete(item)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_delete_error)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun createFile(name: String, stream: InputStream, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.createFile(name, stream)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
                onFinish()
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_add_error)
                Log.e("Data-Feature-Error", ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return dataRepository.hasAuthentications()
    }
}