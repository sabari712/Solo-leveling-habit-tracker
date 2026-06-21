package com.example.sololeveling90days.ui.tools

import android.os.CountDownTimer
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

enum class TimerMode { WORK, BREAK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val savedWork by repository.focusTimerWork.collectAsState(initial = 25)
    val savedBreak by repository.focusTimerBreak.collectAsState(initial = 5)
    val savedTargetEpoch by repository.timerTargetEpoch.collectAsState(initial = 0L)
    val savedIsRunning by repository.timerIsRunning.collectAsState(initial = false)
    val savedTimerMode by repository.timerMode.collectAsState(initial = "WORK")
    val savedRemaining by repository.timerSavedRemaining.collectAsState(initial = 0L)

    var workMinutes by rememberSaveable { mutableIntStateOf(25) }
    var breakMinutes by rememberSaveable { mutableIntStateOf(5) }
    var mode by rememberSaveable { mutableStateOf(TimerMode.WORK) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var millisRemaining by rememberSaveable { mutableLongStateOf(0L) }
    var totalMillis by rememberSaveable { mutableLongStateOf(0L) }
    var countDownTimer by remember { mutableStateOf<CountDownTimer?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var workInput by rememberSaveable { mutableStateOf("25") }
    var breakInput by rememberSaveable { mutableStateOf("5") }

    var isInitialized by rememberSaveable { mutableStateOf(false) }

    fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
        scope.launch {
            repository.saveTimerState(
                isRunning = false,
                targetEpoch = 0L,
                mode = mode.name,
                remaining = millisRemaining
            )
        }
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
        val mins = if (mode == TimerMode.WORK) workMinutes else breakMinutes
        millisRemaining = mins * 60 * 1000L
        totalMillis = millisRemaining
        scope.launch {
            repository.saveTimerState(
                isRunning = false,
                targetEpoch = 0L,
                mode = mode.name,
                remaining = millisRemaining
            )
        }
    }

    fun startTimer() {
        if (millisRemaining <= 0L) {
            val mins = if (mode == TimerMode.WORK) workMinutes else breakMinutes
            millisRemaining = mins * 60 * 1000L
            totalMillis = millisRemaining
        }
        val target = System.currentTimeMillis() + millisRemaining
        scope.launch {
            repository.saveTimerState(
                isRunning = true,
                targetEpoch = target,
                mode = mode.name,
                remaining = millisRemaining
            )
        }
        countDownTimer = object : CountDownTimer(millisRemaining, 100L) {
            override fun onTick(remaining: Long) {
                millisRemaining = remaining
            }
            override fun onFinish() {
                isRunning = false
                millisRemaining = 0L
                mode = if (mode == TimerMode.WORK) TimerMode.BREAK else TimerMode.WORK
                val nextMins = if (mode == TimerMode.BREAK) breakMinutes else workMinutes
                millisRemaining = nextMins * 60 * 1000L
                totalMillis = millisRemaining
                scope.launch {
                    repository.saveTimerState(
                        isRunning = false,
                        targetEpoch = 0L,
                        mode = mode.name,
                        remaining = millisRemaining
                    )
                }
            }
        }.start()
        isRunning = true
    }

    // Sync saved prefs once loaded
    LaunchedEffect(savedWork, savedBreak, savedTargetEpoch, savedIsRunning, savedTimerMode, savedRemaining) {
        if (isInitialized) return@LaunchedEffect

        mode = if (savedTimerMode == "BREAK") TimerMode.BREAK else TimerMode.WORK
        workMinutes = savedWork
        breakMinutes = savedBreak
        workInput = savedWork.toString()
        breakInput = savedBreak.toString()

        val currentTime = System.currentTimeMillis()
        if (savedIsRunning) {
            if (currentTime < savedTargetEpoch) {
                millisRemaining = savedTargetEpoch - currentTime
                val currentModeMins = if (mode == TimerMode.WORK) workMinutes else breakMinutes
                totalMillis = currentModeMins * 60 * 1000L
                startTimer()
            } else {
                stopTimer()
                mode = if (mode == TimerMode.WORK) TimerMode.BREAK else TimerMode.WORK
                val nextMins = if (mode == TimerMode.BREAK) breakMinutes else workMinutes
                millisRemaining = nextMins * 60 * 1000L
                totalMillis = millisRemaining
                scope.launch {
                    repository.saveTimerState(
                        isRunning = false,
                        targetEpoch = 0L,
                        mode = mode.name,
                        remaining = millisRemaining
                    )
                }
            }
        } else {
            millisRemaining = if (savedRemaining > 0L) savedRemaining else (savedWork * 60 * 1000L)
            val currentModeMins = if (mode == TimerMode.WORK) workMinutes else breakMinutes
            totalMillis = currentModeMins * 60 * 1000L
        }
        isInitialized = true
    }

    DisposableEffect(Unit) {
        onDispose { countDownTimer?.cancel() }
    }

    // Pulsing animation when running
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val accentColor = if (mode == TimerMode.WORK) PrimaryPurple else CardWisdom
    val progressFraction = if (totalMillis > 0L) millisRemaining.toFloat() / totalMillis else 0f
    val minutesLeft = (millisRemaining / 1000 / 60).toInt()
    val secondsLeft = (millisRemaining / 1000 % 60).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Focus Timer", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { stopTimer(); onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { showSettings = !showSettings }) {
                        Text("\u2699\u008F Settings", color = TextSecondary, fontSize = 13.sp)
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
            Spacer(Modifier.height(24.dp))

            // Mode toggle chips
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(TimerMode.WORK to "Work Session", TimerMode.BREAK to "Break Time").forEach { (m, label) ->
                    val selected = mode == m
                    val chipColor by animateColorAsState(
                        if (selected) accentColor else DarkCard, label = "chip"
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipColor)
                            .border(
                                1.dp,
                                if (selected) accentColor else TextMuted,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (!isRunning) {
                                    stopTimer()
                                    mode = m
                                    val mins = if (m == TimerMode.WORK) workMinutes else breakMinutes
                                    millisRemaining = mins * 60 * 1000L
                                    totalMillis = millisRemaining
                                    scope.launch {
                                        repository.saveTimerState(
                                            isRunning = false,
                                            targetEpoch = 0L,
                                            mode = mode.name,
                                            remaining = millisRemaining
                                        )
                                    }
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color.White else TextSecondary,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Circular timer
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .scale(pulseScale)
                ) {
                    // Background ring
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = DarkCard,
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round
                    )
                    // Progress ring
                    CircularProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxSize(),
                        color = accentColor,
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round,
                        trackColor = Color.Transparent
                    )
                }

                // Inner circle + time display
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.15f),
                                    DarkSurface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%02d:%02d".format(minutesLeft, secondsLeft),
                            color = TextPrimary,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (mode == TimerMode.WORK) "\uD83C\uDFAF Focus" else "\u2615 Rest",
                            color = accentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Mode label
            Text(
                text = if (mode == TimerMode.WORK) "Work Session" else "Break Time",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (mode == TimerMode.WORK)
                    "Stay focused. No distractions." else "Breathe. You earned this.",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(36.dp))

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset
                IconButton(
                    onClick = { resetTimer() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = TextSecondary, modifier = Modifier.size(24.dp))
                }

