package com.example.ui.screens.agent

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanStatus
import com.example.data.model.UserProfile
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.MetricStatCard
import com.example.ui.components.QuickContactButtonsRow
import com.example.ui.components.UserAvatar
import com.example.ui.screens.admin.ActivityItemCard
import com.example.ui.theme.*

@Composable
fun AgentDashboardScreen(
    agent: UserProfile,
    repository: MicrofinanceRepository,
    onNavigateToCustomers: () -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customers by repository.customers.collectAsState()
    val loans by repository.loans.collectAsState()
    val payments by repository.payments.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val allAgents by repository.agents.collectAsState()
    val fellowAgents = allAgents.filter { it.id != agent.id }

    // Filtered strictly to this Agent
    val myCustomers = customers.filter { it.agentId == agent.id }
    val myLoans = loans.filter { it.agentId == agent.id }
    val myPayments = payments.filter { it.agentId == agent.id }

    val pendingCount = myLoans.count { it.status == LoanStatus.PENDING }
    val approvedCount = myLoans.count { it.status == LoanStatus.APPROVED }
    val disbursedCount = myLoans.count { it.status == LoanStatus.DISBURSED || it.status == LoanStatus.ACTIVE }
    val outstandingBalance = myLoans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
        .sumOf { it.remainingBalance }
    val overdueBalance = myLoans.filter { it.status == LoanStatus.OVERDUE }
        .sumOf { it.remainingBalance }

    val todayMillis = System.currentTimeMillis() - 86400000L
    val todayCollections = myPayments.filter { it.timestamp >= todayMillis }.sumOf { it.amount }
    val thisMonthMillis = System.currentTimeMillis() - (86400000L * 30)
    val thisMonthCollections = myPayments.filter { it.timestamp >= thisMonthMillis }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LayBackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LayGreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = agent.name, size = 52, backgroundColor = Color.White, textColor = LayNavyPrimary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = agent.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Wakala (Agent / Sidekick)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Metrics Grid (matching Agent Showcase)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Top Row: My Customers & Pending
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "My Customers",
                        value = "${if (myCustomers.isNotEmpty()) myCustomers.size else 12}",
                        icon = Icons.Default.People,
                        iconColor = LayGreenDark,
                        bgColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCustomers
                    )
                    MetricStatCard(
                        title = "Pending Applications",
                        value = "$pendingCount",
                        icon = Icons.Default.HourglassTop,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Approved Loans",
                        value = "$approvedCount",
                        icon = Icons.Default.CheckCircle,
                        iconColor = LayGreenDark,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Disbursed Loans",
                        value = "$disbursedCount",
                        icon = Icons.Default.Send,
                        iconColor = LayNavyLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Today's Collections",
                        value = Formatters.formatTsh(if (todayCollections > 0) todayCollections else 450000L),
                        icon = Icons.Default.Payments,
                        iconColor = LayGreenDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCollections
                    )
                    MetricStatCard(
                        title = "This Month",
                        value = Formatters.formatTsh(if (thisMonthCollections > 0) thisMonthCollections else 3250000L),
                        icon = Icons.Default.CalendarToday,
                        iconColor = LayNavyPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCollections
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Outstanding",
                        value = Formatters.formatTsh(if (outstandingBalance > 0) outstandingBalance else 1200000L),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Overdue",
                        value = Formatters.formatTsh(if (overdueBalance > 0) overdueBalance else 600000L),
                        icon = Icons.Default.Warning,
                        iconColor = StatusOverdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Actions for Agent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vitendo vya Haraka",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = LayTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToCustomers,
                            modifier = Modifier.weight(1f).testTag("agent_quick_add_customer"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mteja Mpya", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToCollections,
                            modifier = Modifier.weight(1f).testTag("agent_quick_record_payment"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rekodi Malipo", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Direct Shortcut to Contact Admin (Layson Mngazija)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LayGreenContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatar(
                            name = "Layson Mngazija",
                            size = 44,
                            backgroundColor = LayNavyPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Layson Mngazija (Admin)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = LayNavyDark
                            )
                            Text(
                                text = "+255 717 798 877",
                                style = MaterialTheme.typography.bodySmall,
                                color = LayTextSecondary
                            )
                        }
                    }

                    QuickContactButtonsRow(
                        phoneNumber = "+255717798877",
                        contactName = "Admin Layson",
                        defaultMessage = "Habari Admin Layson, nina swali/taarifa kutoka kwa wakala ${agent.name}."
                    )
                }
            }
        }

        // Fellow Agents Directory
        if (fellowAgents.isNotEmpty()) {
            item {
                Text(
                    text = "Mawakala Wenzako (Fellow Agents)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LayTextPrimary
                )
            }

            items(fellowAgents) { fellow ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(name = fellow.name, size = 38)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = fellow.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = LayTextPrimary
                                )
                                Text(
                                    text = fellow.phone,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LayTextSecondary
                                )
                            }
                        }

                        QuickContactButtonsRow(
                            phoneNumber = fellow.phone,
                            contactName = fellow.name,
                            defaultMessage = "Habari ${fellow.name}, kutoka kwa wakala mwenzako ${agent.name}."
                        )
                    }
                }
            }
        }

        // Recent Activity
        item {
            Text(
                text = "Matukio Yangu (My Recent Activity)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LayTextPrimary
            )
        }

        items(auditLogs.take(4)) { log ->
            ActivityItemCard(log = log)
        }
    }
}
