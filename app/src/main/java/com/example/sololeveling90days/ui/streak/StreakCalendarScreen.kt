package com.example.sololeveling90days.ui.streak

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*
import java.time.LocalDate

// ---------------------------------------------------------------------------
// Streak calculation helpers
// ---------------------------------------------------------------------------

private fun calculateCurrentStreak(completedDates: List<String>): Int {
    if (completedDates.isEmpty()) return 0
    val sortedDates = completedDates
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .sortedDescending()
    var streak = 0
    var expected = LocalDate.now()
    for (date in sortedDates) {
        if (date == expected) {
            streak++
            expected = expected.minusDays(1)
        } else if (date == expected.minusDays(1)) {
            // Allow yesterday to still count (day not done yet today)
            streak++
            expected = date.minusDays(1)
        } else {
            break
        }
    }
    return streak
}

private fun calculateBestStreak(completedDates: List<String>): Int {
    if (completedDates.isEmpty()) return 0
    val sortedDates = completedDates
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .sorted()
    var best = 1
    var current = 1
    for (i in 1 until sortedDates.size) {
        if (sortedDates[i] == sortedDates[i - 1].plusDays(1)) {
            current++
            if (current > best) best = current
        } else {
            current = 1
        }
    }
    return best
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakCalendarScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())

    val completedDates = profile.completedDates
    val startDateStr = profile.startDate

    // Parse start date; fall back to today if missing
    val startDate = remember(startDateStr) {
        runCatching { LocalDate.parse(startDateStr) }.getOrNull() ?: LocalDate.now()
    }

    val currentStreak = remember(completedDates) { calculateCurrentStreak(completedDates) }
    val bestStreak = remember(completedDates) { calculateBestStreak(completedDates) }
    val totalCompleted = completedDates.size

    val today = LocalDate.now()
    val todayDayIndex = remember(startDate, today) {
        // 0-based index of today within the 90-day grid
        (today.toEpochDay() - startDate.toEpochDay()).toInt().coerceIn(0, 89)
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Streak Calendar",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Stats banner ───────────────────────────────────────────────
            StatsBanner(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                totalCompleted = totalCompleted
            )

            // ── Motivational message ───────────────────────────────────────
            MotivationalBanner(dayNumber = profile.dayNumber, totalCompleted = totalCompleted)

            // ── Legend ─────────────────────────────────────────────────────
            LegendRow()

            // ── 90-day heatmap grid ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, SteelGray.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "90-Day Journey",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Use a regular grid with fixed columns; wrap in a Box so
                    // LazyVerticalGrid doesn't conflict with the outer scroll.
                    val columns = 10
                    val rows = 9 // 10 x 9 = 90

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (col in 0 until columns) {
                                    val dayIndex = row * columns + col // 0-based
                                    if (dayIndex >= 90) {
                                        // Invisible filler
                                        Spacer(modifier = Modifier.weight(1f))
                                    } else {
                                        val dayDate = startDate.plusDays(dayIndex.toLong())
                                        val dateStr = dayDate.toString()
                                        val isCompleted = completedDates.contains(dateStr)
                                        val isToday = dayIndex == todayDayIndex
                                        val isFuture = dayDate.isAfter(today)

                                        DaySquare(
                                            dayNumber = dayIndex + 1,
                                            isCompleted = isCompleted,
                                            isToday = isToday,
                                            isFuture = isFuture,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Day labels ─────────────────────────────────────────────────
            DayLabelRow(startDate = startDate, completedDates = completedDates, today = today)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun StatsBanner(currentStreak: Int, bestStreak: Int, totalCompleted: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StreakStatCell(
            emoji = "\uD83D\uDD25",
            value = "$currentStreak",
            label = "Current Streak",
            accentColor = FireOrange,
            modifier = Modifier.weight(1f)
        )
        StreakStatCell(
            emoji = "\uD83C\uDFC6",
            value = "$bestStreak",
            label = "Best Streak",
            accentColor = XPGold,
            modifier = Modifier.weight(1f)
        )
        StreakStatCell(
            emoji = "\u2705",
            value = "$totalCompleted",
            label = "Days Completed",
            accentColor = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StreakStatCell(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, SteelGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Text(
                text = value,
                color = accentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MotivationalBanner(dayNumber: Int, totalCompleted: Int) {
    val message = when {
        totalCompleted == 0 -> "\uD83D\uDE80 Start your 90-day journey today!"
        totalCompleted >= 90 -> "\uD83C\uDF89 You've completed the 90-Day Challenge!"
        totalCompleted >= 70 -> "\uD83D\uDD25 Almost there! The finish line is close!"
        totalCompleted >= 45 -> "\uD83D\uDCAA Halfway hero! Keep going!"
        totalCompleted >= 15 -> "\u26A1 Building momentum \u2014 keep it up!"
        else -> "\uD83C\uDF1F Every day counts. Keep going!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GrowthEmerald.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, GrowthEmerald.copy(alpha = 0.4f))
    ) {
        Text(
            text = message,
            color = PrimaryPurpleLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun LegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = SuccessGreen, label = "Completed")
        LegendItem(color = DarkCardAlt, label = "Incomplete / Future", borderColor = TextMuted.copy(alpha = 0.3f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkCardAlt)
                    .border(2.dp, LevelUpGold, RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Today", color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, borderColor: Color = Color.Transparent) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(
                    if (borderColor != Color.Transparent)
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(3.dp))
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun DaySquare(
    dayNumber: Int,
    isCompleted: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isCompleted -> SuccessGreen
        isFuture    -> DarkCardAlt.copy(alpha = 0.5f)
        else        -> DarkCardAlt
    }
    val borderColor = when {
        isToday     -> LevelUpGold
        isCompleted -> SuccessGreenLight.copy(alpha = 0.5f)
        else        -> Color.Transparent
    }
    val borderWidth = if (isToday) 2.dp else if (isCompleted) 1.dp else 0.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .then(
                if (borderWidth > 0.dp)
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(4.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isToday || (dayNumber <= 9)) {
            Text(
                text = "$dayNumber",
                color = if (isCompleted) Color.White else if (isFuture) TextMuted.copy(alpha = 0.5f) else TextMuted,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else if (isCompleted) {
            Text(
                text = "\u2713",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayLabelRow(
    startDate: LocalDate,
    completedDates: List<String>,
    today: LocalDate
) {
    // Show recent completions summary
    val recentCompleted = completedDates
        .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
        .filter { !it.isAfter(today) }
        .sortedDescending()
        .take(3)

    if (recentCompleted.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
            border = BorderStroke(1.dp, SteelGray.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Completions",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                recentCompleted.forEachIndexed { idx, date ->
                    val dayIndex = (date.toEpochDay() - startDate.toEpochDay()).toInt() + 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Day $dayIndex",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = date.toString(),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
