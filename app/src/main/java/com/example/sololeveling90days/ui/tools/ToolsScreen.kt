package com.example.sololeveling90days.ui.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.CalorieTrackerKey
import com.example.sololeveling90days.FocusTimerKey
import com.example.sololeveling90days.MeditationKey
import com.example.sololeveling90days.MoodJournalKey
import com.example.sololeveling90days.theme.*

data class ToolItem(
    val emoji: String,
    val name: String,
    val description: String,
    val accentColor: Color,
    val action: ToolAction
)

sealed class ToolAction {
    object NavigateFocusTimer : ToolAction()
    object NavigateMeditation : ToolAction()
    object NavigateCalorieTracker : ToolAction()
    object NavigateMoodJournal : ToolAction()
    object ShowColdShowerDialog : ToolAction()
    object ShowScreenTimeDialog : ToolAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    repository: AppRepository,
    onNavigate: (Any) -> Unit,
    onBack: () -> Unit
) {


    var showColdShowerDialog by remember { mutableStateOf(false) }
    var showScreenTimeDialog by remember { mutableStateOf(false) }
    var coldShowerDoneToday by remember { mutableStateOf<Boolean?>(null) }
    var screenTimeMinutes by remember { mutableStateOf("") }
    var screenTimeLogged by remember { mutableStateOf<Int?>(null) }

    val tools = listOf(
        ToolItem("\u23F1\uFE0F", "Focus Timer", "Pomodoro sessions to supercharge productivity", PrimaryPurple, ToolAction.NavigateFocusTimer),
        ToolItem("\uD83E\uDDD8", "Meditation", "Guided breathing to calm your mind", CardWisdom, ToolAction.NavigateMeditation),
        ToolItem("\uD83E\uDD57", "Calorie Tracker", "Estimate & log your daily food intake", CardFocus, ToolAction.NavigateCalorieTracker),
        ToolItem("\uD83D\uDCDA", "Book Summary", "Distilled wisdom from top self-help books", XPGold, ToolAction.NavigateMoodJournal),
        ToolItem("\uD83D\uDCD3", "Mood Journal", "Track emotions & reflect on your day", FireOrange, ToolAction.NavigateMoodJournal),
        ToolItem("\uD83D\uDEBF", "Cold Shower", "Log today's cold shower challenge", CardWisdom, ToolAction.ShowColdShowerDialog),
        ToolItem("\uD83D\uDCF1", "Screen Time", "Self-report your daily screen minutes", HardRedLight, ToolAction.ShowScreenTimeDialog),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mini Tools",
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
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Boost your reset journey",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(tools) { tool ->
                    ToolCard(
                        tool = tool,
                        onClick = {
                            when (tool.action) {
                                is ToolAction.NavigateFocusTimer -> onNavigate(FocusTimerKey)
                                is ToolAction.NavigateMeditation -> onNavigate(MeditationKey)
                                is ToolAction.NavigateCalorieTracker -> onNavigate(CalorieTrackerKey)
                                is ToolAction.NavigateMoodJournal -> onNavigate(MoodJournalKey)
                                is ToolAction.ShowColdShowerDialog -> showColdShowerDialog = true
                                is ToolAction.ShowScreenTimeDialog -> showScreenTimeDialog = true
                            }
                        },
                        badge = when {
                            tool.action is ToolAction.ShowColdShowerDialog && coldShowerDoneToday != null ->
                                if (coldShowerDoneToday == true) "\u2705 Done" else "\u274C Skipped"
                            tool.action is ToolAction.ShowScreenTimeDialog && screenTimeLogged != null ->
                                "${screenTimeLogged}m today"
                            else -> null
                        }
                    )
                }
            }
        }
    }

    // Cold Shower Dialog
    if (showColdShowerDialog) {
        Dialog(onDismissRequest = { showColdShowerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkCard,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("\uD83D\uDEBF", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Cold Shower Tracker",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Did you take a cold shower today?",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                coldShowerDoneToday = true
                                showColdShowerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("\u2705  Yes!", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                coldShowerDoneToday = false
                                showColdShowerDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HardRedLight),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("\u274C  No", color = HardRedLight, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (coldShowerDoneToday != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (coldShowerDoneToday == true) "Great job! Keep it up! \uD83D\uDCAA" else "Try again tomorrow!",
                            color = if (coldShowerDoneToday == true) SuccessGreen else TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Screen Time Dialog
    if (showScreenTimeDialog) {
        Dialog(onDismissRequest = { showScreenTimeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkCard,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("\uD83D\uDCF1", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Screen Time Log",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "How many minutes of recreational screen time today?",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    OutlinedTextField(
                        value = screenTimeMinutes,
                        onValueChange = { if (it.length <= 4) screenTimeMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = TextMuted,
                            cursorColor = PrimaryPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    val minutes = screenTimeMinutes.toIntOrNull() ?: 0
                    val assessment = when {
                        minutes == 0 -> ""
                        minutes <= 30 -> "\uD83C\uDF1F Excellent self-control!"
                        minutes <= 60 -> "\u2705 Within healthy limits"
                        minutes <= 120 -> "\u26A0\u008F Getting a bit high"
                        else -> "\uD83D\uDD34 Try to reduce tomorrow"
                    }
                    if (assessment.isNotEmpty()) {
                        Text(assessment, color = TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val m = screenTimeMinutes.toIntOrNull()
                            if (m != null) {
                                screenTimeLogged = m
                                showScreenTimeDialog = false
                            }
                        },
                        enabled = screenTimeMinutes.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Screen Time", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showScreenTimeDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        }
    }


}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    badge: String?
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            tool.accentColor.copy(alpha = 0.15f),
            DarkCard
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(18.dp))
            .background(gradientBrush)
            .border(
                width = 1.dp,
                color = tool.accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                Text(
                    text = tool.emoji,
                    fontSize = 38.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    text = tool.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tool.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tool.accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badge,
                        color = tool.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Top-right accent dot
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(8.dp)
                .clip(CircleShape)
                .background(tool.accentColor.copy(alpha = 0.6f))
        )
    }
}

