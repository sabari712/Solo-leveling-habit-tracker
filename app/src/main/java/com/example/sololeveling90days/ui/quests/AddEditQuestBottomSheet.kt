package com.example.sololeveling90days.ui.quests
import com.example.sololeveling90days.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditQuestBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Quest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(QuestCategory.FITNESS) }
    var selectedDifficulty by remember { mutableStateOf(QuestDifficulty.NORMAL) }
    
    // Exercise Linkage States
    var selectedExerciseType by remember { mutableStateOf<ExerciseType?>(null) }
    var targetReps by remember { mutableIntStateOf(10) }

    // Frequency variables
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(daysOfWeek.toSet()) } // Default to all days (Daily)

    // Inline Time Picker states
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    // Apple HIG style styling
    val appleBlue = Color(0xFF007AFF)
    val secondaryBackground = DisciplineNavy
    val textPrimary = Color(0xFFE5E2E1)
    val textSecondary = Color(0xFFC4C6CC)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DisciplineNavy,
        contentColor = textPrimary,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "New Habit",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Habit Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Name", color = textSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = appleBlue,
                    unfocusedBorderColor = AppleBlue.copy(alpha = 0.15f),
                    focusedLabelColor = appleBlue,
                    unfocusedLabelColor = textSecondary,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Category Section
            Text(
                text = "Category",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(QuestCategory.values()) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) appleBlue else secondaryBackground)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(category.emoji, fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = category.label,
                                color = if (isSelected) Color.White else textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (selectedCategory == QuestCategory.FITNESS) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Link to Exercise for Camera Tracking",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ExerciseType.entries) { exercise ->
                        val isSelected = exercise == selectedExerciseType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) appleBlue else secondaryBackground)
                                .clickable {
                                    selectedExerciseType = exercise
                                    if (title.isBlank() || ExerciseType.entries.any { it.label == title }) {
                                        title = exercise.label
                                    }
                                    val def = getExerciseDefinition(exercise)
                                    targetReps = def.defaultTargetReps
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${exercise.emoji} ${exercise.label}",
                                color = if (isSelected) Color.White else textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (selectedExerciseType != null) {
                    Spacer(Modifier.height(16.dp))
                    val definition = getExerciseDefinition(selectedExerciseType!!)
                    val unit = if (definition.trackingMethod == TrackingMethod.TIMED_HOLD) "seconds" else "reps"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target ($unit)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = textSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { if (targetReps > 1) targetReps -= 1 }) {
                                Text("-", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = targetReps.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { targetReps += 1 }) {
                                Text("+", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Difficulty Section
            Text(
                text = "Difficulty / Multiplier",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuestDifficulty.values().forEach { diff ->
                    val isSelected = diff == selectedDifficulty
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) appleBlue else secondaryBackground)
                            .clickable { selectedDifficulty = diff }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${diff.emoji} ${diff.label}",
                            color = if (isSelected) Color.White else textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Frequency Selection
            Text(
                text = "Frequency",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { day ->
                    val isSelected = selectedDays.contains(day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) appleBlue else secondaryBackground)
                            .clickable {
                                selectedDays = if (isSelected) {
                                    if (selectedDays.size > 1) selectedDays - day else selectedDays
                                } else {
                                    selectedDays + day
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.first().toString(),
                            color = if (isSelected) Color.White else textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Reminder Time Inline Picker
            Text(
                text = "Reminder Time",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(secondaryBackground)
                    .padding(vertical = 12.dp)
            ) {
                // Hour Selector
                IconButton(onClick = { hour = if (hour > 1) hour - 1 else 12 }) {
                    Text("-", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format("%02d", hour),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                IconButton(onClick = { hour = if (hour < 12) hour + 1 else 1 }) {
                    Text("+", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Text(":", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary, modifier = Modifier.padding(horizontal = 8.dp))

                // Minute Selector
                IconButton(onClick = { minute = if (minute >= 15) minute - 15 else 45 }) {
                    Text("-", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = String.format("%02d", minute),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                IconButton(onClick = { minute = if (minute <= 30) minute + 15 else 0 }) {
                    Text("+", color = appleBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(16.dp))

                // AM / PM Selector
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAm) appleBlue else Color.DarkGray)
                        .clickable { isAm = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("AM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAm) appleBlue else Color.DarkGray)
                        .clickable { isAm = false }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("PM", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Save / Add Habit Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val baseXP = when (selectedDifficulty) {
                            QuestDifficulty.NORMAL -> 100
                            QuestDifficulty.HARD -> 150
                            QuestDifficulty.LEGENDARY -> 300
                        }
                        val formattedTime = String.format("%02d:%02d %s", hour, minute, if (isAm) "AM" else "PM")
                        val newQuest = Quest(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = if (selectedCategory == QuestCategory.FITNESS && selectedExerciseType != null) {
                                val def = getExerciseDefinition(selectedExerciseType!!)
                                val unit = if (def.trackingMethod == TrackingMethod.TIMED_HOLD) "seconds" else "reps"
                                "$targetReps $unit - ${def.instructions}"
                            } else {
                                "Reminder: $formattedTime | Frequency: ${if (selectedDays.size == 7) "Daily" else selectedDays.joinToString(", ")}"
                            },
                            xpReward = baseXP,
                            category = selectedCategory,
                            difficulty = selectedDifficulty,
                            isCompleted = false,
                            isActive = true,
                            exerciseType = selectedExerciseType,
                            targetReps = if (selectedCategory == QuestCategory.FITNESS && selectedExerciseType != null) targetReps else 0
                        )
                        onSave(newQuest)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appleBlue,
                    contentColor = Color.White,
                    disabledContainerColor = AppleBlue.copy(alpha = 0.15f),
                    disabledContentColor = textSecondary.copy(alpha = 0.5f)
                )
            ) {
                Text("Add Habit", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
