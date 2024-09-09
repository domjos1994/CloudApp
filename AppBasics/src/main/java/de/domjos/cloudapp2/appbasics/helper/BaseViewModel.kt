/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.helper

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * ViewModel with helper methods to Log in the UI and the logger
 * @author Dominic Joas
 */
open class LogViewModel : ViewModel() {
    val message = MutableLiveData<String>()
    val resId = MutableLiveData<Int>()

    /**
     * Print an exception to the ui
     * @param ex the Exception
     * @param item the Class in which the Exception happens
     */
    protected fun printException(ex: Exception, item: Any) {
        Log.e(item.javaClass.name, ex.message, ex)
        this.message.postValue(ex.message)
    }

    /**
     * Print an exception to the ui
     * @param ex the Exception
     * @param resId the res id of the string
     * @param item the Class in which the Exception happens
     */
    protected fun printException(ex: Exception, resId: Int, item: Any) {
        Log.e(item.javaClass.name, ex.message, ex)
        this.resId.postValue(resId)
    }

    /**
     * Print an message to the ui
     * @param msg the Message
     * @param item the Class in which the message happens
     */
    protected fun printMessage(msg: String, item: Any) {
        Log.d(item.javaClass.name, msg)
        this.message.postValue(msg)
    }

    /**
     * Print an message to the ui
     * @param resId the res id of the string
     * @param item the Class in which the message happens
     */
    protected fun printMessage(resId: Int, item: Any) {
        Log.d(item.javaClass.name, "Post $resId")
        this.resId.postValue(resId)
    }

    companion object {

        /**
         * Initialize the Observer for the message and print an toast
         */
        @Composable
        fun Init(lvm: ViewModel, onPreMessage: (String) -> Unit = {}) {
            if(lvm is LogViewModel) {
                val context = LocalContext.current
                lvm.message.observe(LocalLifecycleOwner.current) {
                    if(it != null) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                        onPreMessage(it)
                        lvm.message.value = null
                    }
                }
                lvm.resId.observe(LocalLifecycleOwner.current) { res ->
                    res?.let {
                        Toast.makeText(context, res, Toast.LENGTH_LONG).show()
                        lvm.resId.value = null
                    }
                }
            }
        }
    }
}

abstract class ConnectivityViewModel: LogViewModel() {

    protected abstract fun init()

    fun isConnected(): Boolean {
        return IsConnected
    }

    companion object {
        protected var IsConnected: Boolean = false

        @OptIn(ExperimentalCoroutinesApi::class)
        @Composable
        fun Init(vm: ViewModel, onPreMessage: (String) -> Unit = {}) {
            val connection by connectivityState()
            IsConnected = connection === ConnectionState.Available
            LogViewModel.Init(vm, onPreMessage)

            LaunchedEffect(IsConnected) {
                (vm as ConnectivityViewModel).init()
            }
        }
    }
}