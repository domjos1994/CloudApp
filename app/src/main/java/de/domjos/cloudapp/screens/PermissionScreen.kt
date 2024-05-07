package de.domjos.cloudapp.screens

import android.accounts.Account
import android.accounts.AccountManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import de.domjos.cloudapp.services.AuthenticatorService

@Composable
fun PermissionScreen(viewModel: PermissionViewModel = hiltViewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (header, permissions, footer) = createRefs()
        val account = createSyncAccount()

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionItem(
                    stringResource(R.string.permissions_notifications_title),
                    stringResource(R.string.permissions_notifications_summary),
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
                ) { createNotificationChannel(context) }
            } else {
                createNotificationChannel(context)
            }
            PermissionItem(
                stringResource(R.string.permissions_contacts_title),
                stringResource(R.string.permissions_contacts_summary),
                arrayOf(android.Manifest.permission.WRITE_CONTACTS)
            ) { addContactSync(account, viewModel.getContactRegularitySetting()) }
            PermissionItem(
                stringResource(R.string.permissions_calendar_title),
                stringResource(R.string.permissions_calendar_summary),
                arrayOf(android.Manifest.permission.WRITE_CALENDAR, android.Manifest.permission.READ_CALENDAR)
            ) { addCalendarSync(account, viewModel.getCalendarRegularitySetting()) }
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

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if(isGranted) {
            enabled = false
            if(action != null) {
                action()
            }
        }
    }

    permissions.forEach {
        if(ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
            enabled = true
        }
    }

    PermissionComponent(title, description, enabled) {
        permissions.forEach {
            launcher.launch(it)
        }
    }
}

@Composable
fun PermissionComponent(title: String, description: String, enabled: Boolean, onRequest: () -> Unit) {
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
            Row(Modifier.wrapContentHeight()) {
                Button(onClick = { onRequest() }, enabled=enabled) {
                    Text(stringResource(id = R.string.permissions_grant))
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

@Composable
private fun createSyncAccount(): Account {
    val account = AuthenticatorService.getAccount(LocalContext.current, "de.domjos.cloudapp.account")
    val accountManager = LocalContext.current.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    accountManager.addAccountExplicitly(account, null, null)

    return account
}

private fun addContactSync(account: Account, contactRegularity: Float) {
    // contact
    ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1)
    ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
    ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, Bundle(), (contactRegularity * 60 * 1000).toLong())
}

private fun addCalendarSync(account: Account, calendarRegularity: Float) {
    // calendar
    ContentResolver.setIsSyncable(account, CalendarContract.AUTHORITY, 1)
    ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)
    ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, Bundle(), (calendarRegularity * 60 * 1000).toLong())
}

private fun createNotificationChannel(context: Context) {
    val channel = NotificationChannel("cloud_app_notifications", "CloudApp", NotificationManager.IMPORTANCE_DEFAULT)
    val manager = context.getSystemService(ComponentActivity.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}