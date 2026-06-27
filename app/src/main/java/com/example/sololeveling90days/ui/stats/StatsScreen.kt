package com.example.sololeveling90days.ui.stats
import com.example.sololeveling90days.theme.*

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
import com.example.sololeveling90days.data.UserProfile
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.BorderStroke
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
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())

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

        // Attribute Radar Chart
        AttributeRadarChart(profile = profile)

        // Steps Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = secondaryBackground),
            border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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

@Composable
private fun AttributeRadarChart(profile: UserProfile) {
    val stats = listOf(
        "STR" to profile.str.toFloat(),
        "AGI" to profile.agi.toFloat(),
        "INT" to profile.int.toFloat(),
        "VIT" to profile.vit.toFloat(),
        "SEN" to profile.sen.toFloat()
    )

    val chartBlue = AppleBlue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, chartBlue.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PLAYER ATTRIBUTES",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val labelPaint = remember {
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 34f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                    }
                }

                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2.3f
                    val numPoints = 5
                    val angleStep = (2 * Math.PI / numPoints).toFloat()

                    // Draw concentric pentagons (grid web)
                    val numWebLevels = 4
                    for (level in 1..numWebLevels) {
                        val levelRadius = radius * (level.toFloat() / numWebLevels)
                        val path = androidx.compose.ui.graphics.Path()
                        for (i in 0 until numPoints) {
                            val angle = i * angleStep - (Math.PI / 2).toFloat()
                            val x = center.x + levelRadius * Math.cos(angle.toDouble()).toFloat()
                            val y = center.y + levelRadius * Math.sin(angle.toDouble()).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = chartBlue.copy(alpha = 0.12f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Draw axis lines from center to outer vertices
                    for (i in 0 until numPoints) {
                        val angle = i * angleStep - (Math.PI / 2).toFloat()
                        val x = center.x + radius * Math.cos(angle.toDouble()).toFloat()
                        val y = center.y + radius * Math.sin(angle.toDouble()).toFloat()
                        drawLine(
                            color = chartBlue.copy(alpha = 0.2f),
                            start = center,
                            end = androidx.compose.ui.geometry.Offset(x, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Draw stat label text
                        val label = stats[i].first
                        val labelRadius = radius + 20.dp.toPx()
                        val lx = center.x + labelRadius * Math.cos(angle.toDouble()).toFloat()
                        val ly = center.y + labelRadius * Math.sin(angle.toDouble()).toFloat() + 4.dp.toPx()
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            lx,
                            ly,
                            labelPaint
                        )
                    }

                    // Draw player attribute filled polygon
                    val playerPath = androidx.compose.ui.graphics.Path()
                    val maxVal = 50f // baseline max reference
                    for (i in 0 until numPoints) {
                        val statVal = stats[i].second.coerceIn(0f, maxVal)
                        val statRadius = radius * (statVal / maxVal)
                        val angle = i * angleStep - (Math.PI / 2).toFloat()
                        val x = center.x + statRadius * Math.cos(angle.toDouble()).toFloat()
                        val y = center.y + statRadius * Math.sin(angle.toDouble()).toFloat()
                        if (i == 0) playerPath.moveTo(x, y) else playerPath.lineTo(x, y)
                    }
                    playerPath.close()

                    // Fill polygon with glowing gradient
                    drawPath(
                        path = playerPath,
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(chartBlue.copy(alpha = 0.45f), Color(0xFF7C3AED).copy(alpha = 0.25f)),
                            center = center
                        )
                    )

                    // Draw neon outline border
                    drawPath(
                        path = playerPath,
                        color = chartBlue,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    // Draw little glowing points at the vertices
                    for (i in 0 until numPoints) {
                        val statVal = stats[i].second.coerceIn(0f, maxVal)
                        val statRadius = radius * (statVal / maxVal)
                        val angle = i * angleStep - (Math.PI / 2).toFloat()
                        val x = center.x + statRadius * Math.cos(angle.toDouble()).toFloat()
                        val y = center.y + statRadius * Math.sin(angle.toDouble()).toFloat()
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = chartBlue,
                            radius = 5.5.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

