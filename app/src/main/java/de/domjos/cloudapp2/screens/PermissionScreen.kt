package de.domjos.cloudapp2.screens

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp2.appbasics.helper.Notifications
import de.domjos.cloudapp2.appbasics.helper.hasPermission

@Composable
fun PermissionScreen(viewModel: PermissionViewModel = hiltViewModel(), onBack: () -> Unit) {
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (header, permissions, footer) = createRefs()
        val context = LocalContext.current

        viewModel.message.observe(LocalLifecycleOwner.current) { msg ->
            msg?.let {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.message.value = null
            }
        }

        Row(
            Modifier
                .padding(10.dp)
                .constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.permissions), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
        }
        Column(
            Modifier
                .constrainAs(permissions) {
                    top.linkTo(header.bottom)
                    bottom.linkTo(footer.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .verticalScroll(rememberScrollState())
        ) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                PermissionItem(
                    stringResource(R.string.permissions_write_files_title),
                    stringResource(R.string.permissions_write_files_summary),
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            }
            PermissionItem(
                stringResource(R.string.permissions_internet_title),
                stringResource(R.string.permissions_internet_summary),
                arrayOf(android.Manifest.permission.INTERNET)
            )
            val name = stringResource(de.domjos.cloudapp2.R.string.channel_general)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    stringResource(R.string.permissions_notifications_title),
                    stringResource(R.string.permissions_notifications_summary),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
                ) { Notifications.createIfNotExists(context, Notifications.channel_id, name) }
            } else {
                Notifications.createIfNotExists(context, Notifications.channel_id, name)
            }
            PermissionItem(
                stringResource(R.string.permissions_contacts_title),
                stringResource(R.string.permissions_contacts_summary),
                arrayOf(android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS)
            ) {
                viewModel.initContactSync(context, viewModel.getContactRegularitySetting())
            }
            PermissionItem(
                stringResource(R.string.permissions_calendar_title),
                stringResource(R.string.permissions_calendar_summary),
                arrayOf(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)
            ) {
                viewModel.initCalendarSync(context, viewModel.getCalendarRegularitySetting())
            }
            PermissionItem(
                stringResource(R.string.permissions_phone_title),
                stringResource(R.string.permissions_phone_summary),
                arrayOf(android.Manifest.permission.CALL_PHONE)
            ) { }
        }
        Row(
            Modifier
                .padding(10.dp)
                .constrainAs(footer) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onBack() }) {
                Text(stringResource(R.string.permissions_back))
            }
        }
    }

}


@Composable
fun PermissionItem(title: String, description: String, permissions: Array<String>, action: (() -> Unit)? = null) {
    var enabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        map.entries.forEach { element ->
            if(element.value) {
                enabled = false
                if(action != null) {
                    action()
                }
            }
        }
    }

    permissions.forEach {
        if(!hasPermission(it, context)) {
            enabled = true
        }
    }

    PermissionComponent(title, description, enabled) {
        launcher.launch(permissions)
    }
}

@Composable
fun PermissionComponent(title: String, description: String, enabled: Boolean, onRequest: (Boolean) -> Unit) {
    Row {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(5.dp)) {
            Row(Modifier.wrapContentHeight()) {
                Text(title, fontSize = 16.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.wrapContentHeight()) {
                Text(description, fontSize = 12.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
            }
            Row(Modifier.height(70.dp)) {
                Column(
                    modifier = Modifier.weight(9f),
                    verticalArrangement = Arrangement.Center) {
                    val text = stringResource(id = R.string.permissions_grant)
                    Text(text, Modifier.semantics {contentDescription= "$text $title"})
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center) {
                    Switch(checked = !enabled, onCheckedChange = {onRequest(it)})
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewPermissionComponent() {
    CloudAppTheme {
        PermissionComponent(title = "Test", description = "This is a Test", true) {}
    }
}