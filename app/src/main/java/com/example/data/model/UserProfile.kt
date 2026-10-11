package com.example.data.model

enum class UserRole {
    ADMIN,
    AGENT
}

enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.AGENT,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val pin: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
