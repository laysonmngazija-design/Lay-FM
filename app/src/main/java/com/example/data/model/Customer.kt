package com.example.data.model

data class Customer(
    val id: String = "",
    val agentId: String = "",
    val agentName: String = "",
    val fullName: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val guarantorName: String = "",
    val guarantorPhone: String = "",
    val requestedAmount: Long = 0L,
    val loanPurpose: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val recipientNetwork: String = "M-Pesa",
    val notes: String = "",
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
