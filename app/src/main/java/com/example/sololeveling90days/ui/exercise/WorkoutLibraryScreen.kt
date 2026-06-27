package com.example.sololeveling90days.ui.exercise
import com.example.sololeveling90days.theme.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.*

// Theme colors
private val DarkBg = Color(0xFF0A0A0F)
private val CardBg = Color(0xFF1A1A2E)
private val NeonBlue = Color(0xFF4FC3F7)
private val NeonPurple = Color(0xFF7C3AED)
private val AccentGold = Color(0xFFF59E0B)
private val GoodGreen = Color(0xFF00E676)
private val SurfaceDark = Color(0xFF12121A)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLibraryScreen(
    onAddToQuests: (ExerciseDefinition, Int) -> Unit,
    onStartExercise: (ExerciseType, Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedMuscleGroup by remember { mutableStateOf<MuscleGroup?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedExercise by remember { mutableStateOf<ExerciseType?>(null) }
    var showRepsDialog by remember { mutableStateOf<ExerciseDefinition?>(null) }
    var dialogMode by remember { mutableStateOf<DialogMode>(DialogMode.ADD) }

    val filteredExercises = remember(selectedMuscleGroup, searchQuery) {
        EXERCISE_LIBRARY.filter { exercise ->
            val matchesGroup = selectedMuscleGroup == null || exercise.muscleGroup == selectedMuscleGroup
            val matchesSearch = searchQuery.isBlank() ||
                    exercise.type.label.contains(searchQuery, ignoreCase = true) ||
                    exercise.muscleGroup.label.contains(searchQuery, ignoreCase = true)
            matchesGroup && matchesSearch
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Workout Library",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "${EXERCISE_LIBRARY.size} exercises available",
                            fontSize = 12.sp,
                            color = NeonBlue
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search exercises...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonBlue)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Muscle group filter chips
            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip
                    item {
                        FilterChip(
                            selected = selectedMuscleGroup == null,
                            onClick = { selectedMuscleGroup = null },
                            label = { Text("All") },
                            leadingIcon = {
                                Text("\uD83D\uDD25", fontSize = 14.sp)  // 🔥
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                                containerColor = CardBg,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = NeonPurple,
                                enabled = true,
                                selected = selectedMuscleGroup == null
                            )
                        )
                    }
                    items(MuscleGroup.entries.toList()) { group ->
                        FilterChip(
                            selected = selectedMuscleGroup == group,
                            onClick = {
                                selectedMuscleGroup = if (selectedMuscleGroup == group) null else group
                            },
                            label = { Text(group.label) },
                            leadingIcon = {
                                Text(group.emoji, fontSize = 14.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple.copy(alpha = 0.3f),
                                selectedLabelColor = Color.White,
                                containerColor = CardBg,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = NeonPurple,
                                enabled = true,
                                selected = selectedMuscleGroup == group
                            )
                        )
                    }
                }
            }

            // Results count
            item {
                Text(
                    text = "${filteredExercises.size} exercise${if (filteredExercises.size != 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Exercise cards
            items(filteredExercises, key = { it.type.name }) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    isExpanded = expandedExercise == exercise.type,
                    onToggleExpand = {
                        expandedExercise = if (expandedExercise == exercise.type) null else exercise.type
                    },
                    onAddToQuests = {
                        showRepsDialog = exercise
                        dialogMode = DialogMode.ADD
                    },
                    onStartNow = {
                        showRepsDialog = exercise
                        dialogMode = DialogMode.START
                    }
                )
            }

            // Empty state
            if (filteredExercises.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "\uD83D\uDD0D",  // 🔍
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No exercises found",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Target reps dialog
    showRepsDialog?.let { exercise ->
        TargetRepsDialog(
            exercise = exercise,
            onConfirm = { targetReps ->
                when (dialogMode) {
                    DialogMode.ADD -> onAddToQuests(exercise, targetReps)
                    DialogMode.START -> onStartExercise(exercise.type, targetReps)
                }
                showRepsDialog = null
            },
            onDismiss = { showRepsDialog = null }
        )
    }
}

private enum class DialogMode { ADD, START }

// ---------------------------------------------------------------------------
// Exercise Card
// ---------------------------------------------------------------------------

@Composable
private fun ExerciseCard(
    exercise: ExerciseDefinition,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddToQuests: () -> Unit,
    onStartNow: () -> Unit
) {
    val difficultyColor = when (exercise.difficulty) {
        QuestDifficulty.NORMAL -> NeonBlue
        QuestDifficulty.HARD -> AccentGold
        QuestDifficulty.LEGENDARY -> Color(0xFFDC2626)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = CardBg,
        shape = RoundedCornerShape(16.dp),
        onClick = onToggleExpand
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Exercise emoji in circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    NeonPurple.copy(alpha = 0.3f),
                                    NeonBlue.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(exercise.type.emoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = exercise.type.label,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Difficulty badge
                        Surface(
                            color = difficultyColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = exercise.difficulty.label,
                                color = difficultyColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Muscle group
                        Text(
                            text = "${exercise.muscleGroup.emoji} ${exercise.muscleGroup.label}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        // Default reps
                        val unit = if (exercise.trackingMethod == TrackingMethod.TIMED_HOLD) "sec" else "reps"
                        Text(
                            text = "${exercise.defaultTargetReps} $unit",
                            color = NeonBlue.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        // XP
                        Text(
                            text = "+${exercise.xpReward} XP",
                            color = AccentGold.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Expand arrow
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = Color.White.copy(alpha = 0.5f)
                )
            }

            // Expanded details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Instructions
                    Text(
                        text = exercise.instructions,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Camera position info
                    val cameraText = if (exercise.cameraPosition == CameraPosition.FRONT)
                        "\uD83D\uDCF7 Place phone facing you (front camera)"  // 📷
                    else
                        "\uD83D\uDCF7 Place phone to your side (back camera)"  // 📷
                    Text(
                        text = cameraText,
                        color = NeonBlue.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    // Tracking method info
                    val trackingText = if (exercise.trackingMethod == TrackingMethod.TIMED_HOLD)
                        "\u23F1\uFE0F Timed hold - maintain correct form"  // ⏱️
                    else
                        "\uD83D\uDD04 Rep counting - camera counts each repetition"  // 🔄
                    Text(
                        text = trackingText,
                        color = NeonPurple.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Add to quests
                        OutlinedButton(
                            onClick = onAddToQuests,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, NeonPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add to Quests", color = NeonPurple, fontSize = 13.sp)
                        }

                        // Start now
                        Button(
                            onClick = onStartNow,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Now", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Target Reps Dialog
// ---------------------------------------------------------------------------

@Composable
private fun TargetRepsDialog(
    exercise: ExerciseDefinition,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isTimedHold = exercise.trackingMethod == TrackingMethod.TIMED_HOLD
    val unit = if (isTimedHold) "seconds" else "reps"
    var targetText by remember { mutableStateOf(exercise.defaultTargetReps.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.8f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(exercise.type.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(exercise.type.label, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Set target $unit:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { newVal ->
                        if (newVal.all { it.isDigit() } && newVal.length <= 4) {
                            targetText = newVal
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text(unit, color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = NeonBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick-pick chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val quickPicks = if (isTimedHold) listOf(15, 30, 45, 60) else listOf(5, 10, 15, 20)
                    quickPicks.forEach { value ->
                        SuggestionChip(
                            onClick = { targetText = value.toString() },
                            label = { Text("$value") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (targetText == value.toString())
                                    NeonPurple.copy(alpha = 0.3f) else SurfaceDark,
                                labelColor = Color.White
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                borderColor = if (targetText == value.toString())
                                    NeonPurple else Color.Transparent,
                                enabled = true
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toIntOrNull() ?: exercise.defaultTargetReps
                    onConfirm(target.coerceIn(1, 9999))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
