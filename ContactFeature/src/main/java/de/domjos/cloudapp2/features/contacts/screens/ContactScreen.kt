package de.domjos.cloudapp2.features.contacts.screens

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoEntryItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.ImageHelper
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.helper.openContact
import de.domjos.cloudapp2.appbasics.helper.openPhone
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
fun ContactScreen(viewModel: ContactViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val addressBooks by viewModel.addressBooks.collectAsStateWithLifecycle()
    val canInsert by viewModel.canInsert.collectAsStateWithLifecycle()
    val connectivity by connectivityState()
    val available = connectivity === ConnectionState.Available
    viewModel.getAddressBooks(LocalContext.current)
    viewModel.loadAddresses()

    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    ContactScreen(contacts, colorBackground, colorForeground, addressBooks, viewModel.hasAuthentications(), toAuths, canInsert, onSelectedAddressBook = { book: String ->
        var key = ""
        addressBooks.forEach {if(book==it.value) key = it.key}
        viewModel.selectAddressBook(key)
    }, onSave = {contact: Contact ->
        viewModel.addOrUpdateAddress(available, contact)
        viewModel.loadAddresses()
    }, onDelete = {contact: Contact ->
        viewModel.deleteAddress(available, contact)
        viewModel.loadAddresses()
    })
}

