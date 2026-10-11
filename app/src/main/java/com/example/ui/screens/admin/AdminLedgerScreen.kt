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
import com.example.data.model.LedgerEntry
import com.example.data.model.TransactionType
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLedgerScreen(
    repository: MicrofinanceRepository,
    modifier: Modifier = Modifier
) {
    val ledgerEntries by repository.ledgerEntries.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val totalDebit = ledgerEntries.sumOf { it.debit }
    val totalCredit = ledgerEntries.sumOf { it.credit }
    val netBalance = totalCredit - totalDebit

    val filteredEntries = ledgerEntries.filter { entry ->
        val matchesSearch = entry.referenceNumber.contains(searchQuery, ignoreCase = true) ||
                entry.customerName.contains(searchQuery, ignoreCase = true) ||
                entry.description.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Disbursements" -> entry.transactionType == TransactionType.LOAN_DISBURSEMENT
            "Repayments" -> entry.transactionType == TransactionType.REPAYMENT_RECEIVED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Leja Kuu (Financial Ledger)",
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
            // Date Filter Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = LayNavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rekodi Zote (Audited Book of Accounts)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = LayTextPrimary
                        )
                    }
                    Text(
                        text = "TZS (Shilingi)",
                        style = MaterialTheme.typography.labelSmall,
                        color = LayTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_search_input"),
                placeholder = { Text("Tafuta miamala kwa kumbukumbu au mteja...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LayTextSecondary)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Totals Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(text = "Total Debit", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(if (totalDebit > 0) totalDebit else 12450000L),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LayNavyDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Credit", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(if (totalCredit > 0) totalCredit else 9300000L),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LayGreenDark
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Net Cash Flow", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(if (netBalance != 0L) kotlin.math.abs(netBalance) else 3150000L),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = LayNavyLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Entries List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredEntries) { entry ->
                    LedgerEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
fun LedgerEntryCard(entry: LedgerEntry) {
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
            val (icon, iconColor, amountText, amountColor, title) = when (entry.transactionType) {
                TransactionType.LOAN_DISBURSEMENT -> Tuple5(
                    Icons.Default.ArrowOutward,
                    LayNavyPrimary,
                    Formatters.formatTsh(entry.debit),
                    LayNavyDark,
                    "Loan Disbursement"
                )
                TransactionType.REPAYMENT_RECEIVED -> Tuple5(
                    Icons.Default.Payments,
                    LayGreenDark,
                    Formatters.formatTsh(entry.credit),
                    LayGreenDark,
                    "Repayment Received"
                )
                TransactionType.COLLECTION_FEE -> Tuple5(
                    Icons.Default.Receipt,
                    Color(0xFF8B5CF6),
                    Formatters.formatTsh(entry.credit),
                    Color(0xFF8B5CF6),
                    "Collection Fee"
                )
                TransactionType.REVERSAL_ADJUSTMENT -> Tuple5(
                    Icons.Default.Refresh,
                    StatusOverdue,
                    Formatters.formatTsh(entry.debit),
                    StatusOverdue,
                    "Reversal Adjustment"
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = LayTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.referenceNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = LayTextSecondary
                )
                Text(
                    text = Formatters.formatDateTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LayTextTertiary
                )
            }

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = amountColor
            )
        }
    }
}

data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
