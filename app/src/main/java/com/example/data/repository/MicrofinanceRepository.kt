package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MicrofinanceRepository(private val context: Context) {
    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    val payments: StateFlow<List<Payment>> = _payments.asStateFlow()

    private val _ledgerEntries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val ledgerEntries: StateFlow<List<LedgerEntry>> = _ledgerEntries.asStateFlow()

    private val _agents = MutableStateFlow<List<UserProfile>>(emptyList())
    val agents: StateFlow<List<UserProfile>> = _agents.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    init {
        // Initialize default baseline dataset
        initializeBaselineData()
        // Start Firestore real-time listeners
        setupFirestoreListeners()
    }

    private fun initializeBaselineData() {
        // Initial Agents
        val baselineAgents = listOf(
            UserProfile(
                id = "agent_john_001",
                name = "John Mwita",
                email = "john@gmail.com",
                phone = "+255 712 345 678",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = "1234",
                createdAt = System.currentTimeMillis() - 86400000L * 180
            ),
            UserProfile(
                id = "agent_sarah_002",
                name = "Sarah Ally",
                email = "sarah@gmail.com",
                phone = "+255 753 222 111",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = "1234",
                createdAt = System.currentTimeMillis() - 86400000L * 120
            ),
            UserProfile(
                id = "agent_musa_003",
                name = "Musa Kasei",
                email = "musa@gmail.com",
                phone = "+255 777 444 555",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = "1234",
                createdAt = System.currentTimeMillis() - 86400000L * 90
            ),
            UserProfile(
                id = "agent_daniel_004",
                name = "Daniel Ulimwengu",
                email = "daniel@gmail.com",
                phone = "+255 718 999 888",
                role = UserRole.AGENT,
                status = AccountStatus.INACTIVE,
                pin = "1234",
                createdAt = System.currentTimeMillis() - 86400000L * 60
            ),
            UserProfile(
                id = "agent_fatuma_005",
                name = "Fatuma Amiri",
                email = "fatuma@gmail.com",
                phone = "+255 768 333 222",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = "1234",
                createdAt = System.currentTimeMillis() - 86400000L * 45
            )
        )
        _agents.value = baselineAgents

        // Initial Customers
        val baselineCustomers = listOf(
            Customer(
                id = "cust_001",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                fullName = "Asha Mzee",
                phone = "+255713123456",
                address = "Kariakoo, Dar es Salaam",
                guarantorName = "Rashid Mzee",
                guarantorPhone = "+255714000111",
                requestedAmount = 300000L,
                loanPurpose = "Biashara ya nguo",
                recipientName = "Asha Mzee",
                recipientPhone = "+255713123456",
                recipientNetwork = "M-Pesa",
                status = "Active"
            ),
            Customer(
                id = "cust_002",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                fullName = "Juma Salim",
                phone = "+255722678901",
                address = "Ilala Boma, Dar es Salaam",
                guarantorName = "Said Salim",
                guarantorPhone = "+255722111222",
                requestedAmount = 500000L,
                loanPurpose = "Kununua bidhaa za duka",
                recipientName = "Juma Salim",
                recipientPhone = "+255722678901",
                recipientNetwork = "Airtel Money",
                status = "Active"
            ),
            Customer(
                id = "cust_003",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                fullName = "Neema Juma",
                phone = "+255795444333",
                address = "Kinondoni Manyanya",
                guarantorName = "Khadija Ally",
                guarantorPhone = "+255795111222",
                requestedAmount = 100000L,
                loanPurpose = "Mtaji wa genge",
                recipientName = "Neema Juma",
                recipientPhone = "+255795444333",
                recipientNetwork = "Tigo Pesa",
                status = "Pending"
            ),
            Customer(
                id = "cust_004",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                fullName = "Peter Msangi",
                phone = "+255776555666",
                address = "Tegeta Nyuki",
                guarantorName = "Elia Msangi",
                guarantorPhone = "+255776111222",
                requestedAmount = 400000L,
                loanPurpose = "Upanuzi wa gereji",
                recipientName = "Peter Msangi",
                recipientPhone = "+255776555666",
                recipientNetwork = "M-Pesa",
                status = "Active"
            ),
            Customer(
                id = "cust_005",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                fullName = "Zainabu Ally",
                phone = "+255764888999",
                address = "Temeke Mwembeyanga",
                guarantorName = "Bakari Ally",
                guarantorPhone = "+255764111222",
                requestedAmount = 250000L,
                loanPurpose = "Biashara ya chakula",
                recipientName = "Zainabu Ally",
                recipientPhone = "+255764888999",
                recipientNetwork = "Halopesa",
                status = "Declined"
            ),
            Customer(
                id = "cust_006",
                agentId = "agent_sarah_002",
                agentName = "Sarah Ally",
                fullName = "Mohamed Salum",
                phone = "+255755123999",
                address = "Magomeni Mikumi",
                guarantorName = "Hassan Salum",
                guarantorPhone = "+255755000111",
                requestedAmount = 500000L,
                loanPurpose = "Mbao na ujenzi",
                recipientName = "Mohamed Salum",
                recipientPhone = "+255755123999",
                recipientNetwork = "M-Pesa",
                status = "Approved"
            ),
            Customer(
                id = "cust_007",
                agentId = "agent_musa_003",
                agentName = "Musa Kasei",
                fullName = "Amina Hassan",
                phone = "+255788999111",
                address = "Sinza Kijiweni",
                guarantorName = "Fatma Hassan",
                guarantorPhone = "+255788000222",
                requestedAmount = 300000L,
                loanPurpose = "Saluni ya kike",
                recipientName = "Amina Hassan",
                recipientPhone = "+255788999111",
                recipientNetwork = "Tigo Pesa",
                status = "Disbursed"
            ),
            Customer(
                id = "cust_008",
                agentId = "agent_daniel_004",
                agentName = "Daniel Ulimwengu",
                fullName = "Juma Mseti",
                phone = "+255719333444",
                address = "Mbagala Rangi Tatu",
                guarantorName = "Mwanaidi Mseti",
                guarantorPhone = "+255719000555",
                requestedAmount = 200000L,
                loanPurpose = "Biashara ya viatu",
                recipientName = "Juma Mseti",
                recipientPhone = "+255719333444",
                recipientNetwork = "M-Pesa",
                status = "Overdue"
            )
        )
        _customers.value = baselineCustomers

        // Initial Loans
        val baselineLoans = listOf(
            Loan(
                id = "loan_001",
                loanNumber = "LN001",
                customerId = "cust_001",
                customerName = "Asha Mzee",
                customerPhone = "+255713123456",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                principalAmount = 300000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 60000L,
                totalContractualAmount = 360000L,
                installmentAmount = 15000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.ACTIVE,
                remainingBalance = 160000L,
                totalPaid = 200000L,
                disbursedAt = System.currentTimeMillis() - 86400000L * 10,
                disbursementTxRef = "MPESA-9938124",
                disbursementMethod = "M-Pesa",
                disbursedBy = "Admin Layson"
            ),
            Loan(
                id = "loan_002",
                loanNumber = "LN002",
                customerId = "cust_002",
                customerName = "Juma Salim",
                customerPhone = "+255722678901",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                principalAmount = 500000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 100000L,
                totalContractualAmount = 600000L,
                installmentAmount = 25000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.ACTIVE,
                remainingBalance = 320000L,
                totalPaid = 280000L,
                disbursedAt = System.currentTimeMillis() - 86400000L * 12,
                disbursementTxRef = "AIRTEL-772819",
                disbursementMethod = "Airtel Money",
                disbursedBy = "Admin Layson"
            ),
            Loan(
                id = "loan_003",
                loanNumber = "LN003",
                customerId = "cust_003",
                customerName = "Neema Juma",
                customerPhone = "+255795444333",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                principalAmount = 100000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 20000L,
                totalContractualAmount = 120000L,
                installmentAmount = 5000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.PENDING,
                remainingBalance = 120000L,
                totalPaid = 0L
            ),
            Loan(
                id = "loan_004",
                loanNumber = "LN004",
                customerId = "cust_004",
                customerName = "Peter Msangi",
                customerPhone = "+255776555666",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                principalAmount = 400000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 80000L,
                totalContractualAmount = 480000L,
                installmentAmount = 20000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.ACTIVE,
                remainingBalance = 340000L,
                totalPaid = 140000L,
                disbursedAt = System.currentTimeMillis() - 86400000L * 7,
                disbursementTxRef = "MPESA-884920",
                disbursementMethod = "M-Pesa",
                disbursedBy = "Admin Layson"
            ),
            Loan(
                id = "loan_005",
                loanNumber = "LN005",
                customerId = "cust_005",
                customerName = "Zainabu Ally",
                customerPhone = "+255764888999",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                principalAmount = 250000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 50000L,
                totalContractualAmount = 300000L,
                installmentAmount = 12500L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.DECLINED,
                declineReason = "Historia ya mikopo mibaya",
                remainingBalance = 0L,
                totalPaid = 0L
            ),
            Loan(
                id = "loan_006",
                loanNumber = "LN006",
                customerId = "cust_006",
                customerName = "Mohamed Salum",
                customerPhone = "+255755123999",
                agentId = "agent_sarah_002",
                agentName = "Sarah Ally",
                principalAmount = 500000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 100000L,
                totalContractualAmount = 600000L,
                installmentAmount = 25000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.APPROVED,
                remainingBalance = 600000L,
                totalPaid = 0L,
                approvedAt = System.currentTimeMillis() - 86400000L * 1,
                approvedBy = "Admin Layson"
            ),
            Loan(
                id = "loan_007",
                loanNumber = "LN007",
                customerId = "cust_007",
                customerName = "Amina Hassan",
                customerPhone = "+255788999111",
                agentId = "agent_musa_003",
                agentName = "Musa Kasei",
                principalAmount = 300000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 60000L,
                totalContractualAmount = 360000L,
                installmentAmount = 15000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.DISBURSED,
                remainingBalance = 360000L,
                totalPaid = 0L,
                disbursedAt = System.currentTimeMillis() - 3600000L * 4,
                disbursementTxRef = "TIGO-229103",
                disbursementMethod = "Tigo Pesa",
                disbursedBy = "Admin Layson"
            ),
            Loan(
                id = "loan_008",
                loanNumber = "LN008",
                customerId = "cust_008",
                customerName = "Juma Mseti",
                customerPhone = "+255719333444",
                agentId = "agent_daniel_004",
                agentName = "Daniel Ulimwengu",
                principalAmount = 200000L,
                interestMethod = InterestMethod.PERCENTAGE,
                interestRate = 20.0,
                interestAmount = 40000L,
                totalContractualAmount = 240000L,
                installmentAmount = 10000L,
                durationDays = 24,
                repaymentFrequency = RepaymentFrequency.DAILY,
                status = LoanStatus.OVERDUE,
                remainingBalance = 190000L,
                totalPaid = 50000L,
                disbursedAt = System.currentTimeMillis() - 86400000L * 25,
                disbursementTxRef = "MPESA-110294",
                disbursementMethod = "M-Pesa",
                disbursedBy = "Admin Layson"
            )
        )
        _loans.value = baselineLoans

        // Initial Payments
        val now = System.currentTimeMillis()
        val baselinePayments = listOf(
            Payment(
                id = "pay_001",
                receiptNumber = "RCP-1001",
                loanId = "loan_001",
                customerId = "cust_001",
                customerName = "Asha Mzee",
                customerPhone = "+255713123456",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                amount = 100000L,
                paymentType = PaymentType.PARTIAL_PAYMENT,
                balanceBefore = 260000L,
                balanceAfter = 160000L,
                timestamp = now - 3600000L * 5
            ),
            Payment(
                id = "pay_002",
                receiptNumber = "RCP-1002",
                loanId = "loan_002",
                customerId = "cust_002",
                customerName = "Juma Salim",
                customerPhone = "+255722678901",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                amount = 80000L,
                paymentType = PaymentType.PARTIAL_PAYMENT,
                balanceBefore = 400000L,
                balanceAfter = 320000L,
                timestamp = now - 3600000L * 4
            ),
            Payment(
                id = "pay_003",
                receiptNumber = "RCP-1003",
                loanId = "loan_004",
                customerId = "cust_004",
                customerName = "Peter Msangi",
                customerPhone = "+255776555666",
                agentId = "agent_john_001",
                agentName = "John Mwita",
                amount = 70000L,
                paymentType = PaymentType.PARTIAL_PAYMENT,
                balanceBefore = 410000L,
                balanceAfter = 340000L,
                timestamp = now - 3600000L * 2
            ),
            Payment(
                id = "pay_004",
                receiptNumber = "RCP-1004",
                loanId = "loan_008",
                customerId = "cust_008",
                customerName = "Juma Mseti",
                customerPhone = "+255719333444",
                agentId = "agent_daniel_004",
                agentName = "Daniel Ulimwengu",
                amount = 50000L,
                paymentType = PaymentType.PARTIAL_PAYMENT,
                balanceBefore = 240000L,
                balanceAfter = 190000L,
                timestamp = now - 86400000L * 2
            )
        )
        _payments.value = baselinePayments

        // Initial Ledger
        val baselineLedger = listOf(
            LedgerEntry(
                id = "led_001",
                transactionId = "TX-DISB-001",
                transactionType = TransactionType.LOAN_DISBURSEMENT,
                debit = 300000L,
                credit = 0L,
                referenceNumber = "LN001 - Asha Mzee",
                description = "Utoaji wa mkopo (M-Pesa: MPESA-9938124)",
                customerId = "cust_001",
                customerName = "Asha Mzee",
                loanId = "loan_001",
                agentId = "agent_john_001",
                createdBy = "Admin Layson",
                timestamp = now - 86400000L * 10
            ),
            LedgerEntry(
                id = "led_002",
                transactionId = "TX-PAY-001",
                transactionType = TransactionType.REPAYMENT_RECEIVED,
                debit = 0L,
                credit = 100000L,
                referenceNumber = "RCP-1001 - Asha Mzee",
                description = "Marejesho ya mkopo - Cash",
                customerId = "cust_001",
                customerName = "Asha Mzee",
                loanId = "loan_001",
                agentId = "agent_john_001",
                createdBy = "John Mwita",
                timestamp = now - 3600000L * 5
            ),
            LedgerEntry(
                id = "led_003",
                transactionId = "TX-DISB-002",
                transactionType = TransactionType.LOAN_DISBURSEMENT,
                debit = 500000L,
                credit = 0L,
                referenceNumber = "LN002 - Mohamed Salum",
                description = "Utoaji wa mkopo (Airtel: AIRTEL-772819)",
                customerId = "cust_002",
                customerName = "Juma Salim",
                loanId = "loan_002",
                agentId = "agent_john_001",
                createdBy = "Admin Layson",
                timestamp = now - 86400000L * 12
            ),
            LedgerEntry(
                id = "led_004",
                transactionId = "TX-PAY-002",
                transactionType = TransactionType.REPAYMENT_RECEIVED,
                debit = 0L,
                credit = 80000L,
                referenceNumber = "RCP-1002 - Juma Salim",
                description = "Marejesho ya mkopo - Cash",
                customerId = "cust_002",
                customerName = "Juma Salim",
                loanId = "loan_002",
                agentId = "agent_john_001",
                createdBy = "John Mwita",
                timestamp = now - 3600000L * 4
            ),
            LedgerEntry(
                id = "led_005",
                transactionId = "TX-FEE-001",
                transactionType = TransactionType.COLLECTION_FEE,
                debit = 0L,
                credit = 10000L,
                referenceNumber = "LN003 - Amina Hassan",
                description = "Ada ya usajili na ukaguzi",
                customerId = "cust_007",
                customerName = "Amina Hassan",
                loanId = "loan_007",
                agentId = "agent_musa_003",
                createdBy = "Admin Layson",
                timestamp = now - 86400000L * 3
            )
        )
        _ledgerEntries.value = baselineLedger

        // Baseline Notifications
        val baselineNotifications = listOf(
            AppNotification(
                id = "notif_001",
                title = "Maombi Mapya ya Mkopo",
                message = "Neema Juma amewasilisha maombi ya TSh 100,000 kupitia John Mwita.",
                type = "LOAN_APPLICATION",
                recipientRole = "ADMIN",
                timestamp = now - 3600000L * 2
            ),
            AppNotification(
                id = "notif_002",
                title = "Malipo Yamepokelewa",
                message = "John Mwita amerekodi malipo ya TSh 100,000 kutoka kwa Asha Mzee.",
                type = "PAYMENT",
                recipientRole = "ALL",
                timestamp = now - 3600000L * 4
            ),
            AppNotification(
                id = "notif_003",
                title = "Wakala Mpya Amesajiliwa",
                message = "Wakala Sarah Ally amewashwa na kupewa idhini.",
                type = "SYSTEM",
                recipientRole = "ADMIN",
                timestamp = now - 86400000L * 5
            )
        )
        _notifications.value = baselineNotifications

        // Baseline Audit Logs
        val baselineAudit = listOf(
            AuditLog(
                id = "audit_001",
                actorId = "admin_layson_001",
                actorName = "Layson Mngazija",
                actorRole = "ADMIN",
                action = "APPROVE_LOAN",
                targetType = "LOAN",
                targetId = "loan_006",
                details = "Uidhinishaji wa mkopo LN006 wa TSh 500,000 kwa Mohamed Salum",
                timestamp = now - 86400000L * 1
            ),
            AuditLog(
                id = "audit_002",
                actorId = "admin_layson_001",
                actorName = "Layson Mngazija",
                actorRole = "ADMIN",
                action = "DISBURSE_LOAN",
                targetType = "LOAN",
                targetId = "loan_001",
                details = "Utoaji wa fedha TSh 300,000 kwa Asha Mzee (Ref: MPESA-9938124)",
                timestamp = now - 86400000L * 10
            ),
            AuditLog(
                id = "audit_003",
                actorId = "agent_john_001",
                actorName = "John Mwita",
                actorRole = "AGENT",
                action = "RECORD_PAYMENT",
                targetType = "PAYMENT",
                targetId = "pay_001",
                details = "Kupokea marejesho TSh 100,000 kutoka kwa Asha Mzee",
                timestamp = now - 3600000L * 5
            )
        )
        _auditLogs.value = baselineAudit
    }

    private fun setupFirestoreListeners() {
        try {
            // Listen to Customers
            firestore.collection("customers")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                Customer(
                                    id = doc.id,
                                    agentId = doc.getString("agentId") ?: "",
                                    agentName = doc.getString("agentName") ?: "",
                                    fullName = doc.getString("fullName") ?: "",
                                    phone = doc.getString("phone") ?: "",
                                    address = doc.getString("address") ?: "",
                                    photoUrl = doc.getString("photoUrl") ?: "",
                                    guarantorName = doc.getString("guarantorName") ?: "",
                                    guarantorPhone = doc.getString("guarantorPhone") ?: "",
                                    requestedAmount = doc.getLong("requestedAmount") ?: 0L,
                                    loanPurpose = doc.getString("loanPurpose") ?: "",
                                    recipientName = doc.getString("recipientName") ?: "",
                                    recipientPhone = doc.getString("recipientPhone") ?: "",
                                    recipientNetwork = doc.getString("recipientNetwork") ?: "M-Pesa",
                                    notes = doc.getString("notes") ?: "",
                                    status = doc.getString("status") ?: "Active",
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _customers.value = list
                        }
                    }
                }

            // Listen to Loans
            firestore.collection("loans")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                Loan(
                                    id = doc.id,
                                    loanNumber = doc.getString("loanNumber") ?: "",
                                    customerId = doc.getString("customerId") ?: "",
                                    customerName = doc.getString("customerName") ?: "",
                                    customerPhone = doc.getString("customerPhone") ?: "",
                                    customerPhotoUrl = doc.getString("customerPhotoUrl") ?: "",
                                    agentId = doc.getString("agentId") ?: "",
                                    agentName = doc.getString("agentName") ?: "",
                                    principalAmount = doc.getLong("principalAmount") ?: 0L,
                                    interestMethod = InterestMethod.valueOf(doc.getString("interestMethod") ?: "PERCENTAGE"),
                                    interestRate = doc.getDouble("interestRate") ?: 20.0,
                                    interestAmount = doc.getLong("interestAmount") ?: 0L,
                                    totalContractualAmount = doc.getLong("totalContractualAmount") ?: 0L,
                                    installmentAmount = doc.getLong("installmentAmount") ?: 5000L,
                                    durationDays = doc.getLong("durationDays")?.toInt() ?: 24,
                                    status = LoanStatus.valueOf(doc.getString("status") ?: "PENDING"),
                                    recipientName = doc.getString("recipientName") ?: "",
                                    recipientPhone = doc.getString("recipientPhone") ?: "",
                                    recipientNetwork = doc.getString("recipientNetwork") ?: "M-Pesa",
                                    declineReason = doc.getString("declineReason") ?: "",
                                    remainingBalance = doc.getLong("remainingBalance") ?: 0L,
                                    totalPaid = doc.getLong("totalPaid") ?: 0L,
                                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (list.isNotEmpty()) {
                            _loans.value = list
                        }
                    }
                }
        } catch (e: Exception) {
            // Local persistence fallback
        }
    }

    // Agent Registration of Customer + Initial Loan Application
    suspend fun registerCustomerAndLoan(
        customer: Customer,
        principalAmount: Long,
        interestMethod: InterestMethod,
        interestRateOrFixed: Double,
        durationDays: Int,
        installmentAmount: Long,
        frequency: RepaymentFrequency
    ): Result<Pair<Customer, Loan>> {
        val custId = UUID.randomUUID().toString().take(8)
        val loanId = UUID.randomUUID().toString().take(8)
        val loanNum = "LN" + (100 + _loans.value.size + 1)

        val interestAmount = if (interestMethod == InterestMethod.PERCENTAGE) {
            (principalAmount * interestRateOrFixed / 100.0).toLong()
        } else {
            interestRateOrFixed.toLong()
        }
        val totalContractual = principalAmount + interestAmount

        val newCustomer = customer.copy(id = custId, requestedAmount = principalAmount)
        val newLoan = Loan(
            id = loanId,
            loanNumber = loanNum,
            customerId = custId,
            customerName = customer.fullName,
            customerPhone = customer.phone,
            customerPhotoUrl = customer.photoUrl,
            agentId = customer.agentId,
            agentName = customer.agentName,
            principalAmount = principalAmount,
            interestMethod = interestMethod,
            interestRate = if (interestMethod == InterestMethod.PERCENTAGE) interestRateOrFixed else 0.0,
            interestAmount = interestAmount,
            totalContractualAmount = totalContractual,
            installmentAmount = installmentAmount,
            durationDays = durationDays,
            repaymentFrequency = frequency,
            status = LoanStatus.PENDING,
            recipientName = customer.recipientName,
            recipientPhone = customer.recipientPhone,
            recipientNetwork = customer.recipientNetwork,
            remainingBalance = totalContractual,
            totalPaid = 0L,
            createdAt = System.currentTimeMillis()
        )

        // Optimistic local update
        _customers.value = listOf(newCustomer) + _customers.value
        _loans.value = listOf(newLoan) + _loans.value

        // Add Notification for Admin
        addNotification(
            AppNotification(
                id = UUID.randomUUID().toString(),
                title = "Maombi Mapya ya Mkopo",
                message = "${customer.fullName} ameomba TSh ${formatNumber(principalAmount)} kupitia ${customer.agentName}.",
                type = "LOAN_APPLICATION",
                recipientRole = "ADMIN"
            )
        )

        // Write to Firestore asynchronously
        repositoryScope.launch {
            try {
                firestore.collection("customers").document(custId).set(newCustomer).await()
                firestore.collection("loans").document(loanId).set(newLoan).await()
            } catch (e: Exception) {
                // Stays in memory/offline cache
            }
        }

        return Result.success(Pair(newCustomer, newLoan))
    }

    // Admin Approves Loan
    suspend fun approveLoan(loanId: String, adminName: String): Result<Boolean> {
        val target = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Mkopo haukupatikana"))

        val updated = target.copy(
            status = LoanStatus.APPROVED,
            approvedAt = System.currentTimeMillis(),
            approvedBy = adminName,
            updatedAt = System.currentTimeMillis()
        )

        _loans.value = _loans.value.map { if (it.id == loanId) updated else it }

        addAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorId = "admin",
                actorName = adminName,
                actorRole = "ADMIN",
                action = "APPROVE_LOAN",
                targetType = "LOAN",
                targetId = loanId,
                details = "Mkopo ${target.loanNumber} wa TSh ${formatNumber(target.principalAmount)} umeidhinishwa kwa ${target.customerName}."
            )
        )

        addNotification(
            AppNotification(
                id = UUID.randomUUID().toString(),
                title = "Mkopo Umeidhinishwa",
                message = "Mkopo ${target.loanNumber} wa ${target.customerName} (TSh ${formatNumber(target.principalAmount)}) umeidhinishwa na Admin.",
                type = "LOAN_APPROVED",
                recipientRole = "AGENT",
                recipientId = target.agentId
            )
        )

        repositoryScope.launch {
            try {
                firestore.collection("loans").document(loanId).set(updated).await()
            } catch (e: Exception) {
                // offline
            }
        }
        return Result.success(true)
    }

    // Admin Declines Loan
    suspend fun declineLoan(loanId: String, reason: String, adminName: String): Result<Boolean> {
        val target = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Mkopo haukupatikana"))

        val updated = target.copy(
            status = LoanStatus.DECLINED,
            declineReason = reason,
            updatedAt = System.currentTimeMillis()
        )

        _loans.value = _loans.value.map { if (it.id == loanId) updated else it }

        addAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorId = "admin",
                actorName = adminName,
                actorRole = "ADMIN",
                action = "DECLINE_LOAN",
                targetType = "LOAN",
                targetId = loanId,
                details = "Mkopo ${target.loanNumber} umekataliwa. Sababu: $reason"
            )
        )

        repositoryScope.launch {
            try {
                firestore.collection("loans").document(loanId).set(updated).await()
            } catch (e: Exception) {
                // offline
            }
        }
        return Result.success(true)
    }

    // Admin Confirms Disbursement
    suspend fun confirmDisbursement(
        loanId: String,
        amount: Long,
        paymentMethod: String,
        transactionRef: String,
        adminName: String
    ): Result<Boolean> {
        val target = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Mkopo haukupatikana"))

        val updated = target.copy(
            status = LoanStatus.ACTIVE,
            disbursedAt = System.currentTimeMillis(),
            disbursementTxRef = transactionRef,
            disbursementMethod = paymentMethod,
            disbursedBy = adminName,
            updatedAt = System.currentTimeMillis()
        )

        _loans.value = _loans.value.map { if (it.id == loanId) updated else it }

        // Create Ledger Entry: DEBIT for Loan Disbursement
        val ledger = LedgerEntry(
            id = UUID.randomUUID().toString(),
            transactionId = "TX-DISB-" + UUID.randomUUID().toString().take(6).uppercase(),
            transactionType = TransactionType.LOAN_DISBURSEMENT,
            debit = amount,
            credit = 0L,
            referenceNumber = "${target.loanNumber} - ${target.customerName}",
            description = "Utoaji wa fedha za mkopo ($paymentMethod: $transactionRef)",
            customerId = target.customerId,
            customerName = target.customerName,
            loanId = target.id,
            agentId = target.agentId,
            createdBy = adminName,
            timestamp = System.currentTimeMillis()
        )
        _ledgerEntries.value = listOf(ledger) + _ledgerEntries.value

        addAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorId = "admin",
                actorName = adminName,
                actorRole = "ADMIN",
                action = "DISBURSE_LOAN",
                targetType = "LOAN",
                targetId = loanId,
                details = "Kutuma TSh ${formatNumber(amount)} kwa ${target.customerName} ($paymentMethod ref: $transactionRef)"
            )
        )

        addNotification(
            AppNotification(
                id = UUID.randomUUID().toString(),
                title = "Mkopo Umetumwa (Disbursed)",
                message = "Fedha za mkopo ${target.loanNumber} (TSh ${formatNumber(amount)}) zimetumwa kwa ${target.recipientName}.",
                type = "LOAN_DISBURSED",
                recipientRole = "AGENT",
                recipientId = target.agentId
            )
        )

        repositoryScope.launch {
            try {
                firestore.collection("loans").document(loanId).set(updated).await()
                firestore.collection("ledgerEntries").document(ledger.id).set(ledger).await()
            } catch (e: Exception) {
                // offline
            }
        }
        return Result.success(true)
    }

    // Record Cash Repayment (Full or Partial)
    suspend fun recordRepayment(
        loanId: String,
        amount: Long,
        paymentType: PaymentType,
        paymentMethod: String = "CASH",
        notes: String = "",
        agentId: String,
        agentName: String
    ): Result<Pair<Payment, Loan>> {
        val target = _loans.value.find { it.id == loanId }
            ?: return Result.failure(Exception("Mkopo haukupatikana"))

        if (amount <= 0) {
            return Result.failure(Exception("Kiasi cha malipo lazima kiwe zaidi ya 0"))
        }

        if (amount > target.remainingBalance) {
            return Result.failure(Exception("Kiasi kimezidi salio lililobaki (TSh ${formatNumber(target.remainingBalance)})"))
        }

        val balanceBefore = target.remainingBalance
        val balanceAfter = balanceBefore - amount
        val newTotalPaid = target.totalPaid + amount
        val isCompleted = balanceAfter <= 0

        val updatedLoan = target.copy(
            remainingBalance = balanceAfter,
            totalPaid = newTotalPaid,
            status = if (isCompleted) LoanStatus.COMPLETED else LoanStatus.ACTIVE,
            updatedAt = System.currentTimeMillis()
        )

        val receiptNum = "RCP-" + (1000 + _payments.value.size + 1)
        val newPayment = Payment(
            id = UUID.randomUUID().toString(),
            receiptNumber = receiptNum,
            loanId = target.id,
            customerId = target.customerId,
            customerName = target.customerName,
            customerPhone = target.customerPhone,
            agentId = agentId,
            agentName = agentName,
            amount = amount,
            paymentType = paymentType,
            paymentMethod = paymentMethod,
            balanceBefore = balanceBefore,
            balanceAfter = balanceAfter,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        // Create Ledger Entry: CREDIT for Repayment Received
        val ledger = LedgerEntry(
            id = UUID.randomUUID().toString(),
            transactionId = "TX-PAY-" + UUID.randomUUID().toString().take(6).uppercase(),
            transactionType = TransactionType.REPAYMENT_RECEIVED,
            debit = 0L,
            credit = amount,
            referenceNumber = "$receiptNum - ${target.customerName}",
            description = "Marejesho ya mkopo ${target.loanNumber} ($paymentMethod)",
            customerId = target.customerId,
            customerName = target.customerName,
            loanId = target.id,
            agentId = agentId,
            createdBy = agentName,
            timestamp = System.currentTimeMillis()
        )

        // Optimistic local state updates
        _loans.value = _loans.value.map { if (it.id == loanId) updatedLoan else it }
        _payments.value = listOf(newPayment) + _payments.value
        _ledgerEntries.value = listOf(ledger) + _ledgerEntries.value

        addAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorId = agentId,
                actorName = agentName,
                actorRole = "AGENT",
                action = "RECORD_PAYMENT",
                targetType = "PAYMENT",
                targetId = newPayment.id,
                details = "Malipo ya TSh ${formatNumber(amount)} kutoka kwa ${target.customerName}. Salio jipya: TSh ${formatNumber(balanceAfter)}"
            )
        )

        addNotification(
            AppNotification(
                id = UUID.randomUUID().toString(),
                title = "Malipo Yamepokelewa",
                message = "$agentName amepokea TSh ${formatNumber(amount)} kutoka kwa ${target.customerName} ($receiptNum).",
                type = "PAYMENT",
                recipientRole = "ADMIN"
            )
        )

        repositoryScope.launch {
            try {
                firestore.collection("loans").document(loanId).set(updatedLoan).await()
                firestore.collection("payments").document(newPayment.id).set(newPayment).await()
                firestore.collection("ledgerEntries").document(ledger.id).set(ledger).await()
            } catch (e: Exception) {
                // offline
            }
        }

        return Result.success(Pair(newPayment, updatedLoan))
    }

    // Add Agent (Admin only)
    suspend fun addAgent(
        name: String,
        phone: String,
        email: String,
        pin: String
    ): Result<UserProfile> {
        val newAgent = UserProfile(
            id = "agent_" + UUID.randomUUID().toString().take(6),
            name = name,
            email = email.lowercase(),
            phone = phone,
            role = UserRole.AGENT,
            status = AccountStatus.ACTIVE,
            pin = pin,
            createdAt = System.currentTimeMillis()
        )

        _agents.value = listOf(newAgent) + _agents.value

        addAuditLog(
            AuditLog(
                id = UUID.randomUUID().toString(),
                actorId = "admin",
                actorName = "Admin",
                actorRole = "ADMIN",
                action = "ADD_AGENT",
                targetType = "AGENT",
                targetId = newAgent.id,
                details = "Kusajili wakala mpya $name ($phone)"
            )
        )

        repositoryScope.launch {
            try {
                firestore.collection("users").document(newAgent.id).set(newAgent).await()
            } catch (e: Exception) {
                // offline
            }
        }

        return Result.success(newAgent)
    }

    // Toggle Agent Status (Admin only)
    suspend fun toggleAgentStatus(agentId: String): Result<Boolean> {
        val target = _agents.value.find { it.id == agentId }
            ?: return Result.failure(Exception("Wakala haukupatikana"))

        val newStatus = if (target.status == AccountStatus.ACTIVE) AccountStatus.INACTIVE else AccountStatus.ACTIVE
        val updated = target.copy(status = newStatus)

        _agents.value = _agents.value.map { if (it.id == agentId) updated else it }

        repositoryScope.launch {
            try {
                firestore.collection("users").document(agentId).update("status", newStatus.name).await()
            } catch (e: Exception) {
                // offline
            }
        }
        return Result.success(true)
    }

    private fun addNotification(notif: AppNotification) {
        _notifications.value = listOf(notif) + _notifications.value
        repositoryScope.launch {
            try {
                firestore.collection("notifications").document(notif.id).set(notif).await()
            } catch (e: Exception) {
                // offline
            }
        }
    }

    private fun addAuditLog(log: AuditLog) {
        _auditLogs.value = listOf(log) + _auditLogs.value
        repositoryScope.launch {
            try {
                firestore.collection("auditLogs").document(log.id).set(log).await()
            } catch (e: Exception) {
                // offline
            }
        }
    }

    fun markNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    private fun formatNumber(number: Long): String {
        return "%,d".format(number)
    }
}
