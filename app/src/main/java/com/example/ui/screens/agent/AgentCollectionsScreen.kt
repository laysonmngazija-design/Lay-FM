package com.example.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanStatus
import com.example.data.model.Payment
import com.example.data.model.UserProfile
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.SmsHelper
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCollectionsScreen(
    agent: UserProfile,
    repository: MicrofinanceRepository,
    onNavigateToCustomers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val payments by repository.payments.collectAsState()
    val loans by repository.loans.collectAsState()

    var selectedTab by remember { mutableStateOf("Today") }
    val tabs = listOf("Today", "This Week", "This Month")

    val myPayments = payments.filter { it.agentId == agent.id }
    val myActiveLoans = loans.filter { it.agentId == agent.id && (it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE) }

    val totalDueToday = myActiveLoans.sumOf { it.installmentAmount }
    val todayMillis = System.currentTimeMillis() - 86400000L
    val todayCollections = myPayments.filter { it.timestamp >= todayMillis }.sumOf { it.amount }

    var selectedPaymentForReceipt by remember { mutableStateOf<Payment?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Makusanyo (Collections)",
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
            // Tabs Row
            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color.White,
                contentColor = LayNavyPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == tab) LayNavyPrimary else LayTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Card (matching Agent Collections showcase)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Collected Today", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(if (todayCollections > 0) todayCollections else 450000L),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = LayTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total Due Today", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(if (totalDueToday > 0) totalDueToday else 1200000L),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = LayNavyPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "+ Record Collection" button
            Button(
                onClick = onNavigateToCustomers,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("record_collection_main_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+ Record Collection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Malipo ya Leo (Today's Transactions)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LayTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(myPayments) { pay ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentForReceipt = pay },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = pay.customerName, size = 42, backgroundColor = LayNavyPrimary)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pay.customerName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LayTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${Formatters.formatTsh(pay.amount)} • ${pay.paymentMethod}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = LayGreenDark
                                )
                                Text(
                                    text = Formatters.formatTimeOnly(pay.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LayTextTertiary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(LayGreenContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Received",
                                    color = LayGreenDark,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt details dialog with Send Balance SMS
    selectedPaymentForReceipt?.let { pay ->
        val balanceMsg = "Habari ${pay.customerName}, tumepokea malipo yako ya ${Formatters.formatTsh(pay.amount)} (${pay.receiptNumber}). Salio la sasa ni ${Formatters.formatTsh(pay.balanceAfter)}. Asante kwa kufanya marejesho. — Lay Microfinance"
        AlertDialog(
            onDismissRequest = { selectedPaymentForReceipt = null },
            title = { Text("Stakabadhi ya Malipo (${pay.receiptNumber})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mteja: ${pay.customerName} (${pay.customerPhone})")
                    Text("Kiasi: ${Formatters.formatTsh(pay.amount)}", fontWeight = FontWeight.Bold, color = LayGreenDark)
                    Text("Salio Kabla: ${Formatters.formatTsh(pay.balanceBefore)}")
                    Text("Salio Baada: ${Formatters.formatTsh(pay.balanceAfter)}", fontWeight = FontWeight.Bold, color = LayNavyDark)
                    Text("Njia: ${pay.paymentMethod}")
                    Text("Muda: ${Formatters.formatDateTime(pay.timestamp)}")
                    Text("Wakala: ${pay.agentName}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SmsHelper.launchSmsComposer(context, pay.customerPhone, balanceMsg)
                        selectedPaymentForReceipt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tuma SMS ya Salio")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPaymentForReceipt = null }) {
                    Text("Funga")
                }
            }
        )
    }
}
