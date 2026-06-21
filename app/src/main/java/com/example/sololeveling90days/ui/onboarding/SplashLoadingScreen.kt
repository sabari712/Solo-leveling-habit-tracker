package com.example.sololeveling90days.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.R
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashLoadingScreen(
    onTimeout: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var fadeOutActive by remember { mutableStateOf(false) }

    // Animate progress to 1f over 2 seconds
    LaunchedEffect(Unit) {
        val duration = 2000L
        val stepTime = 50L
        val steps = duration / stepTime
        for (i in 0..steps) {
            progress = i.toFloat() / steps
            delay(stepTime)
        }
        fadeOutActive = true
        delay(350L) // Wait for fade-out transition
        onTimeout()
    }

    val alpha by animateFloatAsState(
        targetValue = if (fadeOutActive) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "splash_fade"
    )

    // Pulse animation for logo gold shadow glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBase)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        // Background subtle radial glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LevelUpGold.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp)
        ) {
            // Spacer to push content down
            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // Logo Container
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ObsidianBase)
                        .border(
                            1.dp,
                            SteelGray.copy(alpha = 0.3f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Solo Leveling Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Solo Leveling",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "90 DAY CHALLENGE",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = SteelGray,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Loading bar
                Column(
                    modifier = Modifier.width(180.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(DarkCardAlt)
                    ) {
                        // Progress Fill
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(GrowthEmerald, LevelUpGold)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status text row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INITIATING SEQUENCE",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SteelGray
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthEmerald
                        )
                    }
                }
            }

            // Footer Attribution
            Text(
                text = "created by sabari",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = SteelGray.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )
        }
    }
}
