package com.example.sololeveling90days.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.*
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*
import com.example.sololeveling90days.ui.jogging.JoggingTrackerScreen
import com.example.sololeveling90days.ui.profile.ProfileScreen
import com.example.sololeveling90days.ui.quests.AddEditQuestBottomSheet
import com.example.sololeveling90days.ui.stats.StatsScreen
import com.example.sololeveling90days.ui.social.GuildScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(
    repository: AppRepository,
    onNavigate: (Any) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            HomeBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TodayTab(
                    repository = repository,
                    onNavigate = onNavigate,
                    onAddClick = { showAddSheet = true }
                )
                1 -> StatsScreen(repository = repository)
                2 -> JoggingTrackerScreen(repository = repository)
                3 -> GuildScreen(
                    repository = repository,
                    onNavigateToProfile = { selectedTab = 4 }
                )
                4 -> ProfileScreen(
                    repository = repository,
                    onBack = null,
                    onNavigate = onNavigate
                )
            }
        }
    }

    if (showAddSheet) {
        AddEditQuestBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { newQuest ->
                scope.launch {
                    repository.addCustomQuest(newQuest)
                    showAddSheet = false
                }
            }
        )
    }
}

@Composable
private fun TodayTab(
    repository: AppRepository,
    onNavigate: (Any) -> Unit,
    onAddClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val profile by repository.userProfile.collectAsStateWithLifecycle(UserProfile())
    val quests by repository.quests.collectAsStateWithLifecycle(emptyList())

    val activeQuests = remember(quests) { quests.filter { it.isActive } }

    // Level-up math
    val userLevel = remember(profile.totalXp) { levelFromXp(profile.totalXp) }
    val xpProgress = remember(profile.totalXp) { xpProgressInLevel(profile.totalXp) }

    // Rank system: dynamic E -> D -> C -> B -> A -> S based on XP
    val currentRank = remember(profile.totalXp) { rankFromXp(profile.totalXp) }
    val rankProgress = remember(profile.totalXp) { xpProgressInRank(profile.totalXp) }
    val nextRankXp = remember(profile.totalXp) { xpForNextRank(profile.totalXp) }
    val rankColor = remember(currentRank) {
        try { Color(android.graphics.Color.parseColor("#${currentRank.color}")) }
        catch (_: Exception) { Color(0xFF94A3B8) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOLO LEVELING: 90 DAYS",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = AppleBlue,
                        modifier = Modifier.graphicsLayer { rotationZ = -1f }
                    )
                    Text(
                        text = "System Workout Hub",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = ActionOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${profile.streak}",
                        color = ActionOrange,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Day ${profile.dayNumber}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hero Status Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Player Status",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(rankColor.copy(alpha = 0.1f))
                                        .border(1.dp, rankColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentRank.label.uppercase(),
                                        color = rankColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = "Shadow Monarch in Training",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        Text(
                            text = "LV. $userLevel",
                            color = AppleBlue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EXPERIENCE TRACKER",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile.totalXp} XP",
                            color = AppleBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Progress bar track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(AppleBlue.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(xpProgress)
                                .background(AppleBlue)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Rank progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RANK: ${currentRank.label.uppercase()}",
                            color = rankColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (nextRankXp > 0) "$nextRankXp XP to next rank" else "MAX RANK",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(AppleBlue.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(rankProgress)
                                .background(rankColor)
                        )
                    }
                }
            }
        }

        // Boss Day Banner
        if (isBossDay(profile.dayNumber)) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2E0808), Color(0xFF1C0A0A))
                            )
                        )
                        .border(1.5.dp, Color(0xFFDC2626), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚨", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "GATE OPENED: BOSS BATTLE",
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        Text(
                            text = "A Dimensional Gate has opened on Day ${profile.dayNumber}. The Legendary Boss must be defeated to progress. Failures will result in severe XP penalties.",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        if (profile.hardMode) {
                            Text(
                                text = "⚠️ HARD MODE ACTIVE: Miss will result in -300 XP penalty!",
                                color = Color(0xFFFF8A80),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Daily Quests Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = AppleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "DAILY SYSTEM QUESTS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "+ Add Quest",
                    color = AppleBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onAddClick() }
                )
            }
        }

        // Checklist of Quests
        if (activeQuests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No system quests active today.\nTap + Add Quest to establish one.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(activeQuests) { quest ->
                HabitRow(
                    quest = quest,
                    onToggle = { done ->
                        if (quest.exerciseType != null && !quest.isCompleted) {
                            onNavigate(
                                ExerciseCameraKey(
                                    exerciseTypeName = quest.exerciseType.name,
                                    targetReps = quest.targetReps,
                                    questId = quest.id
                                )
                            )
                        } else {
                            scope.launch {
                                repository.toggleQuestCompletion(quest.id, done)
                                repository.completeDayIfAllDone()
                            }
                        }
                    },
                    onClick = {
                        if (quest.exerciseType != null && !quest.isCompleted) {
                            onNavigate(
                                ExerciseCameraKey(
                                    exerciseTypeName = quest.exerciseType.name,
                                    targetReps = quest.targetReps,
                                    questId = quest.id
                                )
                            )
                        } else {
                            onNavigate(QuestDetailKey(quest.id))
                        }
                    }
                )
            }
        }

        // Quick Navigation Cards / Modules Grid
        item {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { onNavigate(WorkoutLibraryKey) },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "WORKOUTS",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .clickable { onNavigate(ToolsKey) },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = ActionOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "TOOLS HUB",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AdditionalTasksKey) },
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "⭐ BONUS QUESTS",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HabitRow(
    quest: Quest,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary

    val isBossQuest = quest.difficulty == QuestDifficulty.LEGENDARY && quest.id.startsWith("boss")
    val containerBg = if (isBossQuest) Color(0xFF241414) else DisciplineNavy
    val borderStroke = if (isBossQuest) BorderStroke(1.5.dp, Color(0xFFDC2626)) else BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (quest.isCompleted) AppleBlue else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (quest.isCompleted) Color.Transparent else AppleBlue.copy(alpha = 0.15f)
                    )
                    .clickable { onToggle(!quest.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (quest.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = quest.title,
                        color = if (quest.isCompleted) textSecondary else textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (quest.exerciseType != null) {
                        Spacer(Modifier.width(6.dp))
                        if (quest.isCompleted && quest.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Action",
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (!quest.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera Required",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (quest.description.isNotBlank()) {
                    Text(
                        text = quest.description,
                        color = textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HomeBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

    val items = listOf(
        NavItem("Today",   Icons.AutoMirrored.Outlined.List),
        NavItem("Stats",   Icons.Outlined.BarChart),
        NavItem("Activity", Icons.Outlined.DirectionsRun),
        NavItem("Guilds",  Icons.Outlined.Group),
        NavItem("Profile", Icons.Outlined.Person)
    )

    NavigationBar(
        containerColor = DarkBg,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selected == index,
                onClick  = { onSelect(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text     = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = AppleBlue,
                    selectedTextColor   = AppleBlue,
                    unselectedIconColor = Color(0xFF8E8E93),
                    unselectedTextColor = Color(0xFF8E8E93),
                    indicatorColor      = Color.Transparent
                )
            )
        }
    }
}
