package com.example.sololeveling90days.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.DarkBg
import com.example.sololeveling90days.theme.DisciplineNavy
import com.example.sololeveling90days.theme.TextPrimary
import com.example.sololeveling90days.theme.TextSecondary
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: AppRepository
) {
    val completionsMap by repository.questCompletions.collectAsStateWithLifecycle(emptyMap())
    val stepsList by repository.dailySteps.collectAsStateWithLifecycle(emptyList())

    val systemBackground = DarkBg
    val secondaryBackground = DisciplineNavy
    val appleBlue = Color(0xFF007AFF)
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary

    // Generate list of the last 7 days
    val last7Days = remember {
        (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
    }

    // 1. Habit Completion Rate per day
    val habitStats = remember(completionsMap, last7Days) {
        last7Days.map { day ->
            val dayStr = day.toString()
            var count = 0
            completionsMap.forEach { (_, dates) ->
                if (dates.contains(dayStr)) count++
            }
            count
        }
    }
    val maxHabits = remember(habitStats) { (habitStats.maxOrNull() ?: 1).coerceAtLeast(1) }

    // 2. Steps walked per day
    val stepStats = remember(stepsList, last7Days) {
        last7Days.map { day ->
            val dayStr = day.toString()
            val stepRecord = stepsList.firstOrNull { it.date == dayStr }
            stepRecord?.steps ?: 0
        }
    }
    val maxSteps = remember(stepStats) { (stepStats.maxOrNull() ?: 1).coerceAtLeast(1) }
    val todaySteps = stepStats.lastOrNull() ?: 0
    val averageSteps = remember(stepStats) { stepStats.average().toInt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(systemBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Stats",
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            color = textPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Steps Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = secondaryBackground)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Steps today",
                    color = textSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = String.format("%,d", todaySteps),
                    color = textPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Weekly average: ${String.format("%,d", averageSteps)} steps",
                    color = textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Step History Bar Chart
        Text(
            text = "Step History",
            color = textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(secondaryBackground)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            last7Days.forEachIndexed { idx, day ->
                val steps = stepStats[idx]
                val progress = steps.toFloat() / maxSteps.toFloat()
                val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase().first().toString()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(0.35f)
                            .fillMaxHeight(progress.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(appleBlue)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dayLabel,
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Habits Completion Rate Chart
        Text(
            text = "Habit Completions",
            color = textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(secondaryBackground)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            last7Days.forEachIndexed { idx, day ->
                val count = habitStats[idx]
                val progress = count.toFloat() / maxHabits.toFloat()
                val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase().first().toString()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(0.35f)
                            .fillMaxHeight(progress.coerceAtLeast(0.05f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(appleBlue)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = dayLabel,
                        color = textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(48.dp))
    }
}
