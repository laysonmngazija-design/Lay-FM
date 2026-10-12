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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Loan
import com.example.data.model.LoanStatus
import com.example.data.model.Payment
import com.example.data.model.PaymentType
import com.example.data.model.UserProfile
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.Formatters
import com.example.ui.components.QuickContactButtonsRow
import com.example.ui.components.SmsHelper
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCollectionsScreen(
    agent: UserProfile,
    repository: MicrofinanceRepository,
    onNavigateToCustomers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val payments by repository.payments.collectAsState()
    val loans by repository.loans.collectAsState()

    val myPayments = payments.filter { repository.isAgentPayment(it, agent) }
    val myActiveLoans = loans.filter { repository.isAgentLoan(it, agent) && (it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE) }

    val now = System.currentTimeMillis()
    val dayMillis = 86400000L

    val hasPaymentsToday = myPayments.any { it.timestamp >= (now - dayMillis) }
    val tabs = listOf("Leo (Today)", "Wiki Hii", "Mwezi Huu", "Yote (All)")
    var selectedTab by remember { mutableStateOf(if (hasPaymentsToday) "Leo (Today)" else "Yote (All)") }

    val filteredPayments = myPayments.filter { pay ->
        when (selectedTab) {
            "Leo (Today)" -> pay.timestamp >= (now - dayMillis)
            "Wiki Hii" -> pay.timestamp >= (now - dayMillis * 7)
            "Mwezi Huu" -> pay.timestamp >= (now - dayMillis * 30)
            else -> true
        }
    }

    val totalDueToday = myActiveLoans.sumOf { it.installmentAmount }
    val todayCollections = myPayments.filter { it.timestamp >= (now - dayMillis) }.sumOf { it.amount }

    var selectedPaymentForReceipt by remember { mutableStateOf<Payment?>(null) }
    var showQuickPaymentLoanPicker by remember { mutableStateOf(false) }
    var loanToRepay by remember { mutableStateOf<Loan?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Makusanyo Yangu (${myPayments.size})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LayNavyPrimary
                        )
                    )
                },
                actions = {
                    Button(
                        onClick = { showQuickPaymentLoanPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lipa", fontSize = 12.sp)
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
            // Time Period Tabs Row
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
                                color = if (selectedTab == tab) LayNavyPrimary else LayTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Card
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
                    Column {
                        Text(text = "Collected Today", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(todayCollections),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = LayTextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total Due Today", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatTsh(totalDueToday),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = LayNavyPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "+ Record Collection" button
            Button(
                onClick = { showQuickPaymentLoanPicker = true },
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
                    text = "+ Rekodi Malipo (Record Collection)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Malipo (${selectedTab}): ${filteredPayments.size} shughuli",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LayTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = LayTextTertiary,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Hakuna Rekodi za Makusanyo",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LayTextPrimary
                        )
                        Text(
                            text = "Hakuna malipo yaliyorekodiwa kwa kipindi cha '$selectedTab'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showQuickPaymentLoanPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Rekodi Malipo Sasa")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredPayments) { pay ->
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
                                        text = "${Formatters.formatTimeOnly(pay.timestamp)} • ${pay.receiptNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LayTextTertiary
                                    )
                                }

                                IconButton(
                                    onClick = { CommunicationHelper.makePhoneCall(context, pay.customerPhone) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Piga Simu",
                                        tint = LayGreenDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Quick Payment Customer & Loan Picker (Wateja wa wakala huyu pekee)
    if (showQuickPaymentLoanPicker) {
        val activeLoansToChoose = myActiveLoans
        AlertDialog(
            onDismissRequest = { showQuickPaymentLoanPicker = false },
            title = {
                Text(
                    text = "Chagua Mteja wa Kurekodi Malipo",
                    fontWeight = FontWeight.Bold,
                    color = LayNavyPrimary
                )
            },
            text = {
                if (activeLoansToChoose.isEmpty()) {
                    Text("Hakuna mikopo inayoendelea kwa sasa inayohitaji marejesho.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeLoansToChoose) { loan ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        loanToRepay = loan
                                        showQuickPaymentLoanPicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(loan.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${loan.loanNumber} • Salio: ${Formatters.formatTsh(loan.remainingBalance)}", fontSize = 12.sp, color = LayNavyDark)
                                        Text("Rejesho la siku: ${Formatters.formatTsh(loan.installmentAmount)}", fontSize = 11.sp, color = LayGreenDark)
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = LayTextTertiary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickPaymentLoanPicker = false }) {
                    Text("Funga")
                }
            }
        )
    }

    // Quick Payment Record Form for selected loan
    loanToRepay?.let { loan ->
        var paymentType by remember { mutableStateOf(PaymentType.PARTIAL_PAYMENT) }
        var paymentAmountStr by remember { mutableStateOf("${loan.installmentAmount}") }
        var paymentMethod by remember { mutableStateOf("CASH") }
        var notes by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }
        var payError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isProcessing) loanToRepay = null },
            title = {
                Column {
                    Text("Rekodi Malipo ya Mteja", fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                    Text("Mteja: ${loan.customerName}", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (payError != null) {
                        Text(payError ?: "", color = StatusOverdue, fontSize = 12.sp)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Salio Lililobaki:", fontSize = 12.sp)
                                Text(Formatters.formatTsh(loan.remainingBalance), fontWeight = FontWeight.Bold, color = LayNavyDark)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rejesho la Siku:", fontSize = 12.sp)
                                Text(Formatters.formatTsh(loan.installmentAmount), fontWeight = FontWeight.SemiBold, color = LayGreenDark)
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paymentType == PaymentType.FULL_PAYMENT,
                            onClick = {
                                paymentType = PaymentType.FULL_PAYMENT
                                paymentAmountStr = "${loan.remainingBalance}"
                            },
                            label = { Text("Lipa Full") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = paymentType == PaymentType.PARTIAL_PAYMENT,
                            onClick = {
                                paymentType = PaymentType.PARTIAL_PAYMENT
                                paymentAmountStr = "${loan.installmentAmount}"
                            },
                            label = { Text("Lipa Kidogo") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = paymentAmountStr,
                        onValueChange = { paymentAmountStr = it; payError = null },
                        label = { Text("Kiasi Kilichopokelewa (TZS)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        label = { Text("Njia ya Malipo (CASH, M-Pesa...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountStr.toLongOrNull() ?: 0L
                        if (amount <= 0L) {
                            payError = "Ingiza kiasi sahihi cha fedha."
                            return@Button
                        }
                        if (amount > loan.remainingBalance) {
                            payError = "Kiasi kimezidi salio lililobaki."
                            return@Button
                        }
                        isProcessing = true
                        coroutineScope.launch {
                            val res = repository.recordRepayment(
                                loan.id,
                                amount,
                                paymentType,
                                paymentMethod,
                                notes,
                                agent.id,
                                agent.name
                            )
                            isProcessing = false
                            res.onSuccess { (savedPay, _) ->
                                loanToRepay = null
                                selectedPaymentForReceipt = savedPay
                            }.onFailure { err ->
                                payError = err.localizedMessage ?: "Imeshindwa kurekodi malipo."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                    enabled = !isProcessing
                ) {
                    Text("Hifadhi Malipo")
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToRepay = null }, enabled = !isProcessing) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Receipt details dialog with Send Balance SMS and Call Button
    selectedPaymentForReceipt?.let { pay ->
        val balanceMsg = "Habari ${pay.customerName}, tumepokea malipo yako ya ${Formatters.formatTsh(pay.amount)} (${pay.receiptNumber}). Salio la sasa ni ${Formatters.formatTsh(pay.balanceAfter)}. Asante kwa kufanya marejesho. — Lay Microfinance"
        AlertDialog(
            onDismissRequest = { selectedPaymentForReceipt = null },
            title = { Text("Stakabadhi ya Malipo (${pay.receiptNumber})", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mteja: ${pay.customerName}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simu: ${pay.customerPhone}", fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = { CommunicationHelper.makePhoneCall(context, pay.customerPhone) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Piga Simu", tint = LayGreenDark)
                        }
                    }
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
