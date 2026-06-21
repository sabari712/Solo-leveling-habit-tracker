package com.example.sololeveling90days.ui.onboarding

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
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch

@Composable
fun ComebackScreen(
    repository: AppRepository,
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0A), DarkBg, Color(0xFF1A0A0A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("\u26A0\u008F", fontSize = (56 * pulse).sp)

            Text(
                text = "[SYSTEM]",
                color = HardRedLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )

            Text(
                text = "RETURN OF THE HUNTER",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Hunter, you went offline.\nComplete this comeback quest within 2 hours to restore 50% of your streak.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // Comeback Quest Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A0A0A))
                    .border(1.dp, HardRedLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\u26A1", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "COMEBACK QUEST",
                            color = HardRedLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        "Do 20 push-ups RIGHT NOW and return stronger than before.",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("+50% streak restored on completion", color = GrowthEmerald, fontSize = 12.sp)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        repository.completeComebackQuest()
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HardRedLight)
            ) {
                Text("QUEST COMPLETE \u2014 RESTORE STREAK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            TextButton(onClick = {
                scope.launch {
                    repository.dismissComeback()
                    onDismiss()
                }
            }) {
                Text("Skip (streak stays reset)", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}
