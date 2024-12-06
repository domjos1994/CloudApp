package de.domjos.cloudapp2.features.contacts.screens

import de.domjos.cloudapp2.appbasics.custom.Dropdown
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.ActionItem
import de.domjos.cloudapp2.appbasics.custom.ComposeList
import de.domjos.cloudapp2.appbasics.custom.DatePickerDocked
import de.domjos.cloudapp2.appbasics.custom.DropDownItem
import de.domjos.cloudapp2.appbasics.custom.FAB
import de.domjos.cloudapp2.appbasics.custom.ListItem
import de.domjos.cloudapp2.appbasics.custom.LocalizedDropdown
import de.domjos.cloudapp2.appbasics.custom.MultiActionItem
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.ImageHelper
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.helper.hasEmail
import de.domjos.cloudapp2.appbasics.helper.openContact
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.AddressType
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import de.domjos.cloudapp2.database.model.contacts.PhoneType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale


@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ContactScreen(
    viewModel: ContactViewModel = hiltViewModel(),
    colorBackground: Color,
    colorForeground: Color,
    toAuths: () -> Unit,
    toPermissions: () -> Unit,
    toChat: (String) -> Unit) {

    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val addressBooks by viewModel.addressBooks.collectAsStateWithLifecycle()
    val canInsert by viewModel.canInsert.collectAsStateWithLifecycle()
    val connectivity by connectivityState()
    val available = connectivity === ConnectionState.Available

    LogViewModel.Init(viewModel)

    LaunchedEffect(true) {
        viewModel.getAddressBooks(available, context)
    }

    ContactScreen(onReload = {
            viewModel.loadAddresses(available)
            val items = mutableListOf<ListItem<Long>>()
            contacts.forEach { contact ->
                var book = contact.addressBook
                if(addressBooks.containsKey(book)) {
                    book = addressBooks[book].toString()
                }

                val listItem = ListItem<Long>(
                    "${contact.givenName} ${contact.familyName}".trim(),
                    book,
                    Icons.Default.AccountCircle
                )
                listItem.id = contact.id
                items.add(listItem)
            }
            items
        },
        contacts, colorBackground, colorForeground, addressBooks,
        viewModel.hasAuthentications(), toAuths, canInsert,
        onSelectedAddressBook = { book: String ->
            var key = ""
            addressBooks.forEach {if(book==it.value) key = it.key}
            viewModel.selectAddressBook(available, key)
        },
        onSave = {contact: Contact ->
            viewModel.addOrUpdateAddress(available, contact)
            viewModel.loadAddresses(available)
        },
        onDelete = {contact: Contact ->
            viewModel.deleteAddress(available, contact)
            viewModel.loadAddresses(available)
        },
        openEmail = {mail: String -> viewModel.openEmail(mail, context)},
        openPhone = {phone: String -> viewModel.openPhone(phone, toPermissions, context)},
        openChat = {contact: Contact -> viewModel.openChat(contact) {token ->
            if(token.isNotEmpty()) {
                toChat(token)
            }
        }},
        hasPhone = {viewModel.hasPhoneFeature(context)}
    )
}

