package com.example.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.UserProfile
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.SmsHelper
import com.example.ui.theme.*

data class SmsTemplateItem(
    val id: String,
    val title: String,
    val description: String,
    val templateBody: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSmsScreen(
    agent: UserProfile,
    repository: MicrofinanceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by repository.customers.collectAsState()
    val myCustomers = customers.filter { it.agentId == agent.id }

    var selectedTab by remember { mutableStateOf("Templates") }
    var selectedTemplate by remember { mutableStateOf<SmsTemplateItem?>(null) }

    // Compose state
    var recipientPhone by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val templates = listOf(
        SmsTemplateItem(
            id = "tmpl_1",
            title = "Loan application received",
            description = "Your loan application has been received...",
            templateBody = "Habari {name}, maombi yako ya mkopo wa TSh 100,000 yamepokelewa na yanafanyiwa kazi. — Lay Microfinance"
        ),
        SmsTemplateItem(
            id = "tmpl_2",
            title = "Loan approved",
            description = "Your loan has been approved...",
            templateBody = "Habari {name}, mkopo wako umeidhinishwa na Lay Microfinance! Tafadhali subiri uthibitisho wa kutumiwa fedha. — Lay Microfinance"
        ),
        SmsTemplateItem(
            id = "tmpl_3",
            title = "Repayment reminder",
            description = "This is a reminder that your repayment...",
            templateBody = "Habari {name}, kumbuka kulipa marejesho ya leo kabla ya saa 12:00 jioni (6:00 PM). Wasiliana na wakala wako kufanya malipo. — Lay Microfinance"
        ),
        SmsTemplateItem(
            id = "tmpl_4",
            title = "Payment received",
            description = "Thank you for your payment...",
            templateBody = "Habari {name}, tumepokea malipo yako. Asante kwa uaminifu wako na kukuza maisha yako. — Lay Microfinance"
        ),
        SmsTemplateItem(
            id = "tmpl_5",
            title = "Overdue reminder",
            description = "Your loan is overdue. Please pay...",
            templateBody = "Habari {name}, rejesho lako la mkopo limechelewa. Tafadhali wasiliana na wakala wako kufanya malipo mara moja. — Lay Microfinance"
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ujumbe (SMS)",
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
                .padding(horizontal = 16.dp)
        ) {
            // Segmented Tab Button
            TabRow(
                selectedTabIndex = if (selectedTab == "Templates") 0 else 1,
                containerColor = Color.White,
                contentColor = LayNavyPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == "Templates",
                    onClick = { selectedTab = "Templates" },
                    text = { Text("Templates", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == "Compose",
                    onClick = { selectedTab = "Compose" },
                    text = { Text("Compose", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == "Templates") {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(templates) { tmpl ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTemplate = tmpl
                                    messageBody = tmpl.templateBody
                                    selectedTab = "Compose"
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(LayNavyPrimary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = LayNavyPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tmpl.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LayTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tmpl.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LayTextSecondary
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = LayTextTertiary
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                messageBody = ""
                                selectedTab = "Compose"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("new_message_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ New Message")
                        }
                    }
                }
            } else {
                // Compose tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mpokeaji (Recipient)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = LayTextPrimary
                                )
                                TextButton(onClick = { showCustomerPicker = true }) {
                                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chagua Mteja", fontSize = 12.sp)
                                }
                            }

                            if (recipientName.isNotBlank()) {
                                Text("Mteja Aliyechaguliwa: $recipientName", fontWeight = FontWeight.SemiBold, color = LayGreenDark, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            OutlinedTextField(
                                value = recipientPhone,
                                onValueChange = { recipientPhone = it },
                                label = { Text("Namba ya Simu (+255...)") },
                                modifier = Modifier.fillMaxWidth().testTag("sms_recipient_phone"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Ujumbe (Message Text)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LayTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = messageBody,
                                onValueChange = { messageBody = it },
                                placeholder = { Text("Andika ujumbe wako hapa...") },
                                modifier = Modifier.fillMaxWidth().testTag("sms_body_input"),
                                minLines = 5,
                                maxLines = 8
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (recipientPhone.isNotBlank() && messageBody.isNotBlank()) {
                                SmsHelper.launchSmsComposer(context, recipientPhone, messageBody)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("launch_sms_composer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        enabled = recipientPhone.isNotBlank() && messageBody.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fungua SMS ya Simu (Send via Android)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                }
            }
        }
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            title = { Text("Chagua Mteja Wako") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(myCustomers) { cust ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    recipientName = cust.fullName
                                    recipientPhone = cust.phone
                                    if (messageBody.contains("{name}")) {
                                        messageBody = messageBody.replace("{name}", cust.fullName)
                                    }
                                    showCustomerPicker = false
                                },
                            colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(cust.fullName, fontWeight = FontWeight.Bold)
                                Text(cust.phone, fontSize = 12.sp, color = LayTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerPicker = false }) {
                    Text("Funga")
                }
            }
        )
    }
}
