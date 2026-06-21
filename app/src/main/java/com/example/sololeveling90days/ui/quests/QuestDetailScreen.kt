package com.example.sololeveling90days.ui.quests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.sololeveling90days.data.Quest
import com.example.sololeveling90days.theme.DarkBg
import com.example.sololeveling90days.theme.DisciplineNavy
import com.example.sololeveling90days.theme.TextPrimary
import com.example.sololeveling90days.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestDetailScreen(
    repository: AppRepository,
    questId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val quests by repository.quests.collectAsStateWithLifecycle(emptyList())
    val completionsMap by repository.questCompletions.collectAsStateWithLifecycle(emptyMap())

    val quest = remember(quests, questId) { quests.firstOrNull { it.id == questId } }
    val completions = remember(completionsMap, questId) { completionsMap[questId] ?: emptyList() }

    // Color tokens matching Apple HIG system colors in dark mode
    val systemBackground = DarkBg
    val secondaryBackground = DisciplineNavy
    val appleBlue = Color(0xFF007AFF)
    val label = TextPrimary
    val secondaryLabel = TextSecondary

    // Calculate streaks
    val streakData = remember(completions) { calculateStreaks(completions) }
    val currentStreak = streakData.first
    val longestStreak = streakData.second

    if (quest == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(systemBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Habit not found", color = label)
        }
        return
    }

    Scaffold(
        containerColor = systemBackground,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = quest.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = label
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = appleBlue
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                repository.removeQuest(quest.id)
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Habit",
                            tint = Color(0xFFFF453A) // System Red
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = systemBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Subtitle metadata
            Text(
                text = "${quest.category.label} • ${quest.difficulty.label} Quest",
                color = secondaryLabel,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(24.dp))

            // Stats grid row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = secondaryBackground),
                    border = BorderStroke(1.dp, Color(0xFF38383A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Current Streak",
                            color = secondaryLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$currentStreak day${if (currentStreak == 1) "" else "s"}",
                            color = label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Longest Streak Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = secondaryBackground),
                    border = BorderStroke(1.dp, Color(0xFF38383A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Longest Streak",
                            color = secondaryLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$longestStreak day${if (longestStreak == 1) "" else "s"}",
                            color = label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Section Header
            Text(
                text = "History",
                color = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Completion records over the last 30 days",
                color = secondaryLabel,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Heatmap calendar grid
            val daysList = remember {
                (0..29).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(daysList.size) { index ->
                    val day = daysList[index]
                    val dateStr = day.toString()
                    val isCompleted = completions.contains(dateStr)

                    val containerColor = if (isCompleted) appleBlue else Color(0xFF1C1C1E)
                    val contentColor = if (isCompleted) Color.White else label

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) Color.White.copy(alpha = 0.8f) else secondaryLabel
                            )
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // Quick Complete button
            val isTodayCompleted = remember(completions) {
                completions.contains(LocalDate.now().toString())
            }

            Button(
                onClick = {
                    scope.launch {
                        repository.toggleQuestCompletion(quest.id, !isTodayCompleted)
                        repository.completeDayIfAllDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTodayCompleted) Color(0xFF2C2C2E) else appleBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isTodayCompleted) "Completed" else "Mark as done",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun calculateStreaks(dates: List<String>): Pair<Int, Int> {
    if (dates.isEmpty()) return Pair(0, 0)
    val parsedDates = try {
        dates.map { LocalDate.parse(it) }.distinct().sorted()
    } catch (e: Exception) {
        return Pair(0, 0)
    }

    var currentStreak = 0
    var longestStreak = 0
    var tempStreak = 0
    var prevDate: LocalDate? = null

    // Compute longest streak
    for (date in parsedDates) {
        if (prevDate == null) {
            tempStreak = 1
        } else {
            if (date.minusDays(1) == prevDate) {
                tempStreak++
            } else if (date != prevDate) {
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
                tempStreak = 1
            }
        }
        prevDate = date
    }
    if (tempStreak > longestStreak) {
        longestStreak = tempStreak
    }

    // Compute current streak
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)
    
    if (parsedDates.contains(today) || parsedDates.contains(yesterday)) {
        var checkDate = if (parsedDates.contains(today)) today else yesterday
        while (parsedDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }
    }

    return Pair(currentStreak, maxOf(longestStreak, currentStreak))
}
