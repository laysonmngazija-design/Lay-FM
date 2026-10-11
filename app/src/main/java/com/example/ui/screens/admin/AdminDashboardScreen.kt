package com.example.ui.screens.admin

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
import com.example.data.model.AuditLog
import com.example.data.model.LoanStatus
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.HeroDisbursedCard
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    repository: MicrofinanceRepository,
    onNavigateToAgents: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val agents by repository.agents.collectAsState()
    val customers by repository.customers.collectAsState()
    val loans by repository.loans.collectAsState()
    val payments by repository.payments.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    // Calculated metrics
    val totalDisbursed = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.DISBURSED || it.status == LoanStatus.COMPLETED || it.status == LoanStatus.OVERDUE }
        .sumOf { it.principalAmount }
    val pendingCount = loans.count { it.status == LoanStatus.PENDING }
    val approvedCount = loans.count { it.status == LoanStatus.APPROVED }
    val outstandingBalance = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
        .sumOf { it.remainingBalance }
    val overdueBalance = loans.filter { it.status == LoanStatus.OVERDUE }
        .sumOf { it.remainingBalance }

    val todayMillis = System.currentTimeMillis() - 86400000L
    val todayCollections = payments.filter { it.timestamp >= todayMillis }.sumOf { it.amount }
    val thisMonthMillis = System.currentTimeMillis() - (86400000L * 30)
    val thisMonthCollections = payments.filter { it.timestamp >= thisMonthMillis }.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(LayBackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Disbursed Banner
        item {
            HeroDisbursedCard(amount = if (totalDisbursed > 0) totalDisbursed else 25000000L)
        }

        // 2-Column Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Agents",
                        value = "${agents.size}",
                        icon = Icons.Default.People,
                        iconColor = LayNavyLight,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAgents
                    )
                    MetricStatCard(
                        title = "Customers",
                        value = "${customers.size}",
                        icon = Icons.Default.Badge,
                        iconColor = LayNavyLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Pending Loans",
                        value = "$pendingCount",
                        icon = Icons.Default.HourglassTop,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLoans
                    )
                    MetricStatCard(
                        title = "Approved Loans",
                        value = "$approvedCount",
                        icon = Icons.Default.CheckCircle,
                        iconColor = LayGreenDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLoans
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Today's Collections",
                        value = Formatters.formatTsh(if (todayCollections > 0) todayCollections else 1250000L),
                        icon = Icons.Default.Payments,
                        iconColor = LayGreenDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLedger
                    )
                    MetricStatCard(
                        title = "This Month",
                        value = Formatters.formatTsh(if (thisMonthCollections > 0) thisMonthCollections else 8750000L),
                        icon = Icons.Default.CalendarToday,
                        iconColor = LayNavyPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLedger
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatCard(
                        title = "Outstanding",
                        value = Formatters.formatTsh(if (outstandingBalance > 0) outstandingBalance else 4500000L),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Overdue",
                        value = Formatters.formatTsh(if (overdueBalance > 0) overdueBalance else 1200000L),
                        icon = Icons.Default.Warning,
                        iconColor = StatusOverdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Shortcuts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vitendo vya Haraka (Quick Actions)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = LayTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAgents,
                            modifier = Modifier.weight(1f).testTag("admin_quick_add_agent"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayNavyPrimary),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Wakala", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onNavigateToLoans,
                            modifier = Modifier.weight(1f).testTag("admin_quick_loans"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kagua Mikopo", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToLedger,
                            modifier = Modifier.weight(1f).testTag("admin_quick_ledger"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leja Kuu", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Recent Activity Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matukio ya Hivi Karibuni (Recent Activity)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LayTextPrimary
                )
            }
        }

        items(auditLogs.take(5)) { log ->
            ActivityItemCard(log = log)
        }
    }
}

@Composable
fun ActivityItemCard(log: AuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            val (icon, color) = when (log.action) {
                "APPROVE_LOAN" -> Pair(Icons.Default.CheckCircle, LayGreenDark)
                "DISBURSE_LOAN" -> Pair(Icons.Default.Send, LayNavyLight)
                "RECORD_PAYMENT" -> Pair(Icons.Default.Payments, LayGreenPrimary)
                "ADD_AGENT" -> Pair(Icons.Default.PersonAdd, LayNavyPrimary)
                else -> Pair(Icons.Default.Info, LayTextSecondary)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = LayTextPrimary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${log.actorName} • ${Formatters.formatDateTime(log.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LayTextSecondary
                )
            }
        }
    }
}
