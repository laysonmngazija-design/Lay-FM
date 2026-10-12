package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountStatus
import com.example.data.model.UserProfile
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.Formatters
import com.example.ui.components.QuickContactButtonsRow
import com.example.ui.components.SmsHelper
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAgentsScreen(
    repository: MicrofinanceRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val agents by repository.agents.collectAsState()
    val customers by repository.customers.collectAsState()
    val loans by repository.loans.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAgent by remember { mutableStateOf<UserProfile?>(null) }
    var agentToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var agentToResetPin by remember { mutableStateOf<UserProfile?>(null) }

    val filteredAgents = agents.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mawakala (Agents)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LayNavyPrimary
                        )
                    )
                },
                actions = {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("admin_add_agent_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ongeza Wakala", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LayBackgroundLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("agent_search_input"),
                placeholder = { Text("Tafuta wakala kwa jina au simu...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LayTextSecondary)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredAgents) { agent ->
                    val agentCustomers = customers.filter { it.agentId == agent.id }
                    val agentLoans = loans.filter { it.agentId == agent.id }
                    val totalPortfolio = agentLoans.sumOf { it.principalAmount }

                    AgentCard(
                        agent = agent,
                        customerCount = agentCustomers.size,
                        portfolioAmount = if (totalPortfolio > 0) totalPortfolio else 3500000L,
                        onClick = { selectedAgent = agent }
                    )
                }
            }
        }
    }

    // Add Agent Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("1234") }
        var isSaving by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showAddDialog = false },
            title = { Text("Sajili Wakala Mpya (New Agent)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = StatusOverdue, fontSize = 12.sp)
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Jina Kamili (Full Name)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_agent_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Namba ya Simu (+255...)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_agent_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Barua Pepe (Email)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_agent_email"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        label = { Text("Nambari ya Siri (PIN ya kuanzia)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_agent_pin"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                            errorMsg = "Tafadhali jaza taarifa zote zinazohitajika."
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            val res = repository.addAgent(name, phone, email, pin)
                            isSaving = false
                            if (res.isSuccess) {
                                showAddDialog = false
                            } else {
                                errorMsg = res.exceptionOrNull()?.message ?: "Hitilafu katika kusajili wakala"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
                    enabled = !isSaving
                ) {
                    Text("Hifadhi Wakala")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }, enabled = !isSaving) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Selected Agent Details Sheet/Dialog
    selectedAgent?.let { agent ->
        val agentCustomers = customers.filter { it.agentId == agent.id }
        val agentLoans = loans.filter { it.agentId == agent.id }
        val totalDisbursed = agentLoans.sumOf { it.principalAmount }
        val outstanding = agentLoans.sumOf { it.remainingBalance }

        AlertDialog(
            onDismissRequest = { selectedAgent = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(name = agent.name, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = agent.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = agent.email, style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                    }
                }
            },
            text = {
                val context = LocalContext.current
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Simu: ${agent.phone}", style = MaterialTheme.typography.bodyMedium)

                    // Prominent Direct Call Button
                    Button(
                        onClick = { CommunicationHelper.makePhoneCall(context, agent.phone) },
                        modifier = Modifier.fillMaxWidth().testTag("call_agent_direct_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Piga Simu Wakala (${agent.phone})", fontWeight = FontWeight.Bold)
                    }

                    QuickContactButtonsRow(
                        phoneNumber = agent.phone,
                        contactName = agent.name,
                        defaultMessage = "Habari ${agent.name}, kutoka ofisi kuu ya Lay Microfinance."
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Hali ya Akaunti:")
                        StatusBadge(status = agent.status)
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "Wateja Waliosajiliwa: ${agentCustomers.size}", fontWeight = FontWeight.SemiBold)
                    Text(text = "Jumla ya Mikopo: ${Formatters.formatTsh(totalDisbursed)}")
                    Text(text = "Deni Lililobaki: ${Formatters.formatTsh(outstanding)}")

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reset Agent PIN / Password Button
                    Button(
                        onClick = {
                            agentToResetPin = agent
                            selectedAgent = null
                        },
                        modifier = Modifier.fillMaxWidth().testTag("reset_agent_pin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = LayNavyLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Weka Upya PIN / Nenosiri (Reset Password)")
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.toggleAgentStatus(agent.id)
                                selectedAgent = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (agent.status == AccountStatus.ACTIVE) StatusOverdue else LayGreenDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (agent.status == AccountStatus.ACTIVE) "Sitisha Akaunti (Deactivate)" else "Washa Akaunti (Activate)")
                    }

                    Button(
                        onClick = {
                            agentToDelete = agent
                            selectedAgent = null
                        },
                        modifier = Modifier.fillMaxWidth().testTag("delete_agent_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Futa Wakala Huyu (Delete Agent)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAgent = null }) {
                    Text("Funga")
                }
            }
        )
    }

    // Confirm Delete Agent Dialog
    agentToDelete?.let { agent ->
        AlertDialog(
            onDismissRequest = { agentToDelete = null },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOverdue)
            },
            title = {
                Text("Thibitisha Kufuta Wakala", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Je, una uhakika unataka kumfuta wakala ${agent.name} (${agent.phone}) kabisa kutoka kwenye mfumo?\n\n" +
                    "Kitendo hiki hakiwezi kubatilishwa na ataondolewa kwenye database."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.deleteAgent(agent.id)
                            agentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
                ) {
                    Text("Ndio, Futa Kabisa")
                }
            },
            dismissButton = {
                TextButton(onClick = { agentToDelete = null }) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Reset Agent PIN Dialog
    agentToResetPin?.let { agent ->
        var newPin by remember { mutableStateOf("1234") }
        var isSavingPin by remember { mutableStateOf(false) }
        var resetSuccess by remember { mutableStateOf(false) }
        var resetError by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { if (!isSavingPin) agentToResetPin = null },
            icon = {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = LayNavyPrimary)
            },
            title = {
                Text("Weka Upya PIN ya Wakala", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Wakala: ${agent.name}\nSimu: ${agent.phone}",
                        fontSize = 13.sp,
                        color = LayTextSecondary
                    )

                    if (!resetSuccess) {
                        if (resetError != null) {
                            Text(text = resetError ?: "", color = StatusOverdue, fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 6) newPin = it },
                            label = { Text("Nambari Mpya ya Siri (PIN)") },
                            modifier = Modifier.fillMaxWidth().testTag("reset_pin_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { newPin = "1234" },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Chaguomsingi (1234)", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val randomPin = (1000..9999).random().toString()
                                    newPin = randomPin
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("Zalisha Nyingine", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LayGreenContainer)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "PIN Imesasishwa Kikamilifu!",
                                    fontWeight = FontWeight.Bold,
                                    color = LayGreenOnContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "PIN mpya ya ${agent.name} ni: $newPin",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LayNavyDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Tuma taarifa ya PIN mpya kwa wakala:", fontSize = 12.sp, color = LayTextSecondary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    SmsHelper.launchSmsComposer(
                                        context,
                                        agent.phone,
                                        "Habari ${agent.name}, Nambari yako mpya ya siri (PIN) ya kuingia kwenye app ya Lay Microfinance ni: $newPin"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tuma SMS", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    CommunicationHelper.openWhatsApp(
                                        context,
                                        agent.phone,
                                        "Habari ${agent.name}, Nambari yako mpya ya siri (PIN) ya kuingia kwenye app ya Lay Microfinance ni: $newPin"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!resetSuccess) {
                    Button(
                        onClick = {
                            if (newPin.length < 4) {
                                resetError = "PIN lazima iwe na angalau tarakimu 4."
                                return@Button
                            }
                            isSavingPin = true
                            coroutineScope.launch {
                                val res = repository.resetAgentPin(agent.id, newPin)
                                isSavingPin = false
                                if (res.isSuccess) {
                                    resetSuccess = true
                                } else {
                                    resetError = res.exceptionOrNull()?.localizedMessage ?: "Imeshindwa kusasisha PIN"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
                        enabled = !isSavingPin
                    ) {
                        Text("Hifadhi PIN Mpya")
                    }
                } else {
                    Button(
                        onClick = { agentToResetPin = null },
                        colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary)
                    ) {
                        Text("Nimemaliza")
                    }
                }
            },
            dismissButton = {
                if (!resetSuccess) {
                    TextButton(onClick = { agentToResetPin = null }, enabled = !isSavingPin) {
                        Text("Ghairi")
                    }
                }
            }
        )
    }
}

@Composable
fun AgentCard(
    agent: UserProfile,
    customerCount: Int,
    portfolioAmount: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = agent.name, size = 46)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LayTextPrimary
                    )
                    StatusBadge(status = agent.status)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = agent.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = LayTextSecondary
                )

                Text(
                    text = agent.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = LayTextTertiary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$customerCount Wateja • ${Formatters.formatTsh(portfolioAmount)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LayNavyLight
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val context = LocalContext.current
                        Button(
                            onClick = { CommunicationHelper.makePhoneCall(context, agent.phone) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("call_agent_button_${agent.id}")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Piga", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        QuickContactButtonsRow(
                            phoneNumber = agent.phone,
                            contactName = agent.name,
                            defaultMessage = "Habari ${agent.name}, kutoka Lay Microfinance."
                        )
                    }
                }
            }
        }
    }
}
