package com.example.sololeveling90days.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Data model for each onboarding step
// ─────────────────────────────────────────────────────────────────────────────

private sealed class OnboardingStep {
    object Welcome : OnboardingStep()
    object Name : OnboardingStep()
    object Goal : OnboardingStep()
    object WakeTime : OnboardingStep()
}

private val stepOrder = listOf(
    OnboardingStep.Welcome,
    OnboardingStep.Name,
    OnboardingStep.Goal,
    OnboardingStep.WakeTime
)

private val goalOptions = listOf(
    "Build Fitness",
    "Fix Sleep",
    "Mental Clarity",
    "Career Growth",
    "Weight Loss",
    "Overall Discipline",
    "Reduce Anxiety",
    "Healthy Diet",
    "Time Management",
    "Break Bad Habits",
    "Financial Discipline",
    "Daily Meditation"
)

private val wakeTimeOptions = listOf(
    "5:00 AM",
    "6:00 AM",
    "7:00 AM",
    "8:00 AM"
)

// ─────────────────────────────────────────────────────────────────────────────
// Root composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    repository: AppRepository,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // State
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var userName by rememberSaveable { mutableStateOf("") }
    var selectedGoals by rememberSaveable { mutableStateOf(emptySet<String>()) }
    var selectedWakeTime by rememberSaveable { mutableStateOf("6:00 AM") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    // Forward = true, backward = false — used to choose slide direction
    var isMovingForward by rememberSaveable { mutableStateOf(true) }

    val isLastStep = currentStepIndex == stepOrder.lastIndex
    val isFirstStep = currentStepIndex == 0

    // Continue button enabled guard
    val continueEnabled = when (stepOrder[currentStepIndex]) {
        is OnboardingStep.Welcome -> true
        is OnboardingStep.Name -> userName.isNotBlank()
        is OnboardingStep.Goal -> selectedGoals.size >= 3
        is OnboardingStep.WakeTime -> selectedWakeTime.isNotEmpty()
    }

    fun advanceStep() {
        if (isLastStep) {
            isLoading = true
            scope.launch {
                repository.completeOnboarding(
                    name = userName.trim(),
                    goal = selectedGoals.joinToString(", "),
                    wakeTime = selectedWakeTime
                )
                onComplete()
            }
        } else {
            isMovingForward = true
            currentStepIndex++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Subtle radial glow behind content
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── Step progress dots ──────────────────────────────────────────
            if (!isFirstStep) {
                Spacer(modifier = Modifier.height(24.dp))
                StepProgressIndicator(
                    totalSteps = stepOrder.size - 1, // exclude welcome
                    currentStep = currentStepIndex - 1
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            // ── Animated step content ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStepIndex,
                    transitionSpec = {
                        if (isMovingForward) {
                            (slideInHorizontally(
                                animationSpec = tween(380, easing = EaseOutCubic),
                                initialOffsetX = { it }
                            ) + fadeIn(animationSpec = tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = EaseInCubic),
                                        targetOffsetX = { -it / 3 }
                                    ) + fadeOut(animationSpec = tween(200))
                                )
                        } else {
                            (slideInHorizontally(
                                animationSpec = tween(380, easing = EaseOutCubic),
                                initialOffsetX = { -it }
                            ) + fadeIn(animationSpec = tween(300)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(280, easing = EaseInCubic),
                                        targetOffsetX = { it / 3 }
                                    ) + fadeOut(animationSpec = tween(200))
                                )
                        }
                    },
                    label = "onboarding_step"
                ) { stepIdx ->
                    when (stepOrder[stepIdx]) {
                        is OnboardingStep.Welcome -> WelcomeStepContent()
                        is OnboardingStep.Name -> NameStepContent(
                            name = userName,
                            onNameChange = { userName = it },
                            onDone = { if (userName.isNotBlank()) advanceStep() }
                        )
                        is OnboardingStep.Goal -> GoalStepContent(
                            selectedGoals = selectedGoals,
                            onGoalToggled = { goal ->
                                selectedGoals = if (selectedGoals.contains(goal)) {
                                    selectedGoals - goal
                                } else {
                                    selectedGoals + goal
                                }
                            }
                        )
                        is OnboardingStep.WakeTime -> WakeTimeStepContent(
                            selectedTime = selectedWakeTime,
                            onTimeSelected = { selectedWakeTime = it }
                        )
                    }
                }
            }

            // ── Bottom action buttons ───────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                if (isFirstStep) {
                    // Big gradient CTA for welcome screen
                    GradientButton(
                        text = "Begin My Reset",
                        enabled = true,
                        isLoading = false,
                        onClick = { advanceStep() }
                    )
                } else {
                    GradientButton(
                        text = "Continue \u2192",
                        enabled = continueEnabled,
                        isLoading = isLoading,
                        onClick = { advanceStep() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Back link
                    TextButton(onClick = {
                        isMovingForward = false
                        currentStepIndex--
                    }) {
                        Text(
                            text = "\u2190 Back",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step progress indicator (dots)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepProgressIndicator(totalSteps: Int, currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPast = index < currentStep

            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 28.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot_width_$index"
            )
            val dotColor by animateColorAsState(
                targetValue = when {
                    isActive -> PrimaryPurple
                    isPast -> PrimaryPurpleLight.copy(alpha = 0.5f)
                    else -> DarkCard
                },
                animationSpec = tween(300),
                label = "dot_color_$index"
            )

            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dotColor)
            )
            if (index < totalSteps - 1) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 – Welcome
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeStepContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Glow orb behind logo text
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryPurple.copy(alpha = glowAlpha * 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Badge pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryPurple.copy(alpha = 0.2f))
                        .border(
                            1.dp,
                            PrimaryPurpleLight.copy(alpha = 0.4f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "\u2727  90 Day Challenge  \u2727",
                        color = PrimaryPurpleLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logo wordmark
                Text(
                    text = "SOLO",
                    color = TextPrimary,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    modifier = Modifier.scale(logoScale)
                )
                Text(
                    text = "LEVELING",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 8.sp,
                    style = LocalTextStyle.current.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryPurple, PrimaryPurpleLight)
                        )
                    ),
                    modifier = Modifier.scale(logoScale)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Decorative divider line
        Row(
            modifier = Modifier.fillMaxWidth(0.6f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PrimaryPurple.copy(alpha = 0.3f)
            )
            Text(
                text = "  \u25C6  ",
                color = PrimaryPurple.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PrimaryPurple.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tagline
        Text(
            text = "Build the discipline to become\nwho you're meant to be.",
            color = TextSecondary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBadge(value = "90", label = "Days")
            StatBadge(value = "\u221E", label = "Potential")
            StatBadge(value = "1", label = "Decision")
        }
    }
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = PrimaryPurpleLight,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun NameStepContent(
    name: String,
    onNameChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepLabel(step = "Step 1 of 3")
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "What should we\ncall you?",
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your name powers your journey.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Name input field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = "Enter your name",
                    color = TextMuted,
                    fontSize = 18.sp
                )
            },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onDone()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPurple,
                unfocusedBorderColor = DarkCard,
                cursorColor = PrimaryPurpleLight,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
            ),
            shape = RoundedCornerShape(14.dp)
        )

        if (name.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Text(
                    text = "Welcome, $name! \uD83D\uDC4A",
                    color = PrimaryPurpleLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ——————————————————————————————————————————————————————————————————————————————————————————————————
// Step 3 – Goal selection
// ——————————————————————————————————————————————————————————————————————————————————————————————————

@Composable
private fun GoalStepContent(
    selectedGoals: Set<String>,
    onGoalToggled: (String) -> Unit
) {
    val goalEmojis = mapOf(
        "Build Fitness" to "\uD83D\uDCAA",
        "Fix Sleep" to "\uD83C\uDF19",
        "Mental Clarity" to "\uD83E\uDDE0",
        "Career Growth" to "\uD83D\uDE80",
        "Weight Loss" to "\uD83D\uDD25",
        "Overall Discipline" to "\u2694\uFE0F",
        "Reduce Anxiety" to "\uD83E\uDDD8",
        "Healthy Diet" to "\uD83E\uDD57",
        "Time Management" to "\u23F1\uFE0F",
        "Break Bad Habits" to "\uD83D\uDEAB",
        "Financial Discipline" to "\uD83D\uDCB0",
        "Daily Meditation" to "\u2728"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        StepLabel(step = "Step 2 of 3")
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select your\ngoals",
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose at least 3 goals that drive your reset.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selected: ${selectedGoals.size} (Minimum 3)",
            color = if (selectedGoals.size >= 3) PrimaryPurpleLight else TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2-column grid of goal chips
        val chunked = goalOptions.chunked(2)
        chunked.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { goal ->
                    SelectableGoalChip(
                        label = goal,
                        emoji = goalEmojis[goal] ?: "\u2726",
                        isSelected = selectedGoals.contains(goal),
                        modifier = Modifier.weight(1f),
                        onClick = { onGoalToggled(goal) }
                    )
                }
                // Fill remaining slot if odd number
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (rowIdx < chunked.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SelectableGoalChip(
    label: String,
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else DarkCard,
        animationSpec = tween(200),
        label = "chip_border_$label"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.2f) else DarkSurface,
        animationSpec = tween(200),
        label = "chip_bg_$label"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale_$label"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(200),
        label = "chip_text_$label"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 4 – Wake time
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WakeTimeStepContent(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        StepLabel(step = "Step 3 of 3")
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "When do you\nwake up?",
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "We'll schedule your daily reset around this.",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Time chips
        wakeTimeOptions.forEach { time ->
            SelectableTimeChip(
                time = time,
                isSelected = selectedTime == time,
                onClick = { onTimeSelected(time) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Early risers win the day. \uD83C\uDF05",
            color = TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SelectableTimeChip(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple.copy(alpha = 0.22f) else DarkSurface,
        animationSpec = tween(220),
        label = "time_bg_$time"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryPurple else DarkCard,
        animationSpec = tween(220),
        label = "time_border_$time"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextSecondary,
        animationSpec = tween(220),
        label = "time_text_$time"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "time_scale_$time"
    )

    val timeEmoji = when (time) {
        "5:00 AM" -> "\uD83C\uDF19"
        "6:00 AM" -> "\uD83C\uDF05"
        "7:00 AM" -> "\u2600\u008F"
        "8:00 AM" -> "\uD83C\uDF24"
        else -> "\u23F0"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = timeEmoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = time,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryPurple, PrimaryPurpleLight)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2713",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, DarkCardAlt, RoundedCornerShape(50))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepLabel(step: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PrimaryPurple.copy(alpha = 0.15f))
            .border(1.dp, PrimaryPurple.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(
            text = step,
            color = PrimaryPurpleLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.45f,
        animationSpec = tween(200),
        label = "btn_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(PrimaryPurpleDark, PrimaryPurple, PrimaryPurpleLight)
                    )
                } else {
                    Brush.linearGradient(colors = listOf(DarkCard, DarkCardAlt))
                }
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

