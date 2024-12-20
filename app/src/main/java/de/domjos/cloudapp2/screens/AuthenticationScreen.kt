package de.domjos.cloudapp2.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.domjos.cloudapp2.adapter.deleteSyncAccount
import de.domjos.cloudapp2.adapter.getOrCreateSyncAccount
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.NoEntryItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.custom.OutlinedPasswordField
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
fun AuthenticationScreen(
    viewModel: AuthenticationViewModel = hiltViewModel(),
    colorBackground: Color,
    colorForeground: Color,
    onSelectedChange: (Authentication) -> Unit) {

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val authentications by produceState<AuthenticationUiState>(
        initialValue = AuthenticationUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    if(authentications is AuthenticationUiState.Error) {
        val error = authentications as AuthenticationUiState.Error
        Row {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.throwable.message!!, color = colorForeground)
            }
        }
    }

    val context = LocalContext.current

    viewModel.message.observe(LocalLifecycleOwner.current) { msg ->
        msg?.let {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }
    viewModel.resId.observe(LocalLifecycleOwner.current) { msg ->
        msg?.let {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.resId.value = null
        }
    }


    if (
        authentications is AuthenticationUiState.Success) {
        val msg = stringResource(id = R.string.validate_auth)
        val auths = (authentications as AuthenticationUiState.Success).data

        AuthenticationScreen(
            onSaveClick = {auth ->
                if(auth.id == 0L) {
                    viewModel.insertAuthentication(auth, msg) {
                        if(auths.isEmpty()) {
                            auth.id = it
                            viewModel.checkAuthentications(auth)
                            onSelectedChange(auth)
                            getOrCreateSyncAccount(context, auth)
                        }
                    }
                } else {
                    viewModel.updateAuthentication(auth, msg)
                }
            },
            onDeleteClick = { auth ->
                deleteSyncAccount(context, auth)
                viewModel.deleteAuthentication(auth)
            },
            onConnectionCheck = {auth, onSuccess ->
                viewModel.checkConnection(auth, onSuccess) },
            select = { auth -> viewModel.checkAuthentications(auth)
                onSelectedChange(auth)},
            auths,
            colorBackground, colorForeground
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AuthenticationScreen(
    onSaveClick: (Authentication) -> Unit,
    onDeleteClick: (Authentication) -> Unit,
    onConnectionCheck: (Authentication, onSuccess: (Boolean) -> Unit) -> Unit,
    select: (Authentication) -> Unit,
    authentications: List<Authentication>, colorBackground: Color, colorForeground: Color) {

    val connection by connectivityState()
    val isConnected = connection === ConnectionState.Available

    val showDialog =  remember { mutableStateOf(false) }
    val deleteDialog = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<Authentication?>(null) }

    if(showDialog.value) {
        EditDialog(
            authentication = selectedItem.value,
            setShowDialog = { showDialog.value = it },
            {
                selectedItem.value = it
                onSaveClick(selectedItem.value!!)
            },
            {
                selectedItem.value = it
                deleteDialog.value = true
            }, onConnectionCheck
        )
    }
    if(deleteDialog.value) {
        ShowDeleteDialog(
            onShowDialog = {deleteDialog.value = it},
            deleteAction = {onDeleteClick(selectedItem.value!!)}
        )
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
            AuthenticationList(
                authentications = authentications, isConnected, {
                selectedItem.value = it
                showDialog.value = true
            }, select, colorBackground, colorForeground)
        }
        if(isConnected) {
            FloatingActionButton(
                onClick = {
                    selectedItem.value = Authentication(0, "", "", "", "", false, "", null)
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
fun AuthenticationList(authentications: List<Authentication>, isConnected: Boolean, onSelect: (Authentication) -> Unit, select: (Authentication) -> Unit, colorBackground: Color, colorForeground: Color) {
    if(isConnected) {
        if(authentications.isEmpty()) {
            Column {
                NoEntryItem(colorForeground, colorBackground)
            }
        } else {
            authentications.forEach { item ->
                AuthenticationItem(authentication = item, onSelect, select, colorBackground, colorForeground)
            }
        }
    } else {
        Column {
            NoInternetItem(colorForeground, colorBackground)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthenticationItem(authentication: Authentication, onSelect: (Authentication) -> Unit, select: (Authentication) -> Unit, colorBackground: Color, colorForeground: Color) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(70.dp)
        .background(color = colorBackground)
        .combinedClickable(
            onClick = { onSelect(authentication) },
            onLongClick = { select(authentication) }
        )) {


        Row(modifier = Modifier
            .fillMaxWidth()
            .height(69.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                if(authentication.selected) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        "${authentication.title} selected",
                        tint = colorForeground)
                } else {
                    Icon(
                        Icons.Filled.Circle,
                        "${authentication.title} not selected",
                        tint = colorForeground
                    )
                }
            }
            Column(modifier = Modifier
                .weight(9f)
                .fillMaxHeight()) {
                Row(modifier = Modifier
                    .wrapContentHeight()
                    .padding(all = 5.dp), Arrangement.Center) {
                    Text(text = authentication.title, modifier = Modifier
                        .padding(start = 5.dp, bottom = 2.dp)
                        .fillMaxWidth(), fontWeight =  FontWeight.Bold, color = colorForeground)
                }
                Row(modifier = Modifier
                    .wrapContentHeight()
                    .padding(all = 5.dp)) {
                    Column(modifier = Modifier.wrapContentWidth()) {
                        Text(text = authentication.url, modifier = Modifier
                            .padding(start = 5.dp)
                            .wrapContentWidth(), fontWeight =  FontWeight.Normal, color = colorForeground)
                    }
                    Column(modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 5.dp)) {
                        Text(text = "@${authentication.userName}", modifier = Modifier.fillMaxWidth(), fontWeight =  FontWeight.Normal, color = colorForeground)
                    }
                }
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colorForeground)) {}
    }
}

@Composable
private fun EditDialog(
    authentication: Authentication?,
    setShowDialog: (Boolean) -> Unit,
    onSaveClick: (Authentication) -> Unit,
    onDeleteClick: (Authentication) -> Unit,
    onConnectionCheck: (Authentication, onSuccess: (Boolean) -> Unit) -> Unit) {

    var id by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var isValidTitle by remember { mutableStateOf(authentication?.title?.isNotEmpty() == true) }
    var url by remember { mutableStateOf(TextFieldValue("")) }
    var isValidUrl by remember { mutableStateOf(authentication?.url?.isNotEmpty() == true) }
    var user by remember { mutableStateOf(TextFieldValue("")) }
    var pwd by remember { mutableStateOf(TextFieldValue("")) }
    var isConnectionValid by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var isValidDescription by remember { mutableStateOf(true) }
    var color by remember { mutableStateOf(Color.Red) }
    var showProgress by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("") }


    if(authentication != null) {
        id = authentication.id
        title = TextFieldValue(authentication.title)
        url = TextFieldValue(authentication.url)
        user = TextFieldValue(authentication.userName)
        pwd = TextFieldValue(authentication.password)
        description = TextFieldValue(authentication.description ?: "")
        type =
            if(authentication.type==Authentication.nextcloud)
                stringResource(R.string.auth_nextcloud)
            else
                stringResource(R.string.auth_owncloud)
    }

    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if(type.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(type, fontSize = 16.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            isValidTitle = Validator.check(false, 3, 255, it.text)
                        },
                        label = {Text(stringResource(id = R.string.login_title))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidTitle
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    var show by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            isValidUrl = Validator.checkUrl(it.text)
                            isConnectionValid = false
                            color = Color.Red
                            show = it.text.trim().lowercase().startsWith("https")
                        },
                        label = {Text(stringResource(id = R.string.login_url))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidUrl || !isConnectionValid || showProgress,
                        supportingText = {Text(stringResource(R.string.login_check_descr))},
                        leadingIcon = {
                            if(show) {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = stringResource(R.string.login_safe)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.LockOpen,
                                    contentDescription = stringResource(R.string.login_safe) + " - not"
                                )
                            }
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                showProgress = true
                                val auth = Authentication(
                                    0L, title.text, url.text, user.text,
                                    pwd.text, false, "", null
                                )

                                onConnectionCheck(auth) { state:Boolean ->
                                    isConnectionValid = state && isValidTitle && isValidUrl && isValidDescription
                                    color = if(state) Color.Green else Color.Red
                                    showProgress = false
                                }
                            }) {
                                if(showProgress) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = stringResource(R.string.login_check_url)
                                    )
                                } else {
                                    if(isConnectionValid) {
                                        Icon(
                                            Icons.Filled.Power,
                                            contentDescription = stringResource(R.string.login_connect)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.PowerOff,
                                            contentDescription = stringResource(R.string.login_connect)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = user,
                        onValueChange = {
                            user = it
                            isConnectionValid = false
                            color = Color.Red
                        },
                        label = {Text(stringResource(id = R.string.login_user))},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedPasswordField(
                        value = pwd,
                        onValueChange = {
                            pwd = it
                        },
                        label = R.string.login_pwd
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            isValidDescription = Validator.check(true, 0, 500, it.text)
                        },
                        label = {Text(stringResource(id = R.string.login_description))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValidDescription
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    if(authentication?.id != 0L) {
                        Column(modifier = Modifier
                            .width(50.dp)
                            .height(55.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                onDeleteClick(authentication!!)
                                setShowDialog(false)
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.login_delete),
                                    Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier
                        .weight(9F)
                        .height(55.dp)) {

                    }
                    Column(
                        modifier = Modifier
                            .width(50.dp)
                            .height(55.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = { setShowDialog(false) }, Modifier.height(50.dp)) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.login_close),
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .width(55.dp)
                            .height(55.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = {
                            val auth = Authentication(
                                id, title.text, url.text, user.text, pwd.text, false, description.text, null
                            )

                            onSaveClick(auth)
                            setShowDialog(false)
                        },
                            enabled = isValidTitle && isValidUrl && isValidDescription && isConnectionValid,
                            modifier = Modifier.height(50.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                stringResource(R.string.login_close),
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun fake(no: Long): Authentication {
    return Authentication(no, "Test$no", "https://test.de", "test$no", "", false, "", null)
}

@Preview(showBackground = true)
@Composable
fun AuthenticationItemPreview() {
    CloudAppTheme {
        AuthenticationItem(authentication = fake(1L), onSelect = {}, colorBackground = Color.Red, colorForeground = Color.Green, select = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationItemSelectedPreview() {
    CloudAppTheme {
        val fake = fake(1L)
        fake.selected = true
        AuthenticationItem(authentication = fake, onSelect = {}, colorBackground = Color.Red, colorForeground = Color.Green, select = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    AuthenticationScreen({}, {}, {_,_->}, {}, listOf(fake(1L), fake(2L)), Color.Red, Color.Green)
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DialogPreview() {
    CloudAppTheme {
        EditDialog(
            authentication = fake(1L),
            setShowDialog = {},
            onSaveClick = {},
            onDeleteClick = {},
            onConnectionCheck = {_,_->}
        )
    }
}
