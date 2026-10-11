package com.example.data.model

data class AuditLog(
    val id: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val actorRole: String = "",
    val action: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL", // LOAN_APPLICATION, LOAN_APPROVED, PAYMENT, OVERDUE, SYSTEM
    val recipientRole: String = "ALL", // ADMIN, AGENT, ALL
    val recipientId: String = "",
    val relatedEntityId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
