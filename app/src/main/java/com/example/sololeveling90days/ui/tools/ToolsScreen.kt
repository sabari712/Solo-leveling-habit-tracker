package com.example.sololeveling90days.ui.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Science
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
import com.example.sololeveling90days.TdeeKey
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
    object NavigateTdee : ToolAction()
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
        ToolItem("⏱️", "Focus Timer", "Enhance cognitive focus with system timer cycles", AppleBlue, ToolAction.NavigateFocusTimer),
        ToolItem("🧘", "Meditation", "Calm neural pathways and regulate cardiovascular tension", ActionOrange, ToolAction.NavigateMeditation),
        ToolItem("🥗", "Calorie Tracker", "Estimate and log daily food intake metabolics", SuccessGreen, ToolAction.NavigateCalorieTracker),
        ToolItem("📖", "Book Summary", "S-Rank player knowledge library summaries", AppleBlue, ToolAction.NavigateMoodJournal),
        ToolItem("📝", "Mood Journal", "Record and journal current psychological states", ActionOrange, ToolAction.NavigateMoodJournal),
        ToolItem("🚿", "Cold Shower", "Log today's cardiorespiratory cold exposure challenge", SuccessGreen, ToolAction.ShowColdShowerDialog),
        ToolItem("📱", "Screen Time", "Measure and check visual screen mind pollution", HardRed, ToolAction.ShowScreenTimeDialog),
        ToolItem("🔥", "Adaptive TDEE", "Kalman-filter powered metabolic rate estimator", Color(0xFF7C3AED), ToolAction.NavigateTdee),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MINI TOOLS HUB",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
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
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Cognitive and metabolic enhancement inventory",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 20.dp)
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
                                is ToolAction.NavigateTdee -> onNavigate(TdeeKey)
                            }
                        },
                        badge = when {
                            tool.action is ToolAction.ShowColdShowerDialog && coldShowerDoneToday != null ->
                                if (coldShowerDoneToday == true) "✓ Completed" else "❌ Skipped"
                            tool.action is ToolAction.ShowScreenTimeDialog && screenTimeLogged != null ->
                                "${screenTimeLogged}m logged"
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
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "COLD SHOWER TRIAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Did you complete today's cold shower sensory trial?",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                coldShowerDoneToday = true
                                showColdShowerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("YES", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                coldShowerDoneToday = false
                                showColdShowerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HardRed),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("NO", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Screen Time Dialog
    if (showScreenTimeDialog) {
        Dialog(onDismissRequest = { showScreenTimeDialog = false }) {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "COGNITIVE PURIFIER",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Enter today's screen time minutes to evaluate mind pollution levels.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = screenTimeMinutes,
                        onValueChange = { screenTimeMinutes = it },
                        placeholder = { Text("Minutes...", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlue,
                            unfocusedBorderColor = AppleBlue.copy(alpha = 0.15f),
                            cursorColor = AppleBlue,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val mins = screenTimeMinutes.toIntOrNull()
                            if (mins != null) {
                                screenTimeLogged = mins
                            }
                            showScreenTimeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("COMMIT MINUTES", color = Color.Black, fontWeight = FontWeight.Bold)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tool.emoji, fontSize = 24.sp)
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .background(tool.accentColor.copy(alpha = 0.15f))
                            .border(1.dp, tool.accentColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = tool.accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column {
                Text(
                    text = tool.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = tool.description,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
