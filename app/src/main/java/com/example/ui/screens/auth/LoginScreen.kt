package com.example.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var identifier by remember { mutableStateOf("laysonmngazija@gmail.com") }
    var pin by remember { mutableStateOf("9552") }
    var showPin by remember { mutableStateOf(false) }
    var rememberDevice by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }

    fun doLogin() {
        if (identifier.isBlank() || pin.isBlank()) {
            errorMessage = "Tafadhali jaza namba ya simu/barua pepe na PIN."
            return
        }
        errorMessage = null
        isLoading = true
        focusManager.clearFocus()

        coroutineScope.launch {
            val result = authRepository.login(identifier, pin, rememberDevice)
            isLoading = false
            result.onSuccess { user ->
                onLoginSuccess(user)
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Imeshindwa kuingia. Tafadhali jaribu tena."
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.lay_logo),
                    contentDescription = "Lay Microfinance Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lay Microfinance",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = LayNavyPrimary
                )
            )

            Text(
                text = "Kukuza Maisha Yako",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = LayGreenDark
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingia kwenye akaunti yako (Admin au Wakala)",
                style = MaterialTheme.typography.bodySmall,
                color = LayTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Quick Fill Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        identifier = "laysonmngazija@gmail.com"
                        pin = "9552"
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("demo_admin_button"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LayNavyPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Admin (9552)", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        identifier = "0717998877"
                        pin = "1234"
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("demo_agent_button"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = LayGreenDark
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Agent (1234)", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusOverdueBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = StatusOverdue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusOverdue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input: Identifier
            OutlinedTextField(
                value = identifier,
                onValueChange = {
                    identifier = it
                    errorMessage = null
                },
                label = { Text("Barua Pepe au Simu (Email / Phone)") },
                placeholder = { Text("laysonmngazija@gmail.com au 0717798877") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = LayNavyPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_identifier_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: PIN
            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= 6) pin = it
                    errorMessage = null
                },
                label = { Text("Nambari ya Siri (PIN)") },
                placeholder = { Text("Ingiza tarakimu 4") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LayNavyPrimary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showPin = !showPin }) {
                        Icon(
                            imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPin) "Ficha PIN" else "Onyesha PIN",
                            tint = LayTextSecondary
                        )
                    }
                },
                visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_pin_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { doLogin() }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remember device & Forgot PIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberDevice,
                        onCheckedChange = { rememberDevice = it },
                        modifier = Modifier.testTag("remember_device_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = LayNavyPrimary
                        )
                    )
                    Text(
                        text = "Kumbuka kifaa hiki",
                        style = MaterialTheme.typography.bodySmall,
                        color = LayTextPrimary
                    )
                }

                TextButton(
                    onClick = { showForgotDialog = true }
                ) {
                    Text(
                        text = "Umesahau PIN?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = LayNavyLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { doLogin() },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LayNavyPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ingia (Login)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Toleo 1.0.0 • com.lay.app\nLay Microfinance Tanzania",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = LayTextTertiary
                ),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text("Kurejesha Nambari ya Siri (PIN)")
            },
            text = {
                Text(
                    "Kwa mawakala (Agents): Wasiliana na msimamizi wako mkuu (Admin) kubadilisha au kuweka upya PIN yako.\n\n" +
                    "Kwa Admin (Layson Mngazija): Nambari chaguomsingi ni 9552. Unaweza kubadilisha kupitia mipangilio ya akaunti baada ya kuingia."
                )
            },
            confirmButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Nimeelewa")
                }
            }
        )
    }
}
