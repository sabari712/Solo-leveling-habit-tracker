package com.example.sololeveling90days.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.*
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: AppRepository,
    onBack: (() -> Unit)? = null,
    onNavigate: (Any) -> Unit = {}
) {
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())
    val collectedCards by repository.collectedCards.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessionStatus by repository.authRepository.sessionStatus.collectAsStateWithLifecycle(initialValue = io.github.jan.supabase.auth.status.SessionStatus.Initializing)
    val isGuestMode by repository.isGuestMode.collectAsStateWithLifecycle(initialValue = true)
    val isAuthenticated = sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated || repository.authRepository.isAuthenticated()
    val scope = rememberCoroutineScope()

    var showHardModeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Confirmation: enable Hard Mode
    if (showHardModeDialog) {
        HardModeConfirmDialog(
            onConfirm = {
                showHardModeDialog = false
                scope.launch { repository.setHardMode(true) }
            },
            onDismiss = { showHardModeDialog = false }
        )
    }

    // Confirmation: reset progress
    if (showResetDialog) {
        ResetConfirmDialog(
            onConfirm = {
                showResetDialog = false
                scope.launch { repository.resetApp() }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {
                        Text(
                            "Profile",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF007AFF))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBack == null) {
                Text(
                    text = "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            // ── 1. Avatar + name + title ───────────────────────────────────
            ProfileHeader(profile = profile)

            // ── 2. Stats row ───────────────────────────────────────────────
            StatsRow(
                totalDays = profile.dayNumber - 1,
                streak = profile.streak,
                totalXp = profile.totalXp,
                cardsCollected = collectedCards.size,
                onClickCards = {
                    onNavigate(com.example.sololeveling90days.CardsKey)
                }
            )

            // ── 3. 66-Day challenge progress ───────────────────────────────
            ChallengeProgressCard(dayNumber = profile.dayNumber)

            // ── 4. Hard Mode card ─────────────────────────────────────────
            HardModeCard(
                hardModeEnabled = profile.hardMode,
                onToggle = { enable ->
                    if (enable && !profile.hardMode) {
                        showHardModeDialog = true
                    } else if (!enable) {
                        scope.launch { repository.setHardMode(false) }
                    }
                }
            )

            // ── 4b. Cloud Sync / Account Card ─────────────────────────────────────
            AccountCard(
                isAuthenticated = isAuthenticated,
                email = remember(sessionStatus) { repository.authRepository.currentUserEmail() },
                onSignOut = {
                    scope.launch {
                        repository.authRepository.signOut()
                        repository.setGuestMode(false) // Trigger AuthScreen on signout
                    }
                },
                onLinkAccount = {
                    scope.launch {
                        repository.setGuestMode(false) // Trigger AuthScreen to link
                    }
                }
            )

            // ── 5. Settings section ────────────────────────────────────────
            SettingsSection(
                wakeTime = profile.wakeTime,
                onViewCalendar = { onNavigate(com.example.sololeveling90days.StreakCalendarKey) },
                onResetProgress = { showResetDialog = true }
            )

            // ── 6. App info ────────────────────────────────────────────────
            AppInfoCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Profile Header
// ---------------------------------------------------------------------------

@Composable
private fun ProfileHeader(profile: UserProfile) {
    val initials = profile.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }

    val avatarGradient = Brush.radialGradient(
        colors = listOf(Color(0xFF007AFF), Color(0xFF0055B3))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(avatarGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Name
            Text(
                text = profile.name.ifEmpty { "Unnamed Warrior" },
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Level badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF007AFF).copy(alpha = 0.25f))
                    .border(1.dp, Color(0xFF007AFF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "\u26A1 Level ${profile.level} Resetter",
                    color = Color(0xFF007AFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Stats Row
// ---------------------------------------------------------------------------

@Composable
private fun StatsRow(
    totalDays: Int,
    streak: Int,
    totalXp: Int,
    cardsCollected: Int,
    onClickCards: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCell(label = "Total Days", value = "$totalDays", icon = "\uD83D\uDCC5", modifier = Modifier.weight(1f))
        StatCell(label = "Streak", value = "$streak \uD83D\uDD25", icon = "\uD83D\uDD25", modifier = Modifier.weight(1f))
        StatCell(label = "Total XP", value = "$totalXp", icon = "\u26A1", modifier = Modifier.weight(1f))
        StatCell(
            label = "Cards",
            value = "$cardsCollected",
            icon = "\uD83C\uDCCF",
            modifier = Modifier
                .weight(1f)
                .clickable { onClickCards() }
        )
    }
}

@Composable
private fun StatCell(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 90-Day Challenge Progress
// ---------------------------------------------------------------------------

@Composable
private fun ChallengeProgressCard(dayNumber: Int) {
    val progress = (dayNumber - 1).coerceIn(0, 90) / 90f
    val milestones = listOf(15, 45, 90)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uD83C\uDFC6 90-Day Challenge",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Day ${(dayNumber - 1).coerceAtLeast(0)} of 90",
                    color = Color(0xFF007AFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(DarkCardAlt)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GrowthEmerald, LevelUpGold)
                            )
                        )
                )
            }

            // Milestone badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                milestones.forEach { milestone ->
                    val achieved = (dayNumber - 1) >= milestone
                    MilestoneBadge(
                        label = "Day $milestone",
                        achieved = achieved,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneBadge(label: String, achieved: Boolean, modifier: Modifier = Modifier) {
    val bg = if (achieved) XPGold.copy(alpha = 0.2f) else DarkCardAlt
    val border = if (achieved) XPGold.copy(alpha = 0.6f) else TextMuted.copy(alpha = 0.3f)
    val textColor = if (achieved) XPGold else TextMuted

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (achieved) "\uD83C\uDFC5" else "\uD83D\uDD12",
                fontSize = 16.sp
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Hard Mode Card
// ---------------------------------------------------------------------------

@Composable
private fun HardModeCard(hardModeEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    val borderColor = if (hardModeEnabled) HardRed.copy(alpha = 0.6f) else DarkCardAlt
    val bgColor = if (hardModeEnabled) HardRed.copy(alpha = 0.08f) else DarkCard

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "\uD83D\uDC80", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hard Mode",
                        color = if (hardModeEnabled) HardRedLight else TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hardModeEnabled) "\uD83D\uDD34 Active \u2014 No mercy mode engaged" else "XP penalties for missed days. No mercy mode.",
                    color = if (hardModeEnabled) HardRedLight.copy(alpha = 0.85f) else TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = hardModeEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = HardRed,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkCardAlt
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Settings Section
// ---------------------------------------------------------------------------

@Composable
private fun SettingsSection(
    wakeTime: String,
    onViewCalendar: () -> Unit,
    onResetProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Notification reminder time (display only)
            SettingsRow(
                icon = Icons.Filled.Notifications,
                iconTint = XPGold,
                label = "Reminder Time",
                value = wakeTime
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = DarkCardAlt
            )

            // View Streak Calendar
            SettingsButtonRow(
                icon = Icons.Filled.CalendarMonth,
                iconTint = Color(0xFF007AFF),
                label = "View Streak Calendar",
                onClick = onViewCalendar
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = DarkCardAlt
            )

            // Reset Progress
            SettingsButtonRow(
                icon = Icons.Filled.DeleteForever,
                iconTint = HardRedLight,
                label = "Reset Progress",
                labelColor = HardRedLight,
                onClick = onResetProgress
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun SettingsButtonRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    labelColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = labelColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// App Info
// ---------------------------------------------------------------------------

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Solo Leveling: 90 Days",
                color = Color(0xFF007AFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Version 1.0",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "Developed by Sabari",
                color = GrowthEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "com.example.sololeveling90days",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

@Composable
private fun HardModeConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = "\uD83D\uDC80 Enable Hard Mode?",
                color = HardRedLight,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Hard Mode applies XP penalties and resets your streak for missed days. There is no mercy. Are you sure?",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Enable", color = HardRedLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = "\u26A0\u008F Reset All Progress?",
                color = HardRedLight,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your data \u2014 streaks, XP, quests, and collected cards. This action cannot be undone.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset Everything", color = HardRedLight, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AccountCard(
    isAuthenticated: Boolean,
    email: String?,
    onSignOut: () -> Unit,
    onLinkAccount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        border = BorderStroke(1.dp, Color(0xFF38383A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Cloud, contentDescription = null, tint = Color(0xFF007AFF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAuthenticated) "Cloud Sync Active" else "Offline Guest Mode",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAuthenticated) "Logged in as: ${email ?: "Unknown"}" else "Data is stored locally. Sign in to sync across devices.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isAuthenticated) {
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = HardRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onLinkAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Link Account / Log In", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}




