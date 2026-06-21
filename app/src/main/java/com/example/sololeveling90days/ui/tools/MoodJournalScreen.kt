package com.example.sololeveling90days.ui.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.MoodJournalEntry
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch


data class MoodOption(
    val emoji: String,
    val label: String,
    val color: Color,
    val score: Int
)

val MOOD_OPTIONS: List<MoodOption>
    @Composable
    get() = listOf(
        MoodOption("\uD83D\uDE22", "Awful", HardRedLight, 1),
        MoodOption("\uD83D\uDE15", "Bad", FireOrange, 2),
        MoodOption("\uD83D\uDE10", "Okay", XPGold, 3),
        MoodOption("\uD83D\uDE42", "Good", CardFocus, 4),
        MoodOption("\uD83D\uDE04", "Great", SuccessGreen, 5),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodJournalScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val savedEntryState by repository.moodEntry.collectAsStateWithLifecycle(initialValue = null)

    var selectedMoodScore by rememberSaveable { mutableStateOf<Int?>(null) }
    var journalText by rememberSaveable { mutableStateOf("") }
    var showSavedMessage by rememberSaveable { mutableStateOf(false) }
    var hasBeenModified by rememberSaveable { mutableStateOf(false) }

    val selectedMood = MOOD_OPTIONS.find { it.score == selectedMoodScore }

    // Sync state when loaded
    LaunchedEffect(savedEntryState) {
        if (!hasBeenModified) {
            if (savedEntryState != null) {
                selectedMoodScore = savedEntryState!!.score
                journalText = savedEntryState!!.text
                showSavedMessage = true
            } else {
                selectedMoodScore = null
                journalText = ""
                showSavedMessage = false
            }
        }
    }

    val currentMoodColor = selectedMood?.color ?: TextMuted

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mood Journal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            Text(
                "How are you feeling today?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Select your mood and journal your thoughts",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            // Large selected mood display
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedMood != null)
                            Brush.radialGradient(
                                colors = listOf(
                                    selectedMood!!.color.copy(alpha = 0.25f),
                                    DarkCard
                                )
                            )
                        else
                            Brush.radialGradient(colors = listOf(DarkCard, DarkCard))
                    )
                    .border(
                        2.dp,
                        currentMoodColor.copy(alpha = if (selectedMood != null) 0.6f else 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedMood != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(selectedMood!!.emoji, fontSize = 44.sp)
                        Text(
                            selectedMood!!.label,
                            color = selectedMood!!.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\uD83E\uDD14", fontSize = 40.sp)
                        Text("Pick mood", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Mood selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MOOD_OPTIONS.forEach { mood ->
                    val isSelected = selectedMood == mood
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) mood.color else Color.Transparent,
                        animationSpec = tween(200),
                        label = "moodBorder"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) mood.color.copy(alpha = 0.15f) else DarkCard,
                        animationSpec = tween(200),
                        label = "moodBg"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable { selectedMoodScore = mood.score; hasBeenModified = true }
                            .padding(horizontal = 10.dp, vertical = 12.dp)
                    ) {
                        Text(mood.emoji, fontSize = 30.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            mood.label,
                            color = if (isSelected) mood.color else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Mood score bar
            if (selectedMood != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mood Score", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                "${selectedMood!!.score}/5",
                                color = selectedMood!!.color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { selectedMood!!.score / 5f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = selectedMood!!.color,
                            trackColor = DarkBg
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Journal text field
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "What's on your mind today?",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = journalText,
                        onValueChange = { journalText = it; hasBeenModified = true },
                        placeholder = {
                            Text(
                                "Write your thoughts, wins, struggles, gratitude\u2026",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        minLines = 5,
                        maxLines = 10,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = currentMoodColor,
                            unfocusedBorderColor = TextMuted,
                            cursorColor = currentMoodColor,
                            focusedContainerColor = DarkBg,
                            unfocusedContainerColor = DarkBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${journalText.length} characters",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save button
            Button(
                onClick = {
                    if (selectedMood != null) {
                        scope.launch {
                            repository.saveMoodEntry(
                                MoodJournalEntry(
                                    emoji = selectedMood!!.emoji,
                                    label = selectedMood!!.label,
                                    score = selectedMood!!.score,
                                    text = journalText
                                )
                            )
                            hasBeenModified = false
                        }
                    }
                },
                enabled = selectedMood != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = selectedMood?.color ?: PrimaryPurple,
                    disabledContainerColor = TextMuted
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "\uD83D\uDCBE  Save Entry",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Saved entry display
            if (showSavedMessage && selectedMood != null) {
                Spacer(Modifier.height(24.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(selectedMood!!.color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedMood!!.emoji, fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Today's Entry Saved \u2705",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Feeling ${selectedMood!!.label} \u00B7 Score ${selectedMood!!.score}/5",
                                    color = selectedMood!!.color,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        if (journalText.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Divider(color = TextMuted.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                journalText,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
