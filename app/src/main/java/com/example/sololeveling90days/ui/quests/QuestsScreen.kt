package com.example.sololeveling90days.ui.quests

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.sololeveling90days.WorkoutLibraryKey
import com.example.sololeveling90days.ExerciseCameraKey
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun categoryColor(category: QuestCategory): Color = when (category) {
    QuestCategory.MORNING     -> Color(0xFFF59E0B)
    QuestCategory.FITNESS     -> Color(0xFFEA580C)
    QuestCategory.MINDSET     -> Color(0xFF7C3AED)
    QuestCategory.NUTRITION   -> Color(0xFF10B981)
    QuestCategory.LEARNING    -> Color(0xFF0891B2)
    QuestCategory.SLEEP       -> Color(0xFF6366F1)
    QuestCategory.PRODUCTIVITY -> Color(0xFFEC4899)
    QuestCategory.WELLNESS    -> Color(0xFF14B8A6)
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestsScreen(
    repository: AppRepository,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val quests by repository.quests.collectAsStateWithLifecycle(initialValue = emptyList())
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())

    val activeQuests = quests.filter { it.isActive }
    val completedCount = activeQuests.count { it.isCompleted }
    val totalCount = activeQuests.size
    val allDone = totalCount > 0 && completedCount == totalCount
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    // Add quest bottom sheet state
    var showAddSheet by remember { mutableStateOf(false) }
    // Day-complete snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QuestsTopBar(
                dayNumber = profile.dayNumber,
                onBack = onBack
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Browse Workouts Library FAB
                FloatingActionButton(
                    onClick = {
                        onNavigate(WorkoutLibraryKey)
                    },
                    containerColor = Color(0xFF7C3AED), // NeonPurple
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.FitnessCenter, contentDescription = "Browse Workouts")
                }

                // Add Custom Quest FAB
                FloatingActionButton(
                    onClick = {
                        showAddSheet = true
                    },
                    containerColor = GrowthEmerald,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Quest")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress section
            QuestsProgressSection(
                completed = completedCount,
                total = totalCount,
                animatedProgress = animatedProgress
            )

            // Complete Day celebration button
            AnimatedVisibility(
                visible = allDone,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CompleteDayButton(
                    onClick = {
                        scope.launch {
                            repository.completeDayIfAllDone()
                            snackbarHostState.showSnackbar(
                                "\uD83C\uDF89 Day ${profile.dayNumber} complete! +200 XP bonus!"
                            )
                        }
                    }
                )
            }

            // Quest list
            if (activeQuests.isEmpty()) {
                EmptyQuestsPlaceholder()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = activeQuests,
                        key = { it.id }
                    ) { quest ->
                        QuestItem(
                            quest = quest,
                            onToggle = { checked ->
                                scope.launch {
                                    repository.toggleQuestCompletion(quest.id, checked)
                                    if (checked) repository.awardXP(quest.xpReward)
                                    else repository.awardXP(-quest.xpReward)
                                }
                            },
                            onDelete = {
                                scope.launch { repository.removeQuest(quest.id) }
                            },
                            onVerify = {
                                quest.exerciseType?.let { type ->
                                    onNavigate(
                                        ExerciseCameraKey(
                                            exerciseTypeName = type.name,
                                            targetReps = quest.targetReps,
                                            questId = quest.id
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Quest Bottom Sheet
    if (showAddSheet) {
        AddQuestSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { quest ->
                scope.launch {
                    repository.addCustomQuest(quest)
                    showAddSheet = false
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Top Bar
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestsTopBar(dayNumber: Int, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Today's Quests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Day $dayNumber of 90",
                    fontSize = 13.sp,
                    color = PrimaryPurpleLight
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurface
        )
    )
}

// ---------------------------------------------------------------------------
// Progress Section
// ---------------------------------------------------------------------------

@Composable
private fun QuestsProgressSection(
    completed: Int,
    total: Int,
    animatedProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$completed / $total quests",
                fontSize = 13.sp,
                color = if (completed == total && total > 0) SuccessGreen else XPGold,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DarkCardAlt)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GrowthEmerald, LevelUpGold)
                        )
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Complete Day Button
// ---------------------------------------------------------------------------

@Composable
private fun CompleteDayButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessGreen.copy(alpha = glowAlpha)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Complete Day! \uD83C\uDF89",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quest Item
// ---------------------------------------------------------------------------

@Composable
private fun QuestItem(
    quest: Quest,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onVerify: (() -> Unit)? = null
) {
    val catColor = categoryColor(quest.category)
    val contentAlpha = if (quest.isCompleted) 0.55f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (quest.exerciseType != null && !quest.isCompleted && onVerify != null) {
                    onVerify()
                } else {
                    onToggle(!quest.isCompleted)
                }
            },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SteelGray.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category emoji circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quest.category.emoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Quest info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary.copy(alpha = contentAlpha),
                    textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1
                )
                if (quest.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quest.description,
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = contentAlpha),
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // XP badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = XPGold.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "\u26A1 ${quest.xpReward} XP",
                        color = XPGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: delete + verify + checkbox column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Delete icon
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete quest",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Verify with camera button
                if (quest.exerciseType != null && !quest.isCompleted && onVerify != null) {
                    IconButton(
                        onClick = onVerify,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Verify with camera",
                            tint = Color(0xFF4FC3F7), // NeonBlue
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Verified completion badge
                if (quest.exerciseType != null && quest.isCompleted && quest.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified completion",
                        tint = Color(0xFF00E676), // GoodGreen
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Completion checkbox
                Checkbox(
                    checked = quest.isCompleted,
                    onCheckedChange = { checked ->
                        if (quest.exerciseType != null && !quest.isCompleted && onVerify != null) {
                            onVerify()
                        } else {
                            onToggle(checked)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = GrowthEmerald,
                        uncheckedColor = TextMuted,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty State
// ---------------------------------------------------------------------------

@Composable
private fun EmptyQuestsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "\uD83D\uDDD2\u008F", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No quests yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap + to add your first daily quest",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Add Quest Bottom Sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuestSheet(
    onDismiss: () -> Unit,
    onAdd: (Quest) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(QuestCategory.MINDSET) }
    var xpText by remember { mutableStateOf("50") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TextMuted)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Add New Quest",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )

            HorizontalDivider(color = DarkCard)

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Quest Title *") },
                placeholder = { Text("e.g. Morning Run") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Title cannot be empty", color = HardRed) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrowthEmerald,
                    unfocusedBorderColor = DarkCard,
                    focusedLabelColor = GrowthEmerald,
                    cursorColor = GrowthEmerald,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("Short description of this quest") },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = DarkCard,
                    focusedLabelColor = PrimaryPurple,
                    cursorColor = PrimaryPurple,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it }
            ) {
                OutlinedTextField(
                    value = "${selectedCategory.emoji}  ${selectedCategory.label}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = DarkCard,
                        focusedLabelColor = PrimaryPurple,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.background(DarkCard)
                ) {
                    QuestCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = category.emoji,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = category.label,
                                        color = if (category == selectedCategory) PrimaryPurpleLight else TextPrimary,
                                        fontWeight = if (category == selectedCategory) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                selectedCategory = category
                                categoryExpanded = false
                            },
                            modifier = Modifier.background(
                                if (category == selectedCategory) PrimaryPurple.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                        )
                    }
                }
            }

            // XP amount
            OutlinedTextField(
                value = xpText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 4) xpText = input
                },
                label = { Text("XP Reward") },
                prefix = { Text("\u26A1 ", color = XPGold) },
                suffix = { Text(" XP", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryPurple,
                    unfocusedBorderColor = DarkCard,
                    focusedLabelColor = PrimaryPurple,
                    cursorColor = PrimaryPurple,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Add button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    val xp = xpText.toIntOrNull()?.coerceIn(1, 9999) ?: 50
                    onAdd(
                        Quest(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            description = description.trim(),
                            xpReward = xp,
                            category = selectedCategory
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrowthEmerald)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add Quest",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
