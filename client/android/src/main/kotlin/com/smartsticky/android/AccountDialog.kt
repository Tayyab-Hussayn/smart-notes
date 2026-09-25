package com.smartsticky.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AccountDialog(server: String?, onDismiss: () -> Unit, onAuthenticate: (String, String, String, Boolean) -> Unit) {
    var endpoint by remember { mutableStateOf(server ?: "") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val ready = endpoint.startsWith("https://") && email.isNotBlank() && password.length in 12..128
    fun submit(register: Boolean) {
        onAuthenticate(endpoint, email, password, register)
        password = ""
        onDismiss()
    }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (server == null) "Account and sync" else "Sign in again") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connect to your Smart Notes server over HTTPS. Device-only notes stay in their own workspace.")
            OutlinedTextField(endpoint, { endpoint = it }, label = { Text("HTTPS server address") }, singleLine = true)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
            OutlinedTextField(password, { password = it }, label = { Text("Password (12–128 characters)") },
                singleLine = true, visualTransformation = PasswordVisualTransformation())
            Text("Your session stays in memory. Sign in after restarting the app. Tap Sync notes to synchronize saved notes; wallpaper layouts and reminders stay local.")
            if (server == null) TextButton({ submit(true) }, enabled = ready) { Text("Create account") }
        }
    }, confirmButton = { Button({ submit(false) }, enabled = ready) { Text("Sign in") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } })
}