@Composable
fun importContactAction(viewModel: ContactViewModel = hiltViewModel(), hasInternet: Boolean): (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit {
    val context = LocalContext.current
    return {onProgress, onFinish ->
        viewModel.import(onProgress, onFinish, context, hasInternet)
    }
}

@Composable
fun ContactScreen(
    onReload: ()-> MutableList<ListItem<Long>>,
    contacts: List<Contact>,
    colorBackground: Color,
    colorForeground: Color,
    addressBooks: Map<String, String>,
    hasAuths: Boolean, toAuths: () -> Unit,
    canInsert: Boolean,
    onSelectedAddressBook: (String) -> Unit,
    onSave: (Contact) -> Unit,
    onDelete: (Contact) -> Unit,
    openEmail: (String) -> Unit,
    openPhone: (String) -> Unit,
    openChat: (Contact) -> Unit,
    hasPhone: () -> Boolean) {

    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf<Contact?>(null) }
    var selectedContacts by remember { mutableStateOf<List<Contact>>(listOf()) }
    val all = stringResource(R.string.contacts_all)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMultipleDeleteDialog by remember { mutableStateOf(false) }

    if(showDeleteDialog) {
        ShowDeleteDialog(
            onShowDialog = {showDeleteDialog = it},
            {
                onDelete(contact!!)
                contact = null
            }
        )
    }

    if(showMultipleDeleteDialog) {
        ShowDeleteDialog(
            onShowDialog = {showDeleteDialog = it},
            {
                selectedContacts.forEach { selected ->
                    onDelete(selected)
                }
                selectedContacts = listOf()
            }
        )
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (list, control) = createRefs()

        Row(Modifier.constrainAs(list) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }) {
            Column {
                val context = LocalContext.current
                Row(
                    Modifier
                        .background(colorBackground)
                        .padding(5.dp)) {
                    Dropdown(
                        list = addressBooks.values.toList(),
                        value = all,
                        onValueChange = onSelectedAddressBook,
                        label = stringResource(R.string.contacts_book),
                        colorBackground = colorBackground,
                        colorForeground = colorForeground
                    )
                }
                HorizontalDivider(color = colorForeground)

                if(showDialog) {
                    EditDialog(contact = contact, setShowDialog = {
                        showDialog=it
                    }, onSave = onSave, onDelete = {
                        contact = it
                        showDeleteDialog = true
                    }, canInsert = canInsert)
                }
                if(showBottomSheet) {
                    BottomSheet(contact = contact!!, {showBottomSheet=it}, openPhone, openEmail, hasPhone)
                }

                if(hasAuths) {
                    val painter = painterResource(R.drawable.ic_eye)
                    ComposeList(
                        onReload = {onReload()},
                        colorBackground = colorBackground,
                        colorForeground = colorForeground,
                        onSwipeToStart = ActionItem(
                            name = stringResource(R.string.sys_list_delete),
                            icon = Icons.Default.Delete,
                            action = { item:ListItem<Long> ->
                                val c = contacts.find { it.id == item.id }
                                if(c != null) {
                                    contact = c
                                    showDeleteDialog = true
                                    true
                                } else {false}
                            },
                            color = Color.Red,
                            visible = {canInsert}
                        ),
                        actions = listOf(
                            ActionItem(
                                name = stringResource(R.string.chats),
                                painter = painterResource(R.drawable.baseline_chat_24),
                                action = {item:ListItem<Long> ->
                                    val c = contacts.find { it.id == item.id }
                                    if(c != null) {
                                        openChat(c)
                                    }
                                    false
                                },
                                visible = {item ->
                                    val c = contacts.find { it.id == item.id }
                                    c?.contactToChat == true
                                }
                            ),
                            ActionItem(
                                name = stringResource(R.string.contact_open_phone),
                                painter  = painterResource(R.drawable.baseline_phone_24),
                                action = {item:ListItem<Long> ->
                                    try {
                                        val c = contacts.find { it.id == item.id }
                                        if(c != null) {
                                            openPhone(c.phoneNumbers[0].value)
                                            return@ActionItem true
                                        }
                                    } catch (_: Exception) {}
                                    false
                                },
                                visible = {item ->
                                    val c = contacts.find { it.id == item.id }
                                    if(c != null) {
                                        if(c.phoneNumbers.isNotEmpty()) {
                                            de.domjos.cloudapp2.appbasics.helper.hasPhone(context, c.phoneNumbers[0].value)
                                        } else { false }
                                    } else { false }
                                }
                            ),
                            ActionItem(
                                name = stringResource(R.string.contact_mail),
                                painter  = painterResource(R.drawable.baseline_mail_24),
                                action = {item ->
                                    try {
                                        val c = contacts.find { it.id == item.id }
                                        if(c != null) {
                                            openEmail(c.emailAddresses[0].value)
                                            return@ActionItem true
                                        }
                                    } catch (_: Exception) {}
                                    false
                                },
                                visible = {item ->
                                    val c = contacts.find { it.id == item.id }
                                    if(c != null) {
                                        if(c.emailAddresses.isNotEmpty()) {
                                            hasEmail(context,c.emailAddresses[0].value)
                                        } else { false }
                                    } else { false }
                                }
                            ),
                            ActionItem(
                                name = stringResource(R.string.sys_list_show),
                                painter = painter,
                                action = {item ->
                                    val c = contacts.find { it.id == item.id }
                                    if(c != null) {
                                        contact = c
                                        showBottomSheet = true
                                        true
                                    } else {false}
                                }
                            ),
                            ActionItem(
                                name = stringResource(R.string.sys_list_edit),
                                icon = Icons.Default.Edit,
                                action = {item ->
                                    val c = contacts.find { it.id == item.id }
                                    if(c != null) {
                                        contact = c
                                        showDialog = true
                                        true
                                    } else {false}
                                },
                                visible = {canInsert}
                            )
                        ),
                        multiActions = listOf(
                            MultiActionItem(
                                name = stringResource(R.string.sys_list_delete),
                                icon = Icons.Default.Delete,
                                action = { selected ->
                                    val items = contacts.filter {
                                        it.id == (selected.find { ite -> ite.id == it.id }?.id ?: 0)
                                    }
                                    selectedContacts = items
                                    showMultipleDeleteDialog = true
                                    true
                                },
                                visible = canInsert
                            )
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                    )
                } else {
                    NoAuthenticationItem(
                        colorBackground = colorBackground,
                        colorForeground = colorForeground,
                        toAuths = toAuths
                    )
                }
            }
        }

        if(hasAuths && canInsert) {
            FAB(
                Icons.Filled.Add,
                stringResource(R.string.contacts),
                colorBackground,
                colorForeground,
                Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
            ) {
                contact = null
                showDialog = true
            }
        }
    }
}

