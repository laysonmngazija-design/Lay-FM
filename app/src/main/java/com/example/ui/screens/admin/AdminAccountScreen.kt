package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAccountScreen(
    user: UserProfile,
    authRepository: AuthRepository,
    repository: MicrofinanceRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
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
            // Profile Card
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
                    UserAvatar(name = user.name, size = 60, backgroundColor = LayNavyPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = LayTextPrimary
                        )
                        Text(
                            text = "Msimamizi Mkuu (Admin)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LayGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                        Text(
                            text = user.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                    }
                }
            }

            // Menu Section
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
                        icon = Icons.Default.History,
                        title = "Kumbukumbu za Ukaguzi (Audit Logs)",
                        onClick = { showAuditLogsDialog = true }
                    )
                    Divider(color = LayBorderLight)
                    AccountMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Taarifa (Notifications)",
                        onClick = { repository.markNotificationsRead() }
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
                    .testTag("admin_logout_button"),
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

    // Change PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Weka PIN Mpya") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ingiza tarakimu 4 za PIN yako mpya:")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        modifier = Modifier.fillMaxWidth().testTag("new_pin_input"),
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
                                authRepository.changePin(user.id, newPin)
                                isSaving = false
                                showChangePinDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
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

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        val auditLogs by repository.auditLogs.collectAsState()
        AlertDialog(
            onDismissRequest = { showAuditLogsDialog = false },
            title = { Text("Audit Logs (Kumbukumbu)") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    auditLogs.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = log.details, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = "${log.actorName} (${log.actorRole})", fontSize = 11.sp, color = LayNavyPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuditLogsDialog = false }) {
                    Text("Funga")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Lay Microfinance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Kauli Mbiu: Kukuza Maisha Yako", fontWeight = FontWeight.Bold, color = LayGreenDark)
                    Text("Toleo: 1.0.0 (Production)")
                    Text("Mkoa: Dar es Salaam, Tanzania")
                    Text("Timezone: Africa/Dar_es_Salaam")
                    Text("Muda wa Marejesho: Kila siku kabla ya saa 12:00 jioni (6:00 PM)")
                    Text("Mfumo unalinda taarifa zote kupitia Firebase Firestore & Cloud Rules.")
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

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LayNavyPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = LayTextPrimary
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = LayTextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
