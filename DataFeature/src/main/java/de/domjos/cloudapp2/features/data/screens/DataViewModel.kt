package de.domjos.cloudapp2.features.data.screens

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.DataRepository
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.ConnectivityViewModel
import de.domjos.cloudapp2.data.Settings
import de.domjos.cloudapp2.rest.model.shares.InsertShare
import de.domjos.cloudapp2.rest.model.shares.Share
import de.domjos.cloudapp2.rest.model.shares.Types
import de.domjos.cloudapp2.rest.model.shares.UpdateShare
import de.schnettler.datastore.manager.DataStoreManager
import de.schnettler.datastore.manager.PreferenceRequest

@HiltViewModel
class DataViewModel @Inject constructor(
    private val dataRepository: DataRepository,
    private val settings: Settings
) : ConnectivityViewModel() {
    private val _path = MutableStateFlow("")
    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> get() = _items
    private val _item = MutableStateFlow<Item?>(null)
    val item: StateFlow<Item?> get() = _item
    val path: StateFlow<String> get() = _path

    private val _shareItems = MutableStateFlow<List<String>>(listOf())

    override fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.init()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun exists(item: Item): Boolean {
        return try {
            dataRepository.exists(item)
        } catch (ex: Exception) {
            printException(ex, this)
            false
        }
    }

    fun openElement(item: Item, onFinish: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var path = ""
            try {
                _item.value = item
                if(item.directory) {
                    if(item.name == "..") {
                        dataRepository.back()
                    } else {
                        dataRepository.openFolder(item)
                    }
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                } else {
                    val dir = dataRepository.createDirs()
                    if(!exists(item)) {
                        dataRepository.openResource(item, dir)
                    }
                    path = "$dir/${item.name.trim().replace(" ", "_")}"
                }
            } catch (ex: Exception) {
                printException(ex, this)
            } finally {
                onFinish(path)
            }
        }
    }

    fun loadElement(path: String, item: Item, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.openFile(path, item, context)
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun hasFolderToMove(): Boolean {
        return try {
            dataRepository.hasFolderToMove()
        } catch (ex: Exception) {
            printException(ex, this)
            false
        }
    }

    fun getFolderToMove(): String {
        return dataRepository.getFolderToMove()
    }

    fun setFolderToMove(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.setToMove(item)
                dataRepository.reload()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                printException(ex, R.string.data_element_cut_error, this)
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
                printException(ex, R.string.data_element_cut_error, this)
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
                printException(ex, R.string.data_element_add_error, this)
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
                printException(ex, R.string.data_element_delete_error, this)
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
                printException(ex, R.string.data_element_add_error, this)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return dataRepository.hasAuthentications()
    }

    suspend fun <T> getSetting(key: Preferences.Key<T>, default: T): T {
        val manager = DataStoreManager(settings.getStore())
        return manager.getPreference(PreferenceRequest(key, default))
    }

    fun insertShare(share: InsertShare, onFinish: (Share?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.insertShare(share).collect { share ->
                    if(share != null) {
                        onFinish(share)
                        if(share.url != "") {
                            printMessage(R.string.data_shared_copied, this@DataViewModel)
                        }
                    }
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteShare(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.deleteShare(id).collect { state ->
                    if(state != "") {
                        printMessage(state, this@DataViewModel)
                    }
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun updateShare(id: Int, share: UpdateShare, onFinish: (Share?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.updateShare(id, share).collect { share ->
                    if(share != null) {
                        onFinish(share)
                    }
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun autoComplete(text: String, shareType: Types): List<String> {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(text!="") {
                    dataRepository.getAutocompleteItems(text, shareType).collect {
                        _shareItems.value = it
                    }
                } else {
                    _shareItems.value = listOf()
                }
            } catch (ex: Exception) {
                printException(ex, this)
                _shareItems.value = listOf()
            }
        }
        return _shareItems.value
    }
}