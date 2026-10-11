package com.example.data.model

enum class TransactionType {
    LOAN_DISBURSEMENT, // Cash Outflow / Debit to Cash
    REPAYMENT_RECEIVED, // Cash Inflow / Credit to Cash
    COLLECTION_FEE,
    REVERSAL_ADJUSTMENT
}

data class LedgerEntry(
    val id: String = "",
    val transactionId: String = "",
    val transactionType: TransactionType = TransactionType.REPAYMENT_RECEIVED,
    val debit: Long = 0L,
    val credit: Long = 0L,
    val currency: String = "TZS",
    val referenceNumber: String = "",
    val description: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val loanId: String = "",
    val agentId: String = "",
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
