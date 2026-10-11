package com.example.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.ui.components.UserAvatar
import com.example.ui.screens.admin.AccountMenuItem
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentAccountScreen(
    agent: UserProfile,
    authRepository: AuthRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Akaunti (Account)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LayNavyPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LayBackgroundLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card (matching showcase)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = agent.name, size = 60, backgroundColor = LayGreenDark)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = LayTextPrimary
                        )
                        Text(
                            text = "Wakala (Agent)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LayGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = agent.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                        Text(
                            text = agent.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                    }
                }
            }

            // Options List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Default.Lock,
                        title = "Badilisha PIN (Change PIN)",
                        onClick = { showChangePinDialog = true }
                    )
                    Divider(color = LayBorderLight)
                    AccountMenuItem(
                        icon = Icons.Default.Person,
                        title = "Wasifu Wangu (Profile Settings)",
                        onClick = { }
                    )
                    Divider(color = LayBorderLight)
                    AccountMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Taarifa (Notification Preferences)",
                        onClick = { }
                    )
                    Divider(color = LayBorderLight)
                    AccountMenuItem(
                        icon = Icons.Default.Info,
                        title = "Kuhusu Lay Microfinance",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logout Button
            Button(
                onClick = {
                    authRepository.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("agent_logout_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ondoka (Logout)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Weka PIN Mpya ya Wakala") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingiza tarakimu 4 za PIN yako mpya:")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        modifier = Modifier.fillMaxWidth().testTag("agent_new_pin_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length == 4) {
                            isSaving = true
                            coroutineScope.launch {
                                authRepository.changePin(agent.id, newPin)
                                isSaving = false
                                showChangePinDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                    enabled = newPin.length == 4 && !isSaving
                ) {
                    Text("Hifadhi PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Ghairi")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Lay Microfinance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kukuza Maisha Yako", fontWeight = FontWeight.Bold, color = LayGreenDark)
                    Text("Wakala App v1.0.0")
                    Text("Muda wa Kazi: Kila siku hadi saa 12:00 jioni.")
                    Text("Hakikisha unarekodi fedha zote za wateja zilizopokelewa mara moja.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Sawa")
                }
            }
        )
    }
}
