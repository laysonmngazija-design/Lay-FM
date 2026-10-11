package com.example.data.model

enum class PaymentType {
    FULL_PAYMENT,
    PARTIAL_PAYMENT
}

data class Payment(
    val id: String = "",
    val receiptNumber: String = "",
    val loanId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val agentId: String = "",
    val agentName: String = "",
    val amount: Long = 0L,
    val paymentType: PaymentType = PaymentType.PARTIAL_PAYMENT,
    val paymentMethod: String = "CASH",
    val balanceBefore: Long = 0L,
    val balanceAfter: Long = 0L,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
