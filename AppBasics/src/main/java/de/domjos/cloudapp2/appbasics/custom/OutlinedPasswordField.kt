package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import de.domjos.cloudapp2.appbasics.R

@Composable
fun OutlinedPasswordField(value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit, label: Int) {
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        label = { Text(stringResource(id = label)) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility else Icons.Filled.VisibilityOff

            // Please provide localized description for accessibility services
            val pwdDescr = if (passwordVisible) context.getString(R.string.login_pwd_hide) else context.getString(
                R.string.login_pwd_show)

            IconButton(onClick = {passwordVisible = !passwordVisible}){
                Icon(imageVector  = image, pwdDescr)
            }
        }
    )
}