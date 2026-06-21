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

    val systemBackground = DarkBg

    Scaffold(
        containerColor = systemBackground,
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
                3 -> ProfileScreen(
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

    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val appleBlue = Color(0xFF007AFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today",
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = textPrimary
                )
                Text(
                    text = "${profile.streak} day streak",
                    color = textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Habit",
                    tint = appleBlue
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (activeQuests.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No habits for today.\nTap + to add one.",
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeQuests) { quest ->
                    HabitRow(
                        quest = quest,
                        onToggle = { done ->
                            scope.launch {
                                repository.toggleQuestCompletion(quest.id, done)
                                repository.completeDayIfAllDone()
                            }
                        },
                        onClick = {
                            onNavigate(QuestDetailKey(quest.id))
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(AdditionalTasksKey) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "\u2B50 Bonus Quests",
                                color = textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
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
    val appleBlue = Color(0xFF007AFF)

    val scale by animateFloatAsState(
        targetValue = if (quest.isCompleted) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "clickScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF2C2C2E))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (quest.isCompleted) appleBlue else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (quest.isCompleted) Color.Transparent else Color(0xFF38383A),
                        shape = CircleShape
                    )
                    .clickable { onToggle(!quest.isCompleted) },
                contentAlignment = Alignment.Center
            ) {
                if (quest.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = quest.title,
                    color = if (quest.isCompleted) textSecondary else textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (quest.description.isNotBlank()) {
                    Text(
                        text = quest.description,
                        color = textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(20.dp)
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
        NavItem("Jogging", Icons.Outlined.DirectionsRun),
        NavItem("Profile", Icons.Outlined.Person)
    )

    NavigationBar(
        containerColor = Color(0xFF1C1C1E),
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
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text     = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color(0xFF007AFF),
                    selectedTextColor   = Color(0xFF007AFF),
                    unselectedIconColor = Color(0xFF8E8E93),
                    unselectedTextColor = Color(0xFF8E8E93),
                    indicatorColor      = Color(0xFF007AFF).copy(alpha = 0.1f)
                )
            )
        }
    }
}
