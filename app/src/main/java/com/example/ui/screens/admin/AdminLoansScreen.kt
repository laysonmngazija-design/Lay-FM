package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.InterestMethod
import com.example.data.model.Loan
import com.example.data.model.LoanStatus
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.Formatters
import com.example.ui.components.LoanStatusBadge
import com.example.ui.components.QuickContactButtonsRow
import com.example.ui.components.SmsHelper
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoansScreen(
    repository: MicrofinanceRepository,
    adminName: String = "Layson Mngazija",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val loans by repository.loans.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedLoan by remember { mutableStateOf<Loan?>(null) }

    // Dialog states
    var showApproveConfirm by remember { mutableStateOf(false) }
    var showDeclineDialog by remember { mutableStateOf(false) }
    var declineReason by remember { mutableStateOf("") }
    var showDisburseDialog by remember { mutableStateOf(false) }
    var disbMethod by remember { mutableStateOf("M-Pesa") }
    var disbRef by remember { mutableStateOf("") }
    var isOperating by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Pending", "Approved", "Disbursed", "Active", "Overdue")

    val filteredLoans = loans.filter { loan ->
        val matchesFilter = when (selectedFilter) {
            "Pending" -> loan.status == LoanStatus.PENDING
            "Approved" -> loan.status == LoanStatus.APPROVED
            "Disbursed" -> loan.status == LoanStatus.DISBURSED
            "Active" -> loan.status == LoanStatus.ACTIVE
            "Overdue" -> loan.status == LoanStatus.OVERDUE
            else -> true
        }
        val matchesSearch = loan.customerName.contains(searchQuery, ignoreCase = true) ||
                loan.loanNumber.contains(searchQuery, ignoreCase = true) ||
                loan.agentName.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mikopo (Loans)",
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
            // Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LayNavyPrimary,
                            selectedLabelColor = Color.White
                        )
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
                    .testTag("loan_search_input"),
                placeholder = { Text("Tafuta mkopo kwa mteja, namba, au wakala...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LayTextSecondary)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loans List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredLoans) { loan ->
                    AdminLoanCard(loan = loan, onClick = { selectedLoan = loan })
                }
            }
        }
    }

    // Loan Detail Dialog
    selectedLoan?.let { loan ->
        AlertDialog(
            onDismissRequest = { if (!isOperating) selectedLoan = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = loan.customerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${loan.loanNumber} • Wakala: ${loan.agentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                    }
                    LoanStatusBadge(status = loan.status)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider()
                    // Amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mkopo wa Asili (Principal):")
                        Text(Formatters.formatTsh(loan.principalAmount), fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Riba (Interest):")
                        Text(
                            if (loan.interestMethod == InterestMethod.PERCENTAGE)
                                "${Formatters.formatTsh(loan.interestAmount)} (${loan.interestRate}%)"
                            else Formatters.formatTsh(loan.interestAmount)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jumla ya Mkataba (Total):")
                        Text(Formatters.formatTsh(loan.totalContractualAmount), fontWeight = FontWeight.ExtraBold, color = LayNavyPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rejesho la Kila Siku (Daily):")
                        Text(Formatters.formatTsh(loan.installmentAmount), fontWeight = FontWeight.SemiBold, color = LayGreenDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Muda wa Mkopo (Duration):")
                        Text("${loan.durationDays} siku (deadline 6:00 PM)")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simu ya Mteja: ${loan.customerPhone}", fontWeight = FontWeight.SemiBold)
                        QuickContactButtonsRow(
                            phoneNumber = loan.customerPhone,
                            contactName = loan.customerName,
                            defaultMessage = "Habari ${loan.customerName}, kuhusu mkopo wako ${loan.loanNumber} kutoka Lay Microfinance."
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Payment Recipient details (Kept explicitly separate)
                    Text(
                        text = "Mpokeaji wa Fedha (Payment Recipient):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = LayNavyLight
                    )
                    Text("Jina: ${loan.recipientName.ifBlank { loan.customerName }}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simu: ${loan.recipientPhone.ifBlank { loan.customerPhone }} (${loan.recipientNetwork})")
                        QuickContactButtonsRow(
                            phoneNumber = loan.recipientPhone.ifBlank { loan.customerPhone },
                            contactName = loan.recipientName.ifBlank { loan.customerName },
                            defaultMessage = "Habari ${loan.recipientName.ifBlank { loan.customerName }}, kuhusu malipo kutoka Lay Microfinance."
                        )
                    }

                    if (loan.status == LoanStatus.ACTIVE || loan.status == LoanStatus.OVERDUE || loan.status == LoanStatus.COMPLETED) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jumla Iliyolipwa (Paid):")
                            Text(Formatters.formatTsh(loan.totalPaid), color = LayGreenDark, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Salio Lililobaki (Remaining):")
                            Text(
                                Formatters.formatTsh(loan.remainingBalance),
                                color = if (loan.status == LoanStatus.OVERDUE) StatusOverdue else LayTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (loan.status == LoanStatus.DECLINED && loan.declineReason.isNotBlank()) {
                        Text(
                            text = "Sababu ya Kukataliwa: ${loan.declineReason}",
                            color = StatusOverdue,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (loan.status) {
                        LoanStatus.PENDING -> {
                            Button(
                                onClick = { showApproveConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                                modifier = Modifier.testTag("admin_approve_button")
                            ) {
                                Text("Idhinisha (Approve)")
                            }
                            OutlinedButton(
                                onClick = { showDeclineDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue),
                                modifier = Modifier.testTag("admin_decline_button")
                            ) {
                                Text("Kataa (Decline)")
                            }
                        }
                        LoanStatus.APPROVED -> {
                            Button(
                                onClick = {
                                    disbRef = "TX-" + System.currentTimeMillis().toString().takeLast(6)
                                    showDisburseDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LayNavyLight),
                                modifier = Modifier.testTag("admin_disburse_button")
                            ) {
                                Text("Tuma Fedha (Disburse)")
                            }
                        }
                        else -> {
                            TextButton(onClick = { selectedLoan = null }) {
                                Text("Funga")
                            }
                        }
                    }
                }
            },
            dismissButton = {
                if (loan.status == LoanStatus.PENDING || loan.status == LoanStatus.APPROVED) {
                    TextButton(onClick = { selectedLoan = null }) {
                        Text("Funga")
                    }
                }
            }
        )
    }

    // Approve Confirmation Dialog
    if (showApproveConfirm && selectedLoan != null) {
        AlertDialog(
            onDismissRequest = { showApproveConfirm = false },
            title = { Text("Thibitisha Kuidhinisha Mkopo") },
            text = {
                Text("Je, una uhakika unataka kuidhinisha mkopo wa TSh ${Formatters.formatTsh(selectedLoan!!.principalAmount)} kwa ${selectedLoan!!.customerName}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperating = true
                        coroutineScope.launch {
                            repository.approveLoan(selectedLoan!!.id, adminName)
                            isOperating = false
                            showApproveConfirm = false
                            selectedLoan = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
                ) {
                    Text("Ndio, Idhinisha")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirm = false }) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Decline Dialog
    if (showDeclineDialog && selectedLoan != null) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Kataa Maombi ya Mkopo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Weka sababu ya kukataa mkopo huu:")
                    OutlinedTextField(
                        value = declineReason,
                        onValueChange = { declineReason = it },
                        modifier = Modifier.fillMaxWidth().testTag("decline_reason_input"),
                        placeholder = { Text("Mfano: Historia ya madeni mabaya") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperating = true
                        coroutineScope.launch {
                            repository.declineLoan(selectedLoan!!.id, declineReason.ifBlank { "Haikukidhi vigezo" }, adminName)
                            isOperating = false
                            showDeclineDialog = false
                            selectedLoan = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
                ) {
                    Text("Thibitisha Kukataa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Disburse Dialog
    if (showDisburseDialog && selectedLoan != null) {
        AlertDialog(
            onDismissRequest = { showDisburseDialog = false },
            title = { Text("Thibitisha Utoaji wa Fedha (Disbursement)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Mpokeaji: ${selectedLoan!!.recipientName} (${selectedLoan!!.recipientPhone})")
                    Text("Kiasi Kinachotumwa: ${Formatters.formatTsh(selectedLoan!!.principalAmount)}", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = disbMethod,
                        onValueChange = { disbMethod = it },
                        label = { Text("Njia ya Malipo (M-Pesa, Airtel, Tigo, Cash)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = disbRef,
                        onValueChange = { disbRef = it },
                        label = { Text("Kumbukumbu ya Muamala (Tx Reference)") },
                        modifier = Modifier.fillMaxWidth().testTag("disbursement_ref_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isOperating = true
                        coroutineScope.launch {
                            repository.confirmDisbursement(
                                selectedLoan!!.id,
                                selectedLoan!!.principalAmount,
                                disbMethod,
                                disbRef.ifBlank { "CASH-" + System.currentTimeMillis() },
                                adminName
                            )
                            isOperating = false
                            showDisburseDialog = false
                            selectedLoan = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark)
                ) {
                    Text("Thibitisha na Washa Mkopo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisburseDialog = false }) {
                    Text("Ghairi")
                }
            }
        )
    }
}

@Composable
fun AdminLoanCard(
    loan: Loan,
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
            UserAvatar(name = loan.customerName, size = 44)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loan.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LayTextPrimary
                    )
                    LoanStatusBadge(status = loan.status)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = Formatters.formatTsh(loan.principalAmount),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = LayNavyPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agent: ${loan.agentName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary
                        )
                        Text(
                            text = Formatters.formatDate(loan.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = LayTextTertiary
                        )
                    }

                    QuickContactButtonsRow(
                        phoneNumber = loan.customerPhone,
                        contactName = loan.customerName,
                        defaultMessage = "Habari ${loan.customerName}, kutoka Lay Microfinance."
                    )
                }
            }
        }
    }
}
