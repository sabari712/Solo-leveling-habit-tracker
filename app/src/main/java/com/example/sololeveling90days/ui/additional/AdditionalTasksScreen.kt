package com.example.sololeveling90days.ui.additional

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AdditionalTask
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

// ─────────────────────────────────────────────────────────────
//  Emoji picker options — all as Unicode escapes for safe rendering
// ─────────────────────────────────────────────────────────────
private val EMOJI_OPTIONS = listOf(
    "\u2B50",           // ⭐
    "\uD83C\uDFAF",     // 🎯
    "\uD83D\uDCD6",     // 📖
    "\uD83C\uDFC3",     // 🏃
    "\uD83D\uDCAA",     // 💪
    "\uD83E\uDDD8",     // 🧘
    "\uD83C\uDFA8",     // 🎨
    "\uD83C\uDFB5",     // 🎵
    "\uD83C\uDF4E",     // 🍎
    "\uD83D\uDCA7",     // 💧
    "\uD83D\uDECC",     // 🛌
    "\u270D\uFE0F",     // ✍️
    "\uD83E\uDDE0",     // 🧠
    "\uD83D\uDD25",     // 🔥
    "\u26A1",           // ⚡
    "\uD83C\uDF1F",     // 🌟
    "\uD83C\uDFCB\uFE0F", // 🏋️
    "\uD83D\uDEB6",     // 🚶
    "\uD83D\uDEB4",     // 🚴
    "\uD83E\uDD38",     // 🤸
    "\uD83E\uDD57",     // 🥗
    "\uD83D\uDCDD",     // 📝
    "\uD83C\uDFA4",     // 🎤
    "\uD83C\uDFB8",     // 🎸
    "\uD83D\uDCF8",     // 📸
    "\uD83D\uDCBB",     // 💻
    "\uD83C\uDF3F",     // 🌿
    "\uD83D\uDE4F",     // 🙏
    "\uD83E\uDDF9",     // 🧹
    "\uD83D\uDEC1",     // 🛁
    "\uD83C\uDF05",     // 🌅
    "\uD83C\uDF19"      // 🌙
)

// ─────────────────────────────────────────────────────────────
//  AdditionalTasksScreen
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalTasksScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val tasks by repository.additionalTasks.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }

    val completedXp = tasks.filter { it.isCompleted }.sumOf { it.xpReward }
    val completedCount = tasks.count { it.isCompleted }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Bonus Quests",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "$completedCount done \u00B7 +$completedXp XP earned",
                            color = XPGold,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GrowthEmerald,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BonusXpCard(completedXp = completedXp, totalTasks = tasks.size, completedCount = completedCount)

            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                EmptyBonusState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            AdditionalTaskRow(
                                task = task,
                                onToggle = { completed ->
                                    scope.launch { repository.toggleAdditionalTask(task.id, completed) }
                                },
                                onDelete = {
                                    scope.launch { repository.deleteAdditionalTask(task.id) }
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, emoji, xp ->
                scope.launch {
                    repository.addAdditionalTask(
                        AdditionalTask(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            emoji = emoji,
                            xpReward = xp,
                            createdDate = LocalDate.now().toString()
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  BonusXpCard
// ─────────────────────────────────────────────────────────────
@Composable
private fun BonusXpCard(completedXp: Int, totalTasks: Int, completedCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A2E1A), Color(0xFF0D1B2A))
                )
            )
            .border(1.dp, GrowthEmerald.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bonus XP Pool", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("+$completedXp XP", color = XPGold, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("from $completedCount bonus tasks", color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$completedCount / $totalTasks", color = GrowthEmerald, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("DONE", color = GrowthEmerald.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 2.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Task Row
// ─────────────────────────────────────────────────────────────
@Composable
private fun AdditionalTaskRow(
    task: AdditionalTask,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "checkScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0D2010) else Color(0xFF131820)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) GrowthEmerald.copy(alpha = 0.5f) else Color(0xFF2A2F3A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (task.isCompleted) GrowthEmerald.copy(alpha = 0.15f) else Color(0xFF1E2530)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(task.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) TextMuted else TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "+${task.xpReward} XP",
                    color = if (task.isCompleted) GrowthEmerald else XPGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) GrowthEmerald else Color.Transparent)
                    .border(2.dp, if (task.isCompleted) GrowthEmerald else Color(0xFF3A4050), CircleShape)
                    .clickable { onToggle(!task.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Empty State
// ─────────────────────────────────────────────────────────────
@Composable
private fun EmptyBonusState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("\u2B50", fontSize = 64.sp)   // ⭐
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Bonus Quests Yet", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap + to add your own extra challenges.\nBonus tasks give you XP without affecting your streak \u2014 stack them up!",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Add Task Dialog
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, emoji: String, xp: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("\u2B50") }  // ⭐
    var xpValue by remember { mutableIntStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131820),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("New Bonus Quest", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Read 10 pages", color = TextMuted) },
                    label = { Text("Task title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthEmerald,
                        unfocusedBorderColor = Color(0xFF2A2F3A),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = GrowthEmerald
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("XP Reward", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 50, 75, 100).forEach { xp ->
                        FilterChip(
                            selected = xpValue == xp,
                            onClick = { xpValue = xp },
                            label = {
                                Text(
                                    "+$xp",
                                    color = if (xpValue == xp) Color.Black else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = XPGold,
                                containerColor = Color(0xFF1E2530)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = xpValue == xp,
                                selectedBorderColor = XPGold,
                                borderColor = Color(0xFF2A2F3A)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Pick an emoji", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0D0D0D))
                ) {
                    val grid = EMOJI_OPTIONS.chunked(8)
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        grid.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selectedEmoji == emoji) GrowthEmerald.copy(alpha = 0.25f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                if (selectedEmoji == emoji) BorderStroke(1.dp, GrowthEmerald)
                                                else BorderStroke(0.dp, Color.Transparent),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedEmoji = emoji },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), selectedEmoji, xpValue)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrowthEmerald,
                    disabledContainerColor = Color(0xFF1E2530)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Quest", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