@Composable
fun EditDialog(
    contact: Contact?,
    setShowDialog: (Boolean) -> Unit,
    onSave: (Contact) -> Unit,
    onDelete: (Contact) -> Unit,
    canInsert: Boolean) {

    var uid by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf(TextFieldValue("")) }
    var prefix by remember { mutableStateOf(TextFieldValue("")) }
    var firstName by remember { mutableStateOf(TextFieldValue("")) }
    var isFirstNameValid by remember { mutableStateOf(contact?.givenName?.isNotEmpty() == true) }
    var lastName by remember { mutableStateOf(TextFieldValue("")) }
    var isLastNameValid by remember { mutableStateOf(true) }
    var additional by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf(Date()) }
    var organization by remember { mutableStateOf(TextFieldValue("")) }
    val phones = remember { mutableStateListOf(
        Phone(0L, "", "", LinkedList())
    ) }
    val mails = remember { mutableStateListOf(
        Email(0L, "", "")
    ) }
    val addresses = remember { mutableStateListOf(
        Address(0L, "", LinkedList(), "", "", "", "", "", "", "")
    ) }
    val img = remember { mutableStateOf<ByteArray?>(null) }

    if(contact != null) {
        uid = contact.uid!!
        suffix = TextFieldValue(contact.suffix!!)
        prefix = TextFieldValue(contact.prefix!!)
        firstName = TextFieldValue(contact.givenName)
        lastName = TextFieldValue(contact.familyName!!)
        birthDate = if(contact.birthDay != null) contact.birthDay!! else Date()
        additional = TextFieldValue(contact.additional!!)
        organization = TextFieldValue(contact.organization!!)
        img.value = contact.photo

        contact.phoneNumbers.forEach { phones.add(it) }
        contact.addresses.forEach { addresses.add(it) }
        contact.emailAddresses.forEach { mails.add(it) }
    }

    Dialog(
        onDismissRequest = {setShowDialog(false)},
        DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState())) {
            Column {

                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(contract =
                ActivityResultContracts.GetContent()) { uri: Uri? ->
                    val data = convertImageToByte(uri, context)
                    img.value = data
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { launcher.launch("image/*") },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,) {
                        if(img.value != null) {
                            val data = ImageHelper.convertImageByteArrayToBitmap(img.value!!)
                            if(data != null) {
                                Image(data.asImageBitmap(), contentDescription = firstName.text)
                            } else {
                                Image(painterResource(id = R.drawable.baseline_person_24), contentDescription = firstName.text)
                            }
                        } else {
                            Image(
                                painterResource(id = R.drawable.baseline_person_24),
                                contentDescription = firstName.text
                            )
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()) {
                    Column(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight()) {
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                isFirstNameValid = Validator.check(false, 2, 255, it.text)
                            },
                            label = {Text(stringResource(R.string.contact_given))},
                            modifier = Modifier.padding(2.dp),
                            isError = !isFirstNameValid
                        )
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight()) {
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                isLastNameValid = Validator.check(true, 0, 255, it.text)
                            },
                            label = {Text(stringResource(R.string.contact_family))},
                            modifier = Modifier.padding(2.dp),
                            isError = !isLastNameValid
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)) {}

                TabControl({
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()) {
                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight()) {
                            OutlinedTextField(
                                value = suffix,
                                onValueChange = {
                                    suffix = it
                                },
                                label = {Text(stringResource(R.string.contact_suffix))},
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight()) {
                            OutlinedTextField(
                                value = prefix,
                                onValueChange = {
                                    prefix = it
                                },
                                label = {Text(stringResource(R.string.contact_prefix))},
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()) {
                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight()) {
                            OutlinedTextField(
                                value = additional,
                                onValueChange = {
                                    additional = it
                                },
                                label = {Text(stringResource(R.string.contact_additional))},
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()) {
                        Column(
                            Modifier
                                .weight(1f)
                                .wrapContentHeight()) {
                            DatePickerDocked(
                                date = birthDate,
                                onValueChange = {
                                    birthDate = it
                                },
                                label = {Text(stringResource(R.string.contact_birthDate))},
                                showTime = false,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()) {
                        Column(
                            Modifier
                                .weight(2f)
                                .wrapContentHeight()) {
                            OutlinedTextField(
                                value = organization,
                                onValueChange = {
                                    organization = it
                                },
                                label = {Text(stringResource(R.string.contact_org))},
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }, phones, mails, addresses, uid)

                if(canInsert) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()) {
                        if(contact != null) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = {
                                    onDelete(contact)
                                    setShowDialog(false)
                                }) {
                                    Icon(Icons.Default.Delete, "")
                                }
                            }
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                val suf = suffix.text
                                val pre = prefix.text
                                val first = firstName.text
                                val last = lastName.text
                                val add = additional.text
                                val bd = birthDate
                                val org = organization.text
                                val ph = LinkedList<Phone>()
                                phones.forEach {
                                    if(it.value != "") {
                                        ph.add(it)
                                    }
                                }
                                val em = LinkedList<Email>()
                                mails.forEach {
                                    if(it.value != "") {
                                        em.add(it)
                                    }
                                }
                                val a = LinkedList<Address>()
                                addresses.forEach {
                                    if(it.locality != "") {
                                        a.add(it)
                                    }
                                }
                                val photo = img.value
                                val addressBook = contact?.addressBook ?: ""
                                val id = contact?.id ?: 0L
                                val path = contact?.path ?: ""

                                val new = Contact(id, uid, path, suf, pre, last, first, add, bd, org, photo, addressBook, "", -1L, -1L, 0L)
                                new.addresses = a
                                new.emailAddresses = em
                                new.phoneNumbers = ph

                                onSave(new)
                                setShowDialog(false)
                            }, enabled = isFirstNameValid && isLastNameValid) {
                                Icon(Icons.Default.Check, "")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabControl(
    mainContent: @Composable () -> Unit = {},
    phones: MutableList<Phone>,
    mails: MutableList<Email>,
    addresses: MutableList<Address>,
    uid: String) {

    var state by remember { mutableIntStateOf(0) }
    val icons = mapOf(
        Icons.Default.Person to stringResource(R.string.contacts),
        Icons.Default.Phone to stringResource(R.string.contact_phones),
        Icons.Default.Email to stringResource(R.string.contact_mails),
        Icons.Default.LocationOn to stringResource(R.string.contact_addresses)
    )

    Row {
        Column {
            SecondaryTabRow(selectedTabIndex = state) {
                icons.entries.forEachIndexed { index, icon ->
                    Tab(
                        selected = state == index,
                        onClick = { state = index},
                        icon = {Icon(icon.key, icon.value)}
                    )
                }
            }
            if(state == 0) {
                mainContent()
            }
            if(state == 1) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp)) {
                    Column(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = {
                            phones.add(Phone(0L, uid, "", LinkedList()))
                        }) {
                            Icon(Icons.Default.Add, "")
                        }
                    }
                }

                // phones
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)) {}

                phones.forEach { phone ->
                    PhoneItem(phone = phone) {
                        phones.remove(phone)
                    }
                }
            }
            if(state == 2) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp)) {
                    Column(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = {
                            mails.add(Email(0L, uid, ""))
                        }) {
                            Icon(Icons.Default.Add, "")
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)) {}

                mails.forEach { email ->
                    MailItem(email = email) {
                        mails.remove(email)
                    }
                }
            }
            if(state == 3) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(30.dp)) {
                    Column(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = {
                            addresses.add(
                                Address(
                                    0L, uid, LinkedList(),
                                    "", "", "", "",
                                    "", "", ""
                                ))
                        }) {
                            Icon(Icons.Default.Add, "")
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)) {}

                addresses.forEach { address ->
                    AddressItem(address = address) {
                        addresses.remove(it)
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black)) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BottomSheet(
    contact: Contact,
    setShowBottomSheet: (Boolean) -> Unit,
    openPhone: (String) -> Unit,
    openEmail: (String) -> Unit,
    hasPhone: () -> Boolean) {

    val name = contact.toString()

    val context = LocalContext.current
    val iconColor = OutlinedTextFieldDefaults.colors().focusedTextColor
    val backColor = BottomSheetDefaults.ContainerColor
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dt = sdf.format(contact.birthDay ?: Date())

    ModalBottomSheet(onDismissRequest = { setShowBottomSheet(false) }) {
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            if(contact.photo != null) {
                val data = ImageHelper.convertImageByteArrayToBitmap(contact.photo!!)
                if(data != null) {
                    Image(
                        data.asImageBitmap(),
                        contentDescription = name,
                        Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Image(
                        painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = name,
                        colorFilter = ColorFilter.tint(iconColor),
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            } else {
                Image(
                    painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = name,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.basicMarquee()
            )
        }
        Separator(iconColor)
        if(contact.birthDay != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.contact_birthDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.Start) {
                    Text(
                        dt,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }
        if(contact.organization != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(id = R.string.contact_org),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp)
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.Start) {
                    Text(
                        contact.organization!!,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }
        if(contact.additional != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(id = R.string.contact_additional),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp)
                }
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.Start) {
                    Text(
                        contact.additional!!,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }
        Separator(iconColor)
        contact.phoneNumbers.forEach { number ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(1.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(1.dp)) {
                    IconButton(onClick = { openPhone(number.value) }, enabled = hasPhone()) {
                        Icon(Icons.Filled.Phone, number.value)
                    }
                }
                Column(
                    Modifier
                        .weight(5f)
                        .padding(1.dp),
                    horizontalAlignment = Alignment.Start) {
                    Text(
                        number.value,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
                Column(
                    Modifier
                        .weight(4f)
                        .padding(1.dp),
                    horizontalAlignment = Alignment.End) {
                    FlowRow {
                        number.types.forEach { type ->
                            Tag(getPhoneTypeLabel(type), iconColor, backColor)
                        }
                    }
                }
            }
        }

        Separator(iconColor)
        contact.emailAddresses.forEach { email ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(1.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(2f)
                        .padding(1.dp)) {
                    IconButton(onClick = { openEmail(email.value) }) {
                        Icon(Icons.Filled.Email, email.value)
                    }
                }
                Column(
                    Modifier
                        .weight(18f)
                        .padding(1.dp),
                    horizontalAlignment = Alignment.Start) {
                    Text(
                        email.value,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Separator(iconColor)
        contact.addresses.forEach { address ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(1.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                FlowRow {
                    address.types.forEach { type ->
                        Tag(getAddressTypeLabel(type), iconColor, backColor)
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(1.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                if(address.postOfficeAddress != null) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(R.string.contact_addresses_postOfficeAddress),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            address.postOfficeAddress!!,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            if(address.street != "") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.contact_addresses_street),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            address.street,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            if(address.locality != "") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.contact_addresses_locality),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            "${address.postalCode} ${address.locality}",
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            if(address.region != null && address.region != "") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.contact_addresses_region),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            address.region!!,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            if(address.country != null && address.country != "") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.contact_addresses_country),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(1.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            address.country!!,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            if(address.extendedAddress != null && address.extendedAddress != "") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(5.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            stringResource(id = R.string.contact_addresses_extended),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(5.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            address.extendedAddress!!,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        Separator(iconColor)
        if(contact.contactId != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center) {
                Button(onClick = { openContact(context, contact.contactId!!) }, Modifier.height(50.dp)) {
                    Text(stringResource(R.string.contact_open))
                }
            }
        }
        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.navigationBarsIgnoringVisibility
            )
        )
    }
}

fun convertImageToByte(uri: Uri?, context: Context): ByteArray? {
    var data: ByteArray? = null
    try {
        val cr: ContentResolver = context.contentResolver
        val inputStream = cr.openInputStream(uri!!)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        data = outputStream.toByteArray()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    return data
}

@Composable
fun PhoneItem(phone: Phone, onDelete: (Phone) -> Unit) {
    var number by remember { mutableStateOf(phone.value) }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp)) {
        Column(
            Modifier
                .weight(8f)
                .height(130.dp)) {
            Row {
                val items = mutableListOf<DropDownItem>()
                //items.add(DropDownItem(PhoneType.PREF.name, stringResource(R.string.contact_phone_pref)))
                items.add(DropDownItem(PhoneType.WORK.name, stringResource(R.string.contact_phone_work)))
                items.add(DropDownItem(PhoneType.HOME.name, stringResource(R.string.contact_phone_home)))
                //items.add(DropDownItem(PhoneType.VOICE.name, stringResource(R.string.contact_phone_voice)))
                items.add(DropDownItem(PhoneType.FAX.name, stringResource(R.string.contact_phone_fax)))
                //items.add(DropDownItem(PhoneType.MSG.name, stringResource(R.string.contact_phone_msg)))
                items.add(DropDownItem(PhoneType.CELL.name, stringResource(R.string.contact_phone_cell)))

                val item = if(phone.types.size > 0) phone.types[0].name else PhoneType.HOME.name
                LocalizedDropdown(
                    value = item,
                    onValueChange = {
                        phone.types.clear()
                        phone.types.add(PhoneType.valueOf(it))
                    },
                    items,
                )
            }
            Row {
                OutlinedTextField(
                    value = number,
                    onValueChange = {
                        number = it
                        phone.value = number
                    },
                    label = {
                        Text(stringResource(R.string.contact_phone))
                    }
                )
            }
        }

        Column(
            Modifier
                .weight(2f)
                .height(130.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onDelete(phone) }) {
                Icon(Icons.Filled.Delete, phone.value)
            }
        }
    }
    HorizontalDivider(Modifier.padding(top = 10.dp))
}

@Composable
fun MailItem(email: Email, onDelete: (Email) -> Unit) {
    var mail by remember { mutableStateOf(email.value) }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp)) {
        Column(
            Modifier
                .weight(18f)
                .height(75.dp),
            verticalArrangement = Arrangement.Center) {
            OutlinedTextField(
                value = mail,
                onValueChange = {
                    mail = it
                    email.value = mail
                },
                label = {
                    Text(stringResource(R.string.contact_mail))
                })
        }

        Column(
            Modifier
                .weight(2f)
                .height(75.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onDelete(email) }) {
                Icon(Icons.Filled.Delete, email.value)
            }
        }
    }
    HorizontalDivider(Modifier.padding(top = 10.dp))
}

@Composable
fun AddressItem(address: Address, onDelete: (Address) -> Unit) {
    var poAddress by remember { mutableStateOf(address.postOfficeAddress ?: "") }
    var street by remember { mutableStateOf(address.street) }
    var postal by remember { mutableStateOf(address.postalCode ?: "") }
    var locality by remember { mutableStateOf(address.locality ?: "") }
    var region by remember { mutableStateOf(address.region ?: "") }
    var ext by remember { mutableStateOf(address.extendedAddress ?: "") }
    var further by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp)) {
        Column {
            Row {
                Column(
                    Modifier
                        .weight(9f)
                        .height(70.dp)) {
                    val items = mutableListOf<DropDownItem>()
                    items.add(DropDownItem(AddressType.home.name, stringResource(R.string.contact_addresses_type_home)))
                    items.add(DropDownItem(AddressType.work.name, stringResource(R.string.contact_addresses_type_work)))
                    items.add(DropDownItem(AddressType.postal.name, stringResource(R.string.contact_addresses_type_domestic)))

                    val item = if(address.types.size > 0) address.types[0].name else AddressType.home.name

                    LocalizedDropdown(
                        item,
                        {
                            address.types.clear()
                            address.types.add(AddressType.valueOf(it))
                        },
                        items
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .height(70.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { onDelete(address) }) {
                        Icon(Icons.Filled.Delete, address.postOfficeAddress)
                    }
                }
            }
            if(further) {
                Row {
                    OutlinedTextField(
                        value = poAddress,
                        onValueChange = {
                            poAddress = it
                            address.postOfficeAddress = poAddress
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_postOfficeAddress), fontSize=13.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row {
                Column(Modifier.weight(12f)) {
                    OutlinedTextField(
                        value = postal,
                        onValueChange = {
                            postal = it
                            address.postalCode = postal
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_postal), fontSize=13.sp)
                        }
                    )
                }
                Column(Modifier.weight(1f)) { }
                Column(Modifier.weight(17f)) {
                    OutlinedTextField(
                        value = locality,
                        onValueChange = {
                            locality = it
                            address.locality = locality
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_locality), fontSize=13.sp)
                        }
                    )
                }
            }
            if(further) {

                Row {
                    OutlinedTextField(
                        value = region,
                        onValueChange = {
                            region = it
                            address.region = region
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_region), fontSize=13.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row {
                    OutlinedTextField(
                        value = address.country ?: "",
                        onValueChange = {
                            address.country = it
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_country), fontSize=13.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row {
                    OutlinedTextField(
                        value = ext,
                        onValueChange = {
                            ext = it
                            address.extendedAddress = ext
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_extended), fontSize=13.sp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Row {
                OutlinedTextField(
                    value = street,
                    onValueChange = {
                        street = it
                        address.street = street
                    },
                    label = {
                        Text(stringResource(R.string.contact_addresses_street), fontSize=13.sp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row {
                Column(
                    Modifier
                        .weight(8f)
                        .padding(5.dp)
                        .height(30.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.contact_addresses_further))
                }
                Column(
                    Modifier
                        .weight(2f)
                        .padding(5.dp)
                        .height(30.dp)
                ) {
                    Switch(
                        checked = further,
                        onCheckedChange = {further = it}
                    )
                }
            }
            HorizontalDivider(Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
fun getAddressTypeLabel(type: AddressType): String {
    return when(type) {
        AddressType.domestic -> stringResource(R.string.contact_addresses_type_domestic)
        AddressType.home -> stringResource(R.string.contact_addresses_type_home)
        AddressType.work -> stringResource(R.string.contact_addresses_type_work)
        AddressType.postal -> stringResource(R.string.contact_addresses_type_postal)
        AddressType.parcel -> stringResource(R.string.contact_addresses_type_parcel)
        AddressType.international -> stringResource(R.string.contact_addresses_type_international)
        else -> ""
    }
}

@Composable
fun getPhoneTypeLabel(type: PhoneType): String {
    return when(type) {
        PhoneType.HOME -> stringResource(R.string.contact_phone_home)
        PhoneType.CELL -> stringResource(R.string.contact_phone_cell)
        PhoneType.PREF -> stringResource(R.string.contact_phone_pref)
        PhoneType.WORK -> stringResource(R.string.contact_phone_work)
        PhoneType.FAX -> stringResource(R.string.contact_phone_fax)
        PhoneType.VOICE -> stringResource(R.string.contact_phone_voice)
        else -> ""
    }
}

@Composable
private fun Tag(item: String, colorBackground: Color, colorForeground: Color) {
    Column(Modifier.padding(1.dp)) {
        Row(Modifier.background(color = colorBackground, shape = RoundedCornerShape(3.dp))) {
            Column(Modifier.padding(1.dp)) {
                Icon(Icons.Default.Star, contentDescription = item, tint = colorForeground)
            }
            Column(Modifier.padding(start = 1.dp, end = 10.dp)) {
                Text(item, color = colorForeground, fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContactScreenPreview() {
    CloudAppTheme {
        ContactScreen(
            onReload = { mutableListOf() },
            contacts = listOf(fakeContact(1), fakeContact(2), fakeContact(3)),
            colorForeground = Color.White,
            colorBackground = Color.Blue,
            addressBooks = mapOf(),
            hasAuths = true,
            toAuths = {},
            canInsert = true,
            onSelectedAddressBook = {},
            onSave = {},
            onDelete = {},
            openEmail = {},
            openPhone = {},
            openChat = {},
            hasPhone = {true}
        )
    }
}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TabControlPreview() {
    CloudAppTheme {
        TabControl({}, LinkedList(), LinkedList(), LinkedList(), "")
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomSheetPreview() {
    CloudAppTheme {
        BottomSheet(contact = fakeContact(0), {}, {}, {}, {true})
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditDialogPreview() {
    CloudAppTheme {
        EditDialog(contact = fakeContact(1), {}, {}, {}, true)
    }
}

@Preview(showBackground = true)
@Composable
fun TagPreview() {
    CloudAppTheme {
        Tag("Test", Color.Blue, Color.White)
    }
}

fun fakeContact(id: Int): Contact {
    val bDate = Calendar.getInstance()
    bDate.set(1960, 1, 1)
    bDate.add(Calendar.DAY_OF_MONTH, id)
    val contact =  Contact(0L,
        "$id", "", "Suffix", "Prefix", "Doe", "John$id", "Additional",
        bDate.time, "Organization", null, "Test", "", -1L, -1L, 0L)
    contact.phoneNumbers.add(fakeNumber(0))
    contact.phoneNumbers.add(fakeNumber(1))
    contact.emailAddresses.add(fakeEmail(0))
    contact.emailAddresses.add(fakeEmail(1))
    contact.addresses.add(fakeAddress(0))
    contact.addresses.add(fakeAddress(1))
    return contact
}

fun fakeNumber(id: Long): Phone {
    return Phone(id, "$id", "01234 56789", mutableListOf(PhoneType.CELL, PhoneType.HOME))
}

fun fakeEmail(id: Long): Email {
    return Email(id, "$id", "test@test.de")
}

fun fakeAddress(id: Long): Address {
    return Address(
        id, "$id",
        mutableListOf(AddressType.postal, AddressType.home, AddressType.work),
        "postOfficeAddress", "extendedAddress", "Street",
        "Locality", "Region", "postalCode", "Country"
    )
}