@Composable
fun importContactAction(viewModel: ContactViewModel = hiltViewModel()): (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit {
    return {onProgress, onFinish ->
        viewModel.importAddresses(onProgress, onFinish)
    }
}

@Composable
fun ContactScreen(
    contacts: List<Contact>,
    colorBackground: Color,
    colorForeground: Color,
    addressBooks: Map<String, String>,
    hasAuths: Boolean, toAuths: () -> Unit,
    canInsert: Boolean,
    onSelectedAddressBook: (String) -> Unit,
    onSave: (Contact) -> Unit,
    onDelete: (Contact) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var contact by remember { mutableStateOf<Contact?>(null) }
    val all = stringResource(R.string.contacts_all)
    var showDeleteDialog by remember { mutableStateOf(false) }

    if(showDeleteDialog) {
        ShowDeleteDialog(onShowDialog = {showDeleteDialog = it}, {onDelete(contact!!)})
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
                DropDown(addressBooks.values.toList(), all, onSelectedAddressBook, stringResource(R.string.contacts_book))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Black)) {}


                if(showDialog) {
                    EditDialog(contact = contact, setShowDialog = {
                        showDialog=it
                    }, onSave = onSave, onDelete = {
                        contact = it
                        showDeleteDialog = true
                    }, canInsert = canInsert)
                }
                if(showBottomSheet) {
                    BottomSheet(contact = contact!!) {showBottomSheet=it}
                }

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if(hasAuths) {
                        if(contacts.isEmpty()) {
                            NoEntryItem(colorForeground = colorBackground, colorBackground = colorForeground)
                        } else {
                            contacts.forEach {
                                ContactItem(contact = it, addressBooks, colorBackground, colorForeground, { c ->
                                    contact = c
                                    showBottomSheet = true
                                }) { c->
                                    contact = c
                                    showDialog = true
                                }
                            }
                        }
                    } else {
                        NoAuthenticationItem(
                            colorBackground = colorBackground,
                            colorForeground = colorForeground,
                            toAuths = toAuths
                        )
                    }
                }
            }
        }

        if(hasAuths && canInsert) {
            FloatingActionButton(
                onClick = {
                    contact = null
                    showDialog = true
                },
                modifier = Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .padding(5.dp)) {
                Icon(Icons.Filled.Add, stringResource(R.string.chats_room))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactItem(contact: Contact, addressBooks: Map<String, String>, colorBackground: Color, colorForeground: Color, onClick: (Contact) -> Unit, onLongClick: (Contact) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
            .height(40.dp)
            .background(colorBackground)
            .combinedClickable(
                onClick = { onClick(contact) },
                onLongClick = { onLongClick(contact) },
            )) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(colorBackground),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if(contact.photo != null) {
                val data = ImageHelper.convertImageByteArrayToBitmap(contact.photo!!)
                if(data != null) {
                    Image(data.asImageBitmap(), contentDescription = contact.familyName)
                } else {
                    Image(painterResource(id = R.drawable.baseline_person_24), contentDescription = contact.familyName, colorFilter = ColorFilter.tint(colorForeground))
                }
            } else {
                Image(
                    painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = contact.familyName,
                    colorFilter = ColorFilter.tint(colorForeground)
                )
            }
        }
        Column(
            Modifier
                .weight(5f)
                .padding(5.dp)) {
            Row {
                var prefix = ""
                var suffix = ""
                var family = ""
                val given = "${contact.givenName} "
                if(contact.prefix != null) prefix = "${contact.prefix} "
                if(contact.suffix != null) suffix = "${contact.suffix} "
                if(contact.familyName != null) family = "${contact.familyName} "

                val name = "$prefix$given$family$suffix"
                Text(
                    text = name.trim(),
                    fontWeight = FontWeight.Bold,
                    color = colorForeground
                )
            }
            if(contact.birthDay != null) {
                Row {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    Text(sdf.format(contact.birthDay!!),
                        color = colorForeground)
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            var book = ""
            if(addressBooks[contact.addressBook] != null) {
                book = addressBooks[contact.addressBook]!!
            }

            Text(book, color = colorForeground)
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color = colorForeground)) {}
}

@Composable
fun EditDialog(
    contact: Contact?,
    setShowDialog: (Boolean) -> Unit,
    onSave: (Contact) -> Unit,
    onDelete: (Contact) -> Unit,
    canInsert: Boolean) {

    val fullDay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    var uid by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf(TextFieldValue("")) }
    var prefix by remember { mutableStateOf(TextFieldValue("")) }
    var firstName by remember { mutableStateOf(TextFieldValue("")) }
    var isFirstNameValid by remember { mutableStateOf(contact?.givenName?.isNotEmpty() ?: false ) }
    var lastName by remember { mutableStateOf(TextFieldValue("")) }
    var isLastNameValid by remember { mutableStateOf(true) }
    var additional by remember { mutableStateOf(TextFieldValue("")) }
    var birthDate by remember { mutableStateOf(TextFieldValue(fullDay.format(Date()))) }
    var isBirthDateValid by remember { mutableStateOf(true) }
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
        uid = contact.uid
        suffix = TextFieldValue(contact.suffix!!)
        prefix = TextFieldValue(contact.prefix!!)
        firstName = TextFieldValue(contact.givenName)
        lastName = TextFieldValue(contact.familyName!!)
        birthDate = TextFieldValue(
            if(contact.birthDay!=null) fullDay.format(contact.birthDay!!) else fullDay.format(Date())
        )
        additional = TextFieldValue(contact.additional!!)
        organization = TextFieldValue(contact.organization)
        img.value = contact.photo

        if(contact.phoneNumbers != null) contact.phoneNumbers?.forEach { phones.add(it) }
        if(contact.addresses != null) contact.addresses?.forEach { addresses.add(it) }
        if(contact.emailAddresses != null) contact.emailAddresses?.forEach { mails.add(it) }
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
                Row {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
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

                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(contract =
                ActivityResultContracts.GetContent()) { uri: Uri? ->
                    val data = convertImageToByte(uri, context)
                    img.value = data
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()) {
                    Column(Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Filled.Add, "Gallery")
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
                            value = birthDate,
                            onValueChange = {
                                birthDate = it
                                isBirthDateValid = Validator.checkDate(it.text, "dd.MM.yyyy")
                            },
                            label = {Text(stringResource(R.string.contact_birthDate))},
                            modifier = Modifier.padding(2.dp),
                            isError = !isBirthDateValid
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
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Black)) {}

                TabControl(phones, mails, addresses, uid)

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
                                val bd = fullDay.parse(birthDate.text)
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
                            }, enabled = isFirstNameValid && isLastNameValid && isBirthDateValid) {
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
fun TabControl(phones: MutableList<Phone>, mails: MutableList<Email>, addresses: MutableList<Address>, uid: String) {
    var state by remember { mutableIntStateOf(0) }
    val titles = listOf(
        stringResource(R.string.contact_phones),
        stringResource(R.string.contact_mails),
        stringResource(R.string.contact_addresses)
    )

    Row {
        Column {
            SecondaryTabRow(selectedTabIndex = state) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = { state = index},
                        text = {Text(title, fontSize = 13.sp)}
                    )
                }
            }
            if(state == 0) {
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
fun BottomSheet(contact: Contact, setShowBottomSheet: (Boolean) -> Unit) {
    val name =
        "${contact.suffix} ${contact.givenName} ${contact.familyName} ${contact.prefix}".trim()

    val context = LocalContext.current

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
                        Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            } else {
                Image(
                    painterResource(id = R.drawable.baseline_person_24),
                    contentDescription = name,
                    Modifier
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
                fontSize = 18.sp
            )
        }
        Separator(Color.Black)
        if(contact.birthDay != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val dt = sdf.format(contact.birthDay!!)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(5.dp),
                    horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(id = R.string.contact_birthDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp)
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
                        contact.organization,
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
        Separator(Color.Black)
        if(contact.phoneNumbers != null) {
            contact.phoneNumbers!!.forEach { number ->
                var types = ""
                number.types.forEach { type ->
                    types += "${type.name} "
                }
                types = types.trim()

                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        Modifier
                            .weight(5f)
                            .padding(5.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(
                            types,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    Column(
                        Modifier
                            .weight(14f)
                            .padding(5.dp),
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            number.value,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                    Column(
                        Modifier
                            .weight(2f)
                            .padding(5.dp)) {
                        IconButton(onClick = { openPhone(context, number.value) }) {
                            Icon(Icons.Filled.Phone, number.value)
                        }
                    }
                }
            }

            Separator(Color.Black)
        }
        if(contact.emailAddresses != null) {
            contact.emailAddresses!!.forEach { email ->

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
                        horizontalAlignment = Alignment.Start) {
                        Text(
                            email.value,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Separator(Color.Black)
        }
        if(contact.addresses != null) {
            contact.addresses!!.forEach { address ->
                var types = ""
                address.types.forEach { type ->
                    types += "${type.name} "
                }
                types = types.trim()

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
                            types,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp)
                    }
                    if(address.postOfficeAddress != null) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
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
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(id = R.string.contact_addresses_street),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
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
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(id = R.string.contact_addresses_locality),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
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
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(id = R.string.contact_addresses_region),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
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
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
                            horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(id = R.string.contact_addresses_country),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp)
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(5.dp),
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

            Separator(Color.Black)
        }
        if(contact.contactId != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.Center) {
                Button(onClick = { openContact(context, contact.contactId) }, Modifier.height(50.dp)) {
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
                .weight(7f)
                .height(75.dp)
                .padding(2.dp)
                .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())) {
                PhoneType.entries.forEach { type ->
                    var typeState by remember { mutableStateOf(phone.types.contains(type)) }
                    Row(
                        modifier = Modifier.height(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = typeState, onCheckedChange = {
                            typeState = it
                            if(typeState) {
                                if(!phone.types.contains(type)) {
                                    phone.types.add(type)
                                }
                            } else {
                                if(phone.types.contains(type)) {
                                    phone.types.remove(type)
                                }
                            }
                        }, modifier = Modifier.scale(0.7f))
                        Text(type.name, fontSize = 12.sp)
                    }
                }
            }
        }
        Column(
            Modifier
                .weight(11f)
                .height(75.dp),
            verticalArrangement = Arrangement.Center) {
            OutlinedTextField(
                value = number,
                onValueChange = {
                    number = it
                    phone.value = number
                },
                label = {
                    Text(stringResource(R.string.contact_phone))
                })
        }

        Column(
            Modifier
                .weight(2f)
                .height(75.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onDelete(phone) }) {
                Icon(Icons.Filled.Delete, phone.value)
            }
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(2.dp)
            .background(Color.Black)) {

    }
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
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(2.dp)
            .background(Color.Black)) {

    }
}

@Composable
fun AddressItem(address: Address, onDelete: (Address) -> Unit) {
    var poAddress by remember { mutableStateOf(address.postOfficeAddress ?: "") }
    var street by remember { mutableStateOf(address.street) }
    var postal by remember { mutableStateOf(address.postalCode ?: "") }
    var localty by remember { mutableStateOf(address.locality ?: "") }
    var region by remember { mutableStateOf(address.region ?: "") }
    var ext by remember { mutableStateOf(address.extendedAddress ?: "") }

    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(2.dp)) {
        Column {
            Row {
                Column(
                    Modifier
                        .weight(7f)
                        .height(75.dp)
                        .padding(2.dp)
                        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))) {
                    Column(
                        Modifier
                            .verticalScroll(rememberScrollState())) {
                        AddressType.entries.forEach { type ->
                            var typeState by remember { mutableStateOf(address.types.contains(type)) }
                            Row(
                                modifier = Modifier.height(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = typeState, onCheckedChange = {
                                    typeState = it
                                    if(typeState) {
                                        if(!address.types.contains(type)) {
                                            address.types.add(type)
                                        }
                                    } else {
                                        if(address.types.contains(type)) {
                                            address.types.remove(type)
                                        }
                                    }
                                }, modifier = Modifier.scale(0.7f))
                                Text(type.name, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Column(
                    Modifier
                        .weight(11f)
                        .height(75.dp),
                    verticalArrangement = Arrangement.Center) {
                    OutlinedTextField(
                        value = poAddress,
                        onValueChange = {
                            poAddress = it
                            address.postOfficeAddress = poAddress
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_postOfficeAddress), fontSize=13.sp)
                        })
                }

                Column(
                    Modifier
                        .weight(2f)
                        .height(75.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { onDelete(address) }) {
                        Icon(Icons.Filled.Delete, address.postOfficeAddress)
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(2.dp)) {
                OutlinedTextField(
                    value = street,
                    onValueChange = {
                        street = it
                        address.street = street
                    },
                    label = {
                        Text(stringResource(R.string.contact_addresses_street), fontSize=13.sp)
                    })
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(2.dp)) {
                Column(
                    Modifier
                        .weight(4f)
                        .padding(1.dp)) {
                    OutlinedTextField(
                        value = postal,
                        onValueChange = {
                            postal = it
                            address.postalCode = postal
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_postal), fontSize=13.sp)
                        })
                }
                Column(
                    Modifier
                        .weight(6f)
                        .padding(1.dp)) {
                    OutlinedTextField(
                        value = localty,
                        onValueChange = {
                            localty = it
                            address.locality = localty
                        },
                        label = {
                            Text(stringResource(R.string.contact_addresses_locality), fontSize=13.sp)
                        })
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(2.dp)) {
                OutlinedTextField(
                    value = region,
                    onValueChange = {
                        region = it
                        address.region = region
                    },
                    label = {
                        Text(stringResource(R.string.contact_addresses_region), fontSize=13.sp)
                    })
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(2.dp)) {
                OutlinedTextField(
                    value = address.country ?: "",
                    onValueChange = {
                        address.country = it
                    },
                    label = {
                        Text(stringResource(R.string.contact_addresses_country), fontSize=13.sp)
                    })
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(2.dp)) {
                OutlinedTextField(
                    value = ext,
                    onValueChange = {
                        ext = it
                        address.extendedAddress = ext
                    },
                    label = {
                        Text(stringResource(R.string.contact_addresses_extended), fontSize=13.sp)
                    })
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(2.dp)
                    .background(Color.Black)) {

            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TabControlPreview() {
    CloudAppTheme {
        TabControl(LinkedList(), LinkedList(), LinkedList(), "")
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomSheetPreview() {
    CloudAppTheme {
        BottomSheet(contact = fakeContact(0)) {}
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

fun fakeContact(id: Int): Contact {
    val bDate = Calendar.getInstance()
    bDate.set(1960, 1, 1)
    bDate.add(Calendar.DAY_OF_MONTH, id)
    return Contact(0L,
        "$id", "", "", "", "Doe", "John$id", "",
        bDate.time, "", null, "Test", "", -1L, -1L, 0L)
}