                // Play/Pause (large)
                Button(
                    onClick = {
                        if (isRunning) {
                            stopTimer()
                        } else {
                            startTimer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Skip to next
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DarkCard)
                        .clickable {
                             stopTimer()
                             mode = if (mode == TimerMode.WORK) TimerMode.BREAK else TimerMode.WORK
                             val mins = if (mode == TimerMode.BREAK) breakMinutes else workMinutes
                             millisRemaining = mins * 60 * 1000L
                             totalMillis = millisRemaining
                             scope.launch {
                                 repository.saveTimerState(
                                     isRunning = false,
                                     targetEpoch = 0L,
                                     mode = mode.name,
                                     remaining = millisRemaining
                                 )
                             }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("\u00E2 \u00AD", fontSize = 20.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Settings panel
            if (showSettings) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Timer Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Work (min)", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = workInput,
                                onValueChange = { workInput = it.filter { c -> c.isDigit() }.take(2) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = TextMuted,
                                    cursorColor = PrimaryPurple
                                ),
                                modifier = Modifier.width(90.dp)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Break (min)", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = breakInput,
                                onValueChange = { breakInput = it.filter { c -> c.isDigit() }.take(2) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = TextMuted,
                                    cursorColor = PrimaryPurple
                                ),
                                modifier = Modifier.width(90.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val w = workInput.toIntOrNull()?.coerceIn(1, 99) ?: 25
                                val b = breakInput.toIntOrNull()?.coerceIn(1, 30) ?: 5
                                workMinutes = w
                                breakMinutes = b
                                 stopTimer()
                                 val mins = if (mode == TimerMode.WORK) w else b
                                 millisRemaining = mins * 60 * 1000L
                                 totalMillis = millisRemaining
                                 scope.launch {
                                     repository.saveFocusTimerSettings(w, b)
                                     repository.saveTimerState(
                                         isRunning = false,
                                         targetEpoch = 0L,
                                         mode = mode.name,
                                         remaining = millisRemaining
                                     )
                                 }
                                showSettings = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Sessions info
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${workMinutes}", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("Work min", color = TextSecondary, fontSize = 12.sp)
                    }
                    Divider(
                        color = TextMuted,
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${breakMinutes}", color = CardWisdom, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("Break min", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
