package com.example.sololeveling90days.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    private val auth = SupabaseClient.client.auth

    val sessionStatus: Flow<io.github.jan.supabase.auth.status.SessionStatus> = auth.sessionStatus

    suspend fun signUp(email: String, password: String, displayName: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("display_name", displayName)
            }
        }
    }

    suspend fun resendVerification(email: String) {
        // Resend the signup confirmation email
        auth.resendEmail(io.github.jan.supabase.auth.OtpType.Email.SIGNUP, email)
    }


    suspend fun verifyOtp(email: String, code: String) {
        auth.verifyEmailOtp(
            type = io.github.jan.supabase.auth.OtpType.Email.SIGNUP,
            email = email,
            token = code
        )
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun currentUserId(): String? {
        val status = auth.sessionStatus.value
        return (status as? io.github.jan.supabase.auth.status.SessionStatus.Authenticated)?.session?.user?.id
            ?: auth.currentSessionOrNull()?.user?.id
    }

    fun currentUserEmail(): String? {
        val status = auth.sessionStatus.value
        return (status as? io.github.jan.supabase.auth.status.SessionStatus.Authenticated)?.session?.user?.email
            ?: auth.currentSessionOrNull()?.user?.email
    }

    fun isAuthenticated(): Boolean {
        val status = auth.sessionStatus.value
        return status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated || auth.currentSessionOrNull() != null
    }
}
