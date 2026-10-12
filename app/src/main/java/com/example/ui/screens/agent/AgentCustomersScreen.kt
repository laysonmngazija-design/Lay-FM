package com.example.ui.screens.agent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.data.model.*
import com.example.data.repository.MicrofinanceRepository
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.CustomerHistoryDialog
import com.example.ui.components.Formatters
import com.example.ui.components.LoanStatusBadge
import com.example.ui.components.QuickContactButtonsRow
import com.example.ui.components.SmsHelper
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCustomersScreen(
    agent: UserProfile,
    repository: MicrofinanceRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customers by repository.customers.collectAsState()
    val loans by repository.loans.collectAsState()
    val payments by repository.payments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var showPaymentSheetForLoan by remember { mutableStateOf<Loan?>(null) }

    // Filter to this agent only (agent aone wateja wake tu)
    val myCustomers = customers.filter { repository.isAgentCustomer(it, agent) }
    val filteredCustomers = myCustomers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
        it.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LayBackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wateja Wangu (${myCustomers.size})",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = LayNavyPrimary
                        )
                    )
                },
                actions = {
                    Button(
                        onClick = { showAddCustomerDialog = true },
                        modifier = Modifier.testTag("add_customer_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ongeza Mteja", fontSize = 12.sp)
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                placeholder = { Text("Tafuta mteja kwa jina au namba ya simu...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = LayTextSecondary)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Customer List or Empty State
            if (filteredCustomers.isEmpty()) {
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
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = LayTextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Hakuna matokeo ya utafutaji" else "Hakuna Wateja Wako Bado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LayTextPrimary
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Hakuna mteja anayelingana na '$searchQuery'."
                            else "Bofya kitufe cha chini kusajili mteja wako mpya kuanza kumhudumia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LayTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sajili Mteja Mpya")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredCustomers) { cust ->
                        val custLoans = loans.filter { it.customerId == cust.id }
                        val activeLoan = custLoans.find { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE || it.status == LoanStatus.PENDING || it.status == LoanStatus.APPROVED }

                        CustomerCardItem(
                            customer = cust,
                            activeLoan = activeLoan,
                            onClick = { selectedCustomer = cust },
                            onLipaClick = {
                                if (activeLoan != null && (activeLoan.status == LoanStatus.ACTIVE || activeLoan.status == LoanStatus.OVERDUE)) {
                                    showPaymentSheetForLoan = activeLoan
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Customer & Loan Dialog
    if (showAddCustomerDialog) {
        var fullName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("+255 ") }
        var address by remember { mutableStateOf("") }
        var guarantorName by remember { mutableStateOf("") }
        var guarantorPhone by remember { mutableStateOf("+255 ") }
        var loanAmountStr by remember { mutableStateOf("100000") }
        var loanPurpose by remember { mutableStateOf("Biashara") }

        // Loan terms
        var interestMethod by remember { mutableStateOf(InterestMethod.PERCENTAGE) }
        var interestRateStr by remember { mutableStateOf("20") }
        var durationDaysStr by remember { mutableStateOf("24") }
        var installmentAmountStr by remember { mutableStateOf("5000") }

        var photoUri by remember { mutableStateOf<Uri?>(null) }
        var isSaving by remember { mutableStateOf(false) }
        var formError by remember { mutableStateOf<String?>(null) }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            photoUri = uri
        }

        AlertDialog(
            onDismissRequest = { if (!isSaving) showAddCustomerDialog = false },
            title = { Text("Sajili Mteja Mpya & Ombi la Mkopo", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (formError != null) {
                        Text(text = formError ?: "", color = StatusOverdue, fontSize = 12.sp)
                    }

                    Text("1. Taarifa za Mteja (Customer Details)", fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Jina Kamili la Mteja *") },
                        modifier = Modifier.fillMaxWidth().testTag("cust_form_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Namba ya Simu (+255) *") },
                        modifier = Modifier.fillMaxWidth().testTag("cust_form_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Makazi / Mahali anapoishi *") },
                        modifier = Modifier.fillMaxWidth().testTag("cust_form_address"),
                        singleLine = true
                    )

                    // Photo selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (photoUri != null) "Picha imechaguliwa ✓" else "Picha ya Mteja (Photo)",
                            fontSize = 12.sp,
                            color = if (photoUri != null) LayGreenDark else LayTextSecondary
                        )
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chagua Picha", fontSize = 12.sp)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("2. Mtu wa Karibu / Mdhamini (Guarantor)", fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                    OutlinedTextField(
                        value = guarantorName,
                        onValueChange = { guarantorName = it },
                        label = { Text("Jina la Mdhamini *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = guarantorPhone,
                        onValueChange = { guarantorPhone = it },
                        label = { Text("Simu ya Mdhamini *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("3. Vigezo vya Mkopo (Loan Terms)", fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                    OutlinedTextField(
                        value = loanAmountStr,
                        onValueChange = { loanAmountStr = it },
                        label = { Text("Kiasi cha Mkopo (TZS) *") },
                        modifier = Modifier.fillMaxWidth().testTag("cust_form_amount"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = loanPurpose,
                        onValueChange = { loanPurpose = it },
                        label = { Text("Sababu ya Mkopo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Interest Method Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = interestMethod == InterestMethod.PERCENTAGE,
                            onClick = { interestMethod = InterestMethod.PERCENTAGE },
                            label = { Text("Asilimia (%)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = interestMethod == InterestMethod.FIXED_AMOUNT,
                            onClick = { interestMethod = InterestMethod.FIXED_AMOUNT },
                            label = { Text("Kiasi Maalum (TZS)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = interestRateStr,
                        onValueChange = { interestRateStr = it },
                        label = { Text(if (interestMethod == InterestMethod.PERCENTAGE) "Riba (%) Mfano: 20" else "Kiasi cha Riba (TZS)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationDaysStr,
                            onValueChange = { durationDaysStr = it },
                            label = { Text("Muda (Siku) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = installmentAmountStr,
                            onValueChange = { installmentAmountStr = it },
                            label = { Text("Rejesho/Siku (TZS) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // Hesabu ya Moja kwa Moja (Live Auto-Calculation: Mkopo + Riba ÷ Siku = Rejesho/Siku)
                    val principalCalc = loanAmountStr.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    val rateOrFixedCalc = interestRateStr.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                    val daysCalc = durationDaysStr.filter { it.isDigit() }.toIntOrNull() ?: 24

                    val calculatedInterest = if (interestMethod == InterestMethod.PERCENTAGE) {
                        (principalCalc * rateOrFixedCalc / 100.0).toLong()
                    } else {
                        rateOrFixedCalc.toLong()
                    }
                    val calculatedTotal = principalCalc + calculatedInterest
                    val calculatedInstallment = if (daysCalc > 0) (calculatedTotal + daysCalc - 1) / daysCalc else 0L

                    LaunchedEffect(principalCalc, rateOrFixedCalc, interestMethod, daysCalc) {
                        if (calculatedInstallment > 0) {
                            installmentAmountStr = calculatedInstallment.toString()
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = LayGreenContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = LayGreenDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hesabu ya Moja kwa Moja (Auto-Calculation):", fontWeight = FontWeight.Bold, color = LayGreenOnContainer, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1. Kiasi cha Mkopo:", fontSize = 12.sp)
                                Text(Formatters.formatTsh(principalCalc), fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(if (interestMethod == InterestMethod.PERCENTAGE) "2. Riba ($interestRateStr%):" else "2. Kiasi cha Riba:", fontSize = 12.sp)
                                Text("+ ${Formatters.formatTsh(calculatedInterest)}", fontWeight = FontWeight.SemiBold, color = LayGreenDark)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("3. Jumla ya Mkataba (1 + 2):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(Formatters.formatTsh(calculatedTotal), fontWeight = FontWeight.ExtraBold, color = LayNavyDark)
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = LayGreenDark.copy(alpha = 0.2f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("4. Rejesho kwa Siku ($daysCalc siku):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                                    Text("Jumla (${Formatters.formatTsh(calculatedTotal)}) ÷ Siku $daysCalc", fontSize = 10.sp, color = LayTextSecondary)
                                }
                                Text(
                                    "${Formatters.formatTsh(calculatedInstallment)} / siku",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LayGreenDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val principal = loanAmountStr.toLongOrNull() ?: 0L
                        val rateOrFixed = interestRateStr.toDoubleOrNull() ?: 20.0
                        val days = durationDaysStr.toIntOrNull() ?: 24
                        val installment = installmentAmountStr.toLongOrNull() ?: 5000L

                        if (fullName.isBlank() || phone.isBlank() || principal <= 0L) {
                            formError = "Tafadhali jaza taarifa zote muhimu zenye nyota (*)."
                            return@Button
                        }

                        isSaving = true
                        val newCust = Customer(
                            agentId = agent.id,
                            agentName = agent.name,
                            fullName = fullName.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            photoUrl = photoUri?.toString() ?: "",
                            guarantorName = guarantorName.trim(),
                            guarantorPhone = guarantorPhone.trim(),
                            requestedAmount = principal,
                            loanPurpose = loanPurpose.trim(),
                            recipientName = fullName.trim(),
                            recipientPhone = phone.trim(),
                            recipientNetwork = "Simu ya Mteja",
                            status = "Active"
                        )

                        coroutineScope.launch {
                            repository.registerCustomerAndLoan(
                                newCust,
                                principal,
                                interestMethod,
                                rateOrFixed,
                                days,
                                installment,
                                RepaymentFrequency.DAILY
                            )
                            isSaving = false
                            showAddCustomerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                    enabled = !isSaving
                ) {
                    Text("Tuma Maombi ya Mkopo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomerDialog = false }, enabled = !isSaving) {
                    Text("Ghairi")
                }
            }
        )
    }

    // Full Customer Profile & History Dialog
    selectedCustomer?.let { cust ->
        CustomerHistoryDialog(
            customer = cust,
            loans = loans,
            payments = payments,
            onDismiss = { selectedCustomer = null },
            onRecordPaymentClick = { loan ->
                showPaymentSheetForLoan = loan
                selectedCustomer = null
            }
        )
    }

    // Payment Dialog (Lipa Full vs Lipa Kidogo Kidogo)
    showPaymentSheetForLoan?.let { loan ->
        var paymentType by remember { mutableStateOf(PaymentType.PARTIAL_PAYMENT) }
        var paymentAmountStr by remember { mutableStateOf("${loan.installmentAmount}") }
        var paymentMethod by remember { mutableStateOf("CASH") }
        var notes by remember { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }
        var paymentSuccessResult by remember { mutableStateOf<Pair<Payment, Loan>?>(null) }
        var payError by remember { mutableStateOf<String?>(null) }

        if (paymentSuccessResult == null) {
            AlertDialog(
                onDismissRequest = { if (!isProcessing) showPaymentSheetForLoan = null },
                title = {
                    Column {
                        Text("Rekodi Malipo (Lipa)", fontWeight = FontWeight.Bold, color = LayNavyPrimary)
                        Text("Mteja: ${loan.customerName}", style = MaterialTheme.typography.bodySmall, color = LayTextSecondary)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (payError != null) {
                            Text(text = payError ?: "", color = StatusOverdue, fontSize = 12.sp)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Salio Lililobaki (Current Balance):", fontSize = 12.sp)
                                    Text(Formatters.formatTsh(loan.remainingBalance), fontWeight = FontWeight.ExtraBold, color = LayNavyDark)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rejesho Lililokubaliwa (Daily):", fontSize = 12.sp)
                                    Text(Formatters.formatTsh(loan.installmentAmount), fontWeight = FontWeight.SemiBold, color = LayGreenDark)
                                }
                            }
                        }

                        // Payment Type Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = paymentType == PaymentType.FULL_PAYMENT,
                                onClick = {
                                    paymentType = PaymentType.FULL_PAYMENT
                                    paymentAmountStr = "${loan.remainingBalance}"
                                },
                                label = { Text("Lipa Full") },
                                modifier = Modifier.weight(1f).testTag("lipa_full_chip")
                            )

                            FilterChip(
                                selected = paymentType == PaymentType.PARTIAL_PAYMENT,
                                onClick = {
                                    paymentType = PaymentType.PARTIAL_PAYMENT
                                    paymentAmountStr = "${loan.installmentAmount}"
                                },
                                label = { Text("Lipa Kidogo Kidogo") },
                                modifier = Modifier.weight(1f).testTag("lipa_partial_chip")
                            )
                        }

                        OutlinedTextField(
                            value = paymentAmountStr,
                            onValueChange = {
                                paymentAmountStr = it
                                payError = null
                            },
                            label = { Text("Kiasi Kilichopokelewa (TZS)") },
                            modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            label = { Text("Njia ya Malipo (CASH, M-Pesa, n.k.)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Maelezo ya Ziada (Hiari)") },
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
                                payError = "Tafadhali ingiza kiasi sahihi cha fedha."
                                return@Button
                            }
                            if (amount > loan.remainingBalance) {
                                payError = "Kiasi kimezidi salio lililobaki (${Formatters.formatTsh(loan.remainingBalance)})."
                                return@Button
                            }

                            isProcessing = true
                            coroutineScope.launch {
                                val result = repository.recordRepayment(
                                    loan.id,
                                    amount,
                                    paymentType,
                                    paymentMethod,
                                    notes,
                                    agent.id,
                                    agent.name
                                )
                                isProcessing = false
                                result.onSuccess { pair ->
                                    paymentSuccessResult = pair
                                }.onFailure { err ->
                                    payError = err.localizedMessage ?: "Imeshindwa kurekodi malipo."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        enabled = !isProcessing,
                        modifier = Modifier.testTag("save_payment_button")
                    ) {
                        Text("Hifadhi Malipo (Save Payment)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentSheetForLoan = null }, enabled = !isProcessing) {
                        Text("Ghairi")
                    }
                }
            )
        } else {
            // Payment Confirmation Success dialog with "Send Balance SMS" action!
            val (savedPayment, updatedLoan) = paymentSuccessResult!!
            val balanceSmsText = "Habari ${loan.customerName}, tumepokea malipo yako ya ${Formatters.formatTsh(savedPayment.amount)}. Salio la mkopo wako ni ${Formatters.formatTsh(updatedLoan.remainingBalance)}. Asante kwa kufanya malipo. — Lay Microfinance"

            AlertDialog(
                onDismissRequest = { showPaymentSheetForLoan = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LayGreenDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Malipo Yamekamilika!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Stakabadhi: ${savedPayment.receiptNumber}", fontWeight = FontWeight.SemiBold)
                        Text("Kiasi Kilicholipwa: ${Formatters.formatTsh(savedPayment.amount)}", color = LayGreenDark, fontWeight = FontWeight.Bold)
                        Text("Salio Jipya: ${Formatters.formatTsh(updatedLoan.remainingBalance)}", fontWeight = FontWeight.ExtraBold, color = LayNavyDark)

                        if (updatedLoan.remainingBalance <= 0L) {
                            Text("Mkopo umemalizika kikamilifu! (Completed)", color = LayGreenDark, fontWeight = FontWeight.Bold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Ujumbe wa SMS (Balance SMS):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LaySurfaceSubtle)
                        ) {
                            Text(
                                text = balanceSmsText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                color = LayTextPrimary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            SmsHelper.launchSmsComposer(context, loan.customerPhone, balanceSmsText)
                            showPaymentSheetForLoan = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LayGreenDark),
                        modifier = Modifier.testTag("send_balance_sms_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tuma SMS ya Salio")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPaymentSheetForLoan = null }) {
                        Text("Funga")
                    }
                }
            )
        }
    }
}

@Composable
fun CustomerCardItem(
    customer: Customer,
    activeLoan: Loan?,
    onClick: () -> Unit,
    onLipaClick: () -> Unit
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = customer.fullName, size = 46, backgroundColor = LayNavyPrimary)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LayTextPrimary
                    )
                    if (activeLoan != null) {
                        LoanStatusBadge(status = activeLoan.status)
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = LayTextSecondary
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (activeLoan != null) {
                    Text(
                        text = "Loan: ${Formatters.formatTsh(activeLoan.principalAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LayNavyLight
                    )
                } else {
                    Text(
                        text = "Hakuna mkopo unaoendelea",
                        style = MaterialTheme.typography.bodySmall,
                        color = LayTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeLoan != null) {
                        Text(
                            text = "Salio: ${Formatters.formatTsh(activeLoan.remainingBalance)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LayNavyDark
                        )
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    QuickContactButtonsRow(
                        phoneNumber = customer.phone,
                        contactName = customer.fullName,
                        defaultMessage = "Habari ${customer.fullName}, kutoka Lay Microfinance."
                    )
                }
            }

            if (activeLoan != null && (activeLoan.status == LoanStatus.ACTIVE || activeLoan.status == LoanStatus.OVERDUE)) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onLipaClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LayGreenContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Lipa",
                        tint = LayGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
