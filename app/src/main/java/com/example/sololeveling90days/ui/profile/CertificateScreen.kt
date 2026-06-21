package com.example.sololeveling90days.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.UserProfile
import com.example.sololeveling90days.data.rankFromXp
import com.example.sololeveling90days.theme.*

@Composable
fun CertificateScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val profile by repository.userProfile.collectAsStateWithLifecycle(UserProfile())

    val goldShimmer by rememberInfiniteTransition(label = "gold").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "goldShimmer"
    )

    val rank = rankFromXp(profile.totalXp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A1A), DarkBg, Color(0xFF1A1000))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("[SYSTEM]: S-CLASS CERTIFICATION",
                color = HardRedLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            // Certificate card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1000), Color(0xFF0B0F1A), Color(0xFF1A1000))
                        )
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(LevelUpGold, XPGold.copy(alpha = goldShimmer), LevelUpGold)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("\uD83C\uDFC6", fontSize = 56.sp)

                    Text(
                        "S-CLASS HUNTER",
                        color = LevelUpGold,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        "CERTIFICATION",
                        color = XPGold,
                        fontSize = 14.sp,
                        letterSpacing = 4.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Divider(color = LevelUpGold.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        profile.name.ifBlank { "Hunter" }.uppercase(),
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CertStat("FINAL RANK", rank.label)
                        CertStat("TOTAL XP", "${profile.totalXp}")
                        CertStat("BEST STREAK", "${profile.streak} days")
                    }

                    Divider(color = LevelUpGold.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "90-Day Challenge Complete",
                        color = GrowthEmerald,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Developed by Sabari",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                "Hesitation is Defeat.\nYou did not hesitate.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Button(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LevelUpGold),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Return to Home", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CertStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 9.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = LevelUpGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
