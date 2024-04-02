package de.domjos.cloudapp.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.appbasics.R

@Composable
fun AuthenticationScreen(viewModel: AuthenticationViewModel = hiltViewModel()) {
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
                Text(error.throwable.message!!)
            }
        }
    }

    if (
        authentications is AuthenticationUiState.Success) {

        val context = LocalContext.current

        AuthenticationScreen(
            onSaveClick = {auth ->
                if(auth.id == 0L) {
                    viewModel.insertAuthentication(auth, context) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                } else {
                    viewModel.updateAuthentication(auth, context) {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDeleteClick = { auth ->
                viewModel.deleteAuthentication(auth) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            },
            select = { auth -> viewModel.checkAuthentications(auth)},
            (authentications as AuthenticationUiState.Success).data
        )
    }
}

@Composable
fun AuthenticationScreen(
    onSaveClick: (Authentication) -> Unit,
    onDeleteClick: (Authentication) -> Unit,
    select: (Authentication) -> Unit,
    authentications: List<Authentication>) {

    val showDialog =  remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<Authentication?>(null) }

    if(showDialog.value) {
        EditDialog(
            authentication = selectedItem.value,
            setShowDialog = { showDialog.value = it },
            {
                selectedItem.value = it
                onSaveClick(selectedItem.value!!)
            },
            onDeleteClick
        )
    }



    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(5.dp)
            .verticalScroll(rememberScrollState())) {
            AuthenticationList(
                authentications = authentications, {
                selectedItem.value = it
                showDialog.value = true
            }, select)
        }
        Column(modifier = Modifier
            .fillMaxWidth()) {
            Row {
                Column {
                    FloatingActionButton(
                        onClick = {
                            selectedItem.value = Authentication(0, "", "", "", "", false, "", null)
                            showDialog.value = true
                        },
                        modifier = Modifier.align(Alignment.End)
                            .padding(5.dp)) {
                        Icon(Icons.Filled.Add, stringResource(R.string.login_add))
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticationList(authentications: List<Authentication>, onSelect: (Authentication) -> Unit, select: (Authentication) -> Unit) {
    authentications.forEach { item ->
        AuthenticationItem(authentication = item, onSelect, select)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthenticationItem(authentication: Authentication, onSelect: (Authentication) -> Unit, select: (Authentication) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(color = MaterialTheme.colorScheme.primaryContainer)
        .combinedClickable(
            onClick = { onSelect(authentication) },
            onLongClick = { select(authentication) }
        )) {


        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier
                .weight(1f)
                .wrapContentHeight()) {
                Checkbox(checked = authentication.selected, onCheckedChange = {}, enabled = false)
            }
            Column(modifier = Modifier
                .weight(9f)
                .wrapContentHeight()) {
                Row(modifier = Modifier
                    .wrapContentHeight()
                    .padding(all = 5.dp), Arrangement.Center) {
                    Text(text = authentication.title, modifier = Modifier
                        .padding(start = 5.dp, bottom = 2.dp)
                        .fillMaxWidth(), fontWeight =  FontWeight.Bold)
                }
                Row(modifier = Modifier
                    .wrapContentHeight()
                    .padding(all = 5.dp)) {
                    Column(modifier = Modifier.wrapContentWidth()) {
                        Text(text = authentication.url, modifier = Modifier
                            .padding(start = 5.dp)
                            .wrapContentWidth(), fontWeight =  FontWeight.Normal)
                    }
                    Column(modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 5.dp)) {
                        Text(text = "@${authentication.userName}", modifier = Modifier.fillMaxWidth(), fontWeight =  FontWeight.Normal)
                    }
                }
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Composable
private fun EditDialog(
    authentication: Authentication?,
    setShowDialog: (Boolean) -> Unit,
    onSaveClick: (Authentication) -> Unit,
    onDeleteClick: (Authentication) -> Unit) {

    val context = LocalContext.current

    var id by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var titleVal by remember { mutableStateOf("")}
    var url by remember { mutableStateOf(TextFieldValue("")) }
    var urlVal by remember { mutableStateOf("")}
    var user by remember { mutableStateOf(TextFieldValue("")) }
    var pwd by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }


    if(authentication != null) {
        id = authentication.id
        title = TextFieldValue(authentication.title)
        url = TextFieldValue(authentication.url)
        user = TextFieldValue(authentication.userName)
        pwd = TextFieldValue(authentication.password)
        description = TextFieldValue(authentication.description ?: "")
        titleVal = "" //Validator.validateTextNotEmpty(title.text, 3, 255, context)
        urlVal = "" //Validator.validateTextNotEmpty(url.text, 10, 500, context)
    }

    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            //titleVal = Validator.validateTextNotEmpty(title.text, 3, 255, context)
                        },
                        label = {Text(stringResource(id = R.string.login_title))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = titleVal!=""
                    )
                }
                if(titleVal!="") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = titleVal)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = {
                            url = it
                            //urlVal = Validator.validateTextNotEmpty(url.text, 10, 500, context)
                        },
                        label = {Text(stringResource(id = R.string.login_url))},
                        modifier = Modifier.fillMaxWidth(),
                        isError = urlVal!=""
                    )
                }
                if(urlVal!="") {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = urlVal)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = user,
                        onValueChange = {
                            user = it
                        },
                        label = {Text(stringResource(id = R.string.login_user))},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pwd,
                        onValueChange = {
                            pwd = it
                        },
                        label = {Text(stringResource(id = R.string.login_pwd))},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = {Text(stringResource(id = R.string.login_description))},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    if(authentication?.id != 0L) {
                        Column(modifier = Modifier.weight(1F)) {
                            IconButton(onClick = {
                                onDeleteClick(authentication!!)
                                setShowDialog(false)
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.login_delete))
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(9F)) {

                    }
                    Column(modifier = Modifier.weight(1F)) {
                        IconButton(onClick = { setShowDialog(false) }) {
                            Icon(Icons.Default.Close, stringResource(R.string.login_close))
                        }
                    }
                    if(titleVal=="" && urlVal=="") {
                        Column(
                            modifier = Modifier.weight(1F)) {
                            IconButton(onClick = {
                                val auth = Authentication(
                                    id, title.text, url.text, user.text, pwd.text, false, description.text, null
                                )

                                onSaveClick(auth)
                                setShowDialog(false)
                            }) {
                                Icon(Icons.Default.CheckCircle, stringResource(R.string.login_close))
                            }
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
    AuthenticationItem(authentication = fake(1L), {}) {}
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    AuthenticationScreen({}, {}, {}, listOf(fake(1L), fake(2L)))
}

@Preview(showBackground = true)
@Composable
fun DialogPreview() {
    EditDialog(
        authentication = fake(1L),
        setShowDialog = {},
        onSaveClick = {},
        onDeleteClick = {}
    )
}
