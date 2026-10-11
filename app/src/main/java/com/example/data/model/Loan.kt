package com.example.data.model

enum class LoanStatus {
    DRAFT,
    PENDING,
    APPROVED,
    DECLINED,
    DISBURSED,
    ACTIVE,
    OVERDUE,
    COMPLETED,
    CANCELLED
}

enum class InterestMethod {
    PERCENTAGE,
    FIXED_AMOUNT
}

enum class RepaymentFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class RepaymentScheduleItem(
    val installmentNumber: Int = 1,
    val dueDate: Long = 0L,
    val amountDue: Long = 0L,
    val amountPaid: Long = 0L,
    val isPaid: Boolean = false,
    val isOverdue: Boolean = false
)

data class Loan(
    val id: String = "",
    val loanNumber: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerPhotoUrl: String = "",
    val agentId: String = "",
    val agentName: String = "",
    val principalAmount: Long = 0L,
    val interestMethod: InterestMethod = InterestMethod.PERCENTAGE,
    val interestRate: Double = 20.0,
    val interestAmount: Long = 0L,
    val totalContractualAmount: Long = 0L,
    val installmentAmount: Long = 5000L,
    val repaymentFrequency: RepaymentFrequency = RepaymentFrequency.DAILY,
    val durationDays: Int = 24,
    val firstDueDate: Long = 0L,
    val dailyDeadline: String = "18:00",
    val status: LoanStatus = LoanStatus.PENDING,
    val recipientName: String = "",
    val recipientPhone: String = "",
    val recipientNetwork: String = "M-Pesa",
    val declineReason: String = "",
    val approvedAt: Long = 0L,
    val approvedBy: String = "",
    val disbursedAt: Long = 0L,
    val disbursementTxRef: String = "",
    val disbursementMethod: String = "",
    val disbursedBy: String = "",
    val remainingBalance: Long = 0L,
    val totalPaid: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
