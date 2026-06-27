package com.example.sololeveling90days.ui.tools

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.delay


enum class BreathPhase(
    val label: String,
    val instruction: String,
    val durationMs: Long,
    val color: Color
) {
    INHALE("Inhale", "Breathe in slowly\u2026", 4000L, Color(0xFF0891B2)),
    HOLD_IN("Hold", "Hold your breath\u2026", 4000L, Color(0xFF7C3AED)),
    EXHALE("Exhale", "Release slowly\u2026", 4000L, Color(0xFF10B981)),
    HOLD_OUT("Hold", "Rest\u2026", 2000L, Color(0xFF475569))
}

@Composable
private fun colorForPhase(phase: BreathPhase): Color = when (phase) {
    BreathPhase.INHALE   -> AppleBlue
    BreathPhase.HOLD_IN  -> ActionOrange
    BreathPhase.EXHALE   -> SuccessGreen
    BreathPhase.HOLD_OUT -> SteelGray
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val durationOptions = listOf(5, 10, 15, 20)
    var selectedDuration by rememberSaveable { mutableIntStateOf(10) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var currentPhaseName by rememberSaveable { mutableStateOf("INHALE") }
    val currentPhase = try { BreathPhase.valueOf(currentPhaseName) } catch (e: Exception) { BreathPhase.INHALE }
    var phaseProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var sessionProgress by rememberSaveable { mutableFloatStateOf(0f) }
    var elapsedMs by rememberSaveable { mutableLongStateOf(0L) }
    var sessionComplete by rememberSaveable { mutableStateOf(false) }
    var phaseIndex by rememberSaveable { mutableIntStateOf(0) }

    val totalMs = selectedDuration * 60 * 1000L
    val cycleDuration = BreathPhase.values().sumOf { it.durationMs }

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        sessionComplete = false
        val phaseList = BreathPhase.values().toList()

        while (isRunning && elapsedMs < totalMs) {
            val phase = phaseList[phaseIndex % phaseList.size]
            currentPhaseName = phase.name
            val phaseDuration = phase.durationMs
            var phaseElapsed = (phaseProgress * phaseDuration).toLong().coerceIn(0L, phaseDuration)
            while (phaseElapsed < phaseDuration && isRunning && elapsedMs < totalMs) {
                delay(50L)
                phaseElapsed += 50L
                elapsedMs += 50L
                phaseProgress = phaseElapsed.toFloat() / phaseDuration
                sessionProgress = elapsedMs.toFloat() / totalMs
            }
            if (isRunning && elapsedMs < totalMs) {
                phaseIndex++
                phaseProgress = 0f
            }
        }
        if (isRunning) {
            isRunning = false
            sessionComplete = true
            repository.completeMeditationSession(selectedDuration)
        }
    }

    // Circle size animation
    val circleScale by animateFloatAsState(
        targetValue = when {
            !isRunning -> 1f
            currentPhase == BreathPhase.INHALE -> 0.7f + 0.3f * phaseProgress
            currentPhase == BreathPhase.HOLD_IN -> 1f
            currentPhase == BreathPhase.EXHALE -> 1f - 0.3f * phaseProgress
            currentPhase == BreathPhase.HOLD_OUT -> 0.7f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "circleScale"
    )

    val phaseColor = if (isRunning) colorForPhase(currentPhase) else CardWisdom

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Meditation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { isRunning = false; onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Duration selector
            if (!isRunning) {
                Text("Session Duration", color = TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    durationOptions.forEach { min ->
                        val selected = selectedDuration == min
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) CardWisdom else DarkCard)
                                .border(
                                    1.dp,
                                    if (selected) CardWisdom else TextMuted,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedDuration = min }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${min}m",
                                color = if (selected) Color.White else TextSecondary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(36.dp))
            }

            // Main breathing circle
            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(circleScale * 1.15f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    phaseColor.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // Main circle
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(circleScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    phaseColor.copy(alpha = 0.35f),
                                    phaseColor.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(2.dp, phaseColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isRunning) {
                            Text(
                                text = currentPhase.label,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = currentPhase.instruction,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        } else if (sessionComplete) {
                            Text("\uD83C\uDF89", fontSize = 42.sp)
                            Text(
                                "Complete!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        } else {
                            Text("\uD83E\uDDD8", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Ready",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Phase instruction (below circle)
            if (isRunning) {
                // Phase progress bar
                LinearProgressIndicator(
                    progress = { phaseProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = phaseColor,
                    trackColor = DarkCard
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = currentPhase.label.uppercase(),
                    color = phaseColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(24.dp))
            }

            // Session progress
            if (isRunning || sessionComplete) {
                Text(
                    "Session Progress",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { sessionProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CardWisdom,
                    trackColor = DarkCard
                )
                Spacer(Modifier.height(4.dp))
                val elapsed = (elapsedMs / 1000).toInt()
                val total = selectedDuration * 60
                Text(
                    "${elapsed / 60}:${"%02d".format(elapsed % 60)} / ${total / 60}:00",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(32.dp))
            }

            // Start / Stop button
            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                    } else {
                        sessionComplete = false
                        phaseProgress = 0f
                        sessionProgress = 0f
                        elapsedMs = 0L
                        phaseIndex = 0
                        currentPhaseName = BreathPhase.INHALE.name
                        isRunning = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) HardRedLight else CardWisdom
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isRunning) "Stop Session" else if (sessionComplete) "Start Again" else "Begin Meditation",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Breathing cycle info
            if (!isRunning) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Breathing Pattern",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        BreathPhase.values().forEach { phase ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(colorForPhase(phase))
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(phase.label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Text("${phase.durationMs / 1000}s", color = colorForPhase(phase), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
