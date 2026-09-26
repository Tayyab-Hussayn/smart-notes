package com.smartsticky.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartsticky.sync.BackendClient
import com.smartsticky.sync.BackendException
import com.smartsticky.sync.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AccountDialog(onDismiss: () -> Unit, onSignedIn: (BackendClient, Session) -> Unit) {
    var endpoint by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localDevelopment by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun authenticate(register: Boolean) {
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val client = BackendClient(endpoint.trim(), allowLocalHttp = localDevelopment)
                    client to if (register) client.register(email.trim(), password) else client.login(email.trim(), password)
                }
                password = ""
                onSignedIn(result.first, result.second)
            } catch (_: BackendException) { message = "Sign-in failed. Check your details and server availability." }
            catch (_: Exception) { message = "Use a valid HTTPS server address. HTTP is allowed only for local development." }
            finally { busy = false }
        }
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Account and sync") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connect to your Smart Notes server. Device-only notes stay in the local workspace.")
            OutlinedTextField(endpoint, { endpoint = it }, label = { Text("Server URL") }, enabled = !busy)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, enabled = !busy)
            OutlinedTextField(password, { password = it }, label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(), enabled = !busy)
            Row { Checkbox(localDevelopment, { localDevelopment = it }, enabled = !busy); Text("Allow localhost HTTP for development") }
            Text("The session stays in memory and expires after 30 minutes. Sign in again after restarting.")
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(enabled = !busy && password.length >= 12, onClick = { authenticate(true) }) { Text("Create account") }
        }
    }, confirmButton = { Button(enabled = !busy && password.isNotBlank(), onClick = { authenticate(false) }) { Text("Sign in") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Cancel") } })
}
