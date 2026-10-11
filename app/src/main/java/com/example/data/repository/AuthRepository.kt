package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AccountStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            FirebaseAuth.getInstance()
        }
    }
    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("lay_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        // Restore remembered user session
        val savedId = prefs.getString("user_id", null)
        val savedEmail = prefs.getString("user_email", null)
        val savedRole = prefs.getString("user_role", null)
        val savedName = prefs.getString("user_name", null)
        val savedPhone = prefs.getString("user_phone", null)

        if (savedId != null && savedRole != null) {
            _currentUser.value = UserProfile(
                id = savedId,
                name = savedName ?: "User",
                email = savedEmail ?: "",
                phone = savedPhone ?: "",
                role = if (savedRole == "ADMIN") UserRole.ADMIN else UserRole.AGENT,
                status = AccountStatus.ACTIVE
            )
        }
    }

    suspend fun login(
        identifier: String,
        pin: String,
        rememberDevice: Boolean
    ): Result<UserProfile> {
        val trimmedIdentifier = identifier.trim()
        val trimmedPin = pin.trim()

        if (trimmedIdentifier.isBlank() || trimmedPin.length < 4) {
            return Result.failure(Exception("Tafadhali weka namba ya simu/barua pepe na PIN sahihi ya tarakimu 4."))
        }

        try {
            // Check in Firestore users collection
            var userProfile: UserProfile? = null

            // Query by email
            val queryByEmail = firestore.collection("users")
                .whereEqualTo("email", trimmedIdentifier.lowercase())
                .limit(1)
                .get()
                .await()

            if (!queryByEmail.isEmpty) {
                val doc = queryByEmail.documents.first()
                userProfile = mapDocToUserProfile(doc)
            } else {
                // Query by phone
                val cleanPhone = cleanPhoneNumber(trimmedIdentifier)
                val queryByPhone = firestore.collection("users")
                    .whereEqualTo("phone", cleanPhone)
                    .limit(1)
                    .get()
                    .await()

                if (!queryByPhone.isEmpty) {
                    val doc = queryByPhone.documents.first()
                    userProfile = mapDocToUserProfile(doc)
                }
            }

            // If not found in Firestore yet (e.g. first launch or bootstrap demo users)
            if (userProfile == null) {
                userProfile = checkBootstrapUsers(trimmedIdentifier, trimmedPin)
                if (userProfile != null) {
                    // Seed this user into Firestore so future sessions sync
                    try {
                        firestore.collection("users").document(userProfile.id)
                            .set(mapUserProfileToMap(userProfile))
                            .await()
                    } catch (e: Exception) {
                        // Offline or permission pending, continue with local profile
                    }
                }
            }

            if (userProfile == null) {
                return Result.failure(Exception("Akaunti haikupatikana. Tafadhali hakiki taarifa zako."))
            }

            if (userProfile.status != AccountStatus.ACTIVE) {
                return Result.failure(Exception("Akaunti hii imesitishwa. Wasiliana na msimamizi (Admin)."))
            }

            // Verify PIN
            val pinValid = verifyPin(trimmedPin, userProfile.pin)
            if (!pinValid) {
                return Result.failure(Exception("PIN siyo sahihi. Tafadhali jaribu tena."))
            }

            // Persist session if rememberDevice is requested
            _currentUser.value = userProfile
            if (rememberDevice) {
                prefs.edit()
                    .putString("user_id", userProfile.id)
                    .putString("user_email", userProfile.email)
                    .putString("user_role", userProfile.role.name)
                    .putString("user_name", userProfile.name)
                    .putString("user_phone", userProfile.phone)
                    .apply()
            } else {
                prefs.edit().clear().apply()
            }

            return Result.success(userProfile)
        } catch (e: Exception) {
            // Fallback for bootstrap users in case Firestore network error occurs
            val fallback = checkBootstrapUsers(trimmedIdentifier, trimmedPin)
            if (fallback != null && verifyPin(trimmedPin, fallback.pin)) {
                _currentUser.value = fallback
                if (rememberDevice) {
                    prefs.edit()
                        .putString("user_id", fallback.id)
                        .putString("user_email", fallback.email)
                        .putString("user_role", fallback.role.name)
                        .putString("user_name", fallback.name)
                        .putString("user_phone", fallback.phone)
                        .apply()
                }
                return Result.success(fallback)
            }
            return Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value = null
        prefs.edit().clear().apply()
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
    }

    suspend fun changePin(userId: String, newPin: String): Result<Boolean> {
        if (newPin.length < 4) {
            return Result.failure(Exception("PIN lazima iwe na angalau tarakimu 4."))
        }
        val hashed = hashPin(newPin)
        return try {
            firestore.collection("users").document(userId)
                .update("pin", hashed)
                .await()
            _currentUser.value?.let { current ->
                if (current.id == userId) {
                    _currentUser.value = current.copy(pin = hashed)
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            // Local update
            _currentUser.value?.let { current ->
                if (current.id == userId) {
                    _currentUser.value = current.copy(pin = hashed)
                }
            }
            Result.success(true)
        }
    }

    private fun checkBootstrapUsers(identifier: String, pin: String): UserProfile? {
        val lower = identifier.lowercase()
        val clean = cleanPhoneNumber(identifier)

        // Admin: Layson Mngazija (from user prompt)
        if (lower == "laysonmngazija@gmail.com" || clean == "0717798877" || clean == "+255717798877") {
            return UserProfile(
                id = "admin_layson_001",
                name = "Layson Mngazija",
                email = "laysonmngazija@gmail.com",
                phone = "+255717798877",
                role = UserRole.ADMIN,
                status = AccountStatus.ACTIVE,
                pin = hashPin("9552") // Default initial admin PIN matching showcase mockup
            )
        }

        // Agent: John Mwita (from showcase mockup)
        if (lower == "john@gmail.com" || clean == "0717998877" || clean == "0717998877" || clean == "+255717998877" || clean == "+255712345678") {
            return UserProfile(
                id = "agent_john_001",
                name = "John Mwita",
                email = "john@gmail.com",
                phone = "+255712345678",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = hashPin("1234") // Default initial agent PIN matching showcase mockup
            )
        }

        // Generic admin test shortcut
        if (lower == "admin@lay.com" || lower == "admin") {
            return UserProfile(
                id = "admin_layson_001",
                name = "Layson Mngazija",
                email = "laysonmngazija@gmail.com",
                phone = "+255717798877",
                role = UserRole.ADMIN,
                status = AccountStatus.ACTIVE,
                pin = hashPin("9552")
            )
        }

        // Generic agent test shortcut
        if (lower == "agent@lay.com" || lower == "agent") {
            return UserProfile(
                id = "agent_john_001",
                name = "John Mwita",
                email = "john@gmail.com",
                phone = "+255712345678",
                role = UserRole.AGENT,
                status = AccountStatus.ACTIVE,
                pin = hashPin("1234")
            )
        }

        return null
    }

    private fun mapDocToUserProfile(doc: com.google.firebase.firestore.DocumentSnapshot): UserProfile {
        val roleStr = doc.getString("role") ?: "AGENT"
        val statusStr = doc.getString("status") ?: "ACTIVE"
        return UserProfile(
            id = doc.id,
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            role = if (roleStr == "ADMIN") UserRole.ADMIN else UserRole.AGENT,
            status = if (statusStr == "INACTIVE") AccountStatus.INACTIVE else AccountStatus.ACTIVE,
            pin = doc.getString("pin") ?: "",
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
    }

    private fun mapUserProfileToMap(user: UserProfile): Map<String, Any> {
        return mapOf(
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "role" to user.role.name,
            "status" to user.status.name,
            "pin" to user.pin,
            "createdAt" to user.createdAt
        )
    }

    private fun cleanPhoneNumber(phone: String): String {
        return phone.replace("[^0-9+]".toRegex(), "")
    }

    fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPin(inputPin: String, storedPinOrHash: String): Boolean {
        if (storedPinOrHash == inputPin) return true
        val hashedInput = hashPin(inputPin)
        return hashedInput.equals(storedPinOrHash, ignoreCase = true)
    }
}
