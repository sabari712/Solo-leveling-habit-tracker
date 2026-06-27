package com.example.sololeveling90days.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.AuthRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showVerificationSent by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var resendSuccessMessage by remember { mutableStateOf<String?>(null) }
    var resendErrorMessage by remember { mutableStateOf<String?>(null) }
    var otpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showVerificationSent) {
            // Email Verification Sent Card
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Verify Your Email",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "A 6-digit verification code has been sent to:\n$email\n\nEnter the code below to awaken your Hunter account.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = otpCode,
                        onValueChange = { otpCode = it; verificationError = null },
                        placeholder = { Text("Enter 6-digit Code...", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = AppleBlue,
                            unfocusedIndicatorColor = AppleBlue.copy(alpha = 0.2f),
                            cursorColor = AppleBlue,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (verificationError != null) {
                        Text(
                            text = verificationError!!,
                            color = HardRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            if (otpCode.isBlank()) {
                                verificationError = "Please enter verification code."
                                return@Button
                            }
                            isVerifying = true
                            scope.launch {
                                try {
                                    authRepository.verifyOtp(email.trim(), otpCode.trim())
                                    onAuthSuccess()
                                } catch (e: Exception) {
                                    verificationError = e.localizedMessage ?: "Invalid code."
                                } finally {
                                    isVerifying = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isVerifying
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "VERIFY & AWAKEN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (resendSuccessMessage != null) {
                        Text(
                            text = resendSuccessMessage!!,
                            color = SuccessGreen,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (resendErrorMessage != null) {
                        Text(
                            text = resendErrorMessage!!,
                            color = HardRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resend Code",
                            color = AppleBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(enabled = !isResending) {
                                    isResending = true
                                    resendSuccessMessage = null
                                    resendErrorMessage = null
                                    scope.launch {
                                        try {
                                            authRepository.resendVerification(email.trim())
                                            resendSuccessMessage = "Verification code resent successfully!"
                                        } catch (e: Exception) {
                                            resendErrorMessage = e.localizedMessage ?: "Failed to resend code."
                                        } finally {
                                            isResending = false
                                        }
                                    }
                                }
                        )

                        Text(
                            text = "← Back to Login",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                showVerificationSent = false
                                otpCode = ""
                                verificationError = null
                            }
                        )
                    }
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Main Auth Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // S-Rank Header Title
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SYSTEM_LOGIN",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = AppleBlue,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.02).sp
                            )
                            Text(
                                text = "VERIFY PLAYER CREDENTIALS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Input Form Fields with Bottom Borders
                        if (isSignUp) {
                            TextField(
                                value = displayName,
                                onValueChange = { displayName = it; errorMessage = null },
                                placeholder = { Text("Enter Display Name...", color = TextSecondary.copy(alpha = 0.5f)) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = AppleBlue,
                                    unfocusedIndicatorColor = AppleBlue.copy(alpha = 0.2f),
                                    cursorColor = AppleBlue,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        TextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            placeholder = { Text("Enter Email...", color = TextSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = AppleBlue,
                                unfocusedIndicatorColor = AppleBlue.copy(alpha = 0.2f),
                                cursorColor = AppleBlue,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            placeholder = { Text("Password", color = TextSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = null, tint = TextSecondary)
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = AppleBlue,
                                unfocusedIndicatorColor = AppleBlue.copy(alpha = 0.2f),
                                cursorColor = AppleBlue,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = HardRed,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Awaken Action Button (S-Rank style)
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank() || (isSignUp && displayName.isBlank())) {
                                    errorMessage = "Please fill in all fields."
                                    return@Button
                                }
                                isLoading = true
                                scope.launch {
                                    try {
                                        if (isSignUp) {
                                            try {
                                                authRepository.signUp(email.trim(), password.trim(), displayName.trim())
                                                if (!authRepository.isAuthenticated()) {
                                                    showVerificationSent = true
                                                } else {
                                                    onAuthSuccess()
                                                }
                                            } catch (e: Exception) {
                                                val msg = e.localizedMessage ?: ""
                                                if (msg.contains("already registered", true) || 
                                                    msg.contains("already exists", true) || 
                                                    msg.contains("already in use", true)
                                                ) {
                                                    verificationError = "This player is already registered but not verified. Enter code or request a new one."
                                                    showVerificationSent = true
                                                } else {
                                                    throw e
                                                }
                                            }
                                        } else {
                                            try {
                                                authRepository.signIn(email.trim(), password.trim())
                                                onAuthSuccess()
                                            } catch (e: Exception) {
                                                val msg = e.localizedMessage ?: ""
                                                if (msg.contains("Email not confirmed", true) || msg.contains("confirm", true)) {
                                                    verificationError = "Email not verified yet. Enter the code sent to your email."
                                                    showVerificationSent = true
                                                } else {
                                                    throw e
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        val msg = e.localizedMessage ?: ""
                                        errorMessage = when {
                                            msg.contains("Invalid login", true) || msg.contains("invalid credentials", true) -> "Invalid player ID or key."
                                            msg.contains("Email not confirmed", true) -> "Confirm your email first."
                                            else -> msg
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (isSignUp) "AWAKEN" else "AWAKEN",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Visibility, // Stand-in for bolt
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Secondary Nav Links
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUp) "Sign In" else "Register Player",
                                color = AppleBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isSignUp = !isSignUp
                                        errorMessage = null
                                    }
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                text = "Recover Key",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable {
                                        errorMessage = "Please contact system administrator."
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Offline Guest Mode Button
                Row(
                    modifier = Modifier
                        .clickable { onContinueAsGuest() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Offline Guest Mode",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
