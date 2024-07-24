package de.domjos.cloudapp2.features.data.screens

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
) : ViewModel() {
    private val _path = MutableStateFlow("")
    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> get() = _items
    private val _item = MutableStateFlow<Item?>(null)
    val item: StateFlow<Item?> get() = _item
    val path: StateFlow<String> get() = _path
    var message = MutableLiveData<String?>()
    var resId = MutableLiveData<Int>()

    private val _shareItems = MutableStateFlow<List<String>>(listOf())

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.init()
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun exists(item: Item): Boolean {
        return try {
            dataRepository.exists(item)
        } catch (ex: Exception) {
            message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
            false
        }
    }

    fun openElement(item: Item, onFinish: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _item.value = item
                var path = ""
                if(item.directory) {
                    if(item.name == "..") {
                        dataRepository.back()
                        _items.value = dataRepository.getFromDatabase()
                        _items.value = dataRepository.getList()
                        _path.value = dataRepository.path
                    } else {
                        dataRepository.openFolder(item)
                        _items.value = dataRepository.getFromDatabase()
                        _items.value = dataRepository.getList()
                        _path.value = dataRepository.path
                    }
                } else {
                    val dir = dataRepository.createDirs()
                    if(!exists(item)) {
                        dataRepository.openResource(item, dir)
                    }
                    path = "$dir/${item.name.trim().replace(" ", "_")}"
                }
                onFinish(path)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun loadElement(path: String, item: Item, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.openFile(path, item, context)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasFolderToMove(): Boolean {
        return try {
            dataRepository.hasFolderToMove()
        } catch (ex: Exception) {
            message.postValue(ex.message)
            Log.e(this.javaClass.name, ex.message, ex)
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
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_cut_error)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun moveFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.move(item)
                dataRepository.reload()
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_cut_error)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.createFolder(name)
                dataRepository.reload()
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_add_error)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }


    fun deleteFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.delete(item)
                dataRepository.reload()
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_delete_error)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun createFile(name: String, stream: InputStream, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.createFile(name, stream)
                dataRepository.reload()
                _items.value = dataRepository.getFromDatabase()
                _items.value = dataRepository.getList()
                _path.value = dataRepository.path
                onFinish()
            } catch (ex: Exception) {
                resId.postValue(R.string.data_element_add_error)
                Log.e(this.javaClass.name, ex.message, ex)
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
                            resId.postValue(R.string.data_shared_copied)
                        }
                    }
                    _items.value = dataRepository.getFromDatabase()
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
                        message.postValue(state)
                    }
                    _items.value = dataRepository.getFromDatabase()
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
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
                    _items.value = dataRepository.getFromDatabase()
                    _items.value = dataRepository.getList()
                    _path.value = dataRepository.path
                }
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
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
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
                _shareItems.value = listOf()
            }
        }
        return _shareItems.value
    }

}