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
import kotlinx.coroutines.delay

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
    val shadowList by repository.shadowArmy.collectAsStateWithLifecycle(initialValue = emptyList())
    val isAuthenticated = sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated || repository.authRepository.isAuthenticated()
    val scope = rememberCoroutineScope()

    var showHardModeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showPhysiologyDialog by remember { mutableStateOf(false) }
    var showShopDialog by remember { mutableStateOf(false) }
    var showShadowCitadelDialog by remember { mutableStateOf(false) }
    var showTitlesDialog by remember { mutableStateOf(false) }
    var activeExtractionShadow by remember { mutableStateOf<ShadowHunter?>(null) }

    if (showAvatarDialog) {
        AvatarPickerDialog(
            currentAvatarId = profile.avatarResId,
            onSelect = { avatarId ->
                showAvatarDialog = false
                scope.launch { repository.updateAvatar(avatarId) }
            },
            onDismiss = { showAvatarDialog = false }
        )
    }

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

    if (showShopDialog) {
        SystemShopDialog(
            profile = profile,
            repository = repository,
            onDismiss = { showShopDialog = false }
        )
    }

    if (showShadowCitadelDialog) {
        ShadowCitadelDialog(
            shadowList = shadowList,
            profile = profile,
            repository = repository,
            onExtractClick = { shadow ->
                showShadowCitadelDialog = false
                activeExtractionShadow = shadow
            },
            onDismiss = { showShadowCitadelDialog = false }
        )
    }

    if (activeExtractionShadow != null) {
        ShadowExtractionDialog(
            shadow = activeExtractionShadow!!,
            onSuccess = {
                scope.launch {
                    repository.extractShadow(activeExtractionShadow!!.id)
                }
            },
            onDismiss = { activeExtractionShadow = null }
        )
    }

    if (showTitlesDialog) {
        TitlesDialog(
            profile = profile,
            repository = repository,
            onDismiss = { showTitlesDialog = false }
        )
    }

    if (showPhysiologyDialog) {
        PhysiologicalDetailsDialog(
            profile = profile,
            repository = repository,
            onDismiss = { showPhysiologyDialog = false }
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
            ProfileHeader(profile = profile, onAvatarClick = { showAvatarDialog = true })

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

            // ── 2b. Attribute Allocator ───────────────────────────────────
            AttributeAllocatorCard(
                profile = profile,
                onAllocate = { key ->
                    scope.launch { repository.allocateStatPoint(key) }
                }
            )

            // ── Phase 2: RPG System Expansions ─────────────────────────────
            SystemStoreInventoryCard(profile = profile, onClick = { showShopDialog = true })
            ShadowCitadelCard(shadowList = shadowList, onClick = { showShadowCitadelDialog = true })
            TitlesCard(profile = profile, onClick = { showTitlesDialog = true })
            PhysiologicalProfileCard(profile = profile, onClick = { showPhysiologyDialog = true })

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
private fun ProfileHeader(profile: UserProfile, onAvatarClick: () -> Unit) {
    val selectedAvatar = remember(profile.avatarResId) {
        AVATAR_OPTIONS.firstOrNull { it.id == profile.avatarResId } ?: AVATAR_OPTIONS[0]
    }

    val avatarGradient = remember(selectedAvatar) {
        Brush.radialGradient(
            colors = listOf(Color(selectedAvatar.startColor), Color(selectedAvatar.endColor))
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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
                    .background(avatarGradient)
                    .clickable { onAvatarClick() }
                    .border(2.dp, AppleBlue.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedAvatar.emoji,
                    fontSize = 36.sp,
                    textAlign = TextAlign.Center
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

            if (profile.activeTitle.isNotEmpty()) {
                Text(
                    text = "« ${profile.activeTitle.uppercase()} »",
                    color = Color(0xFF7C3AED),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

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
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
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

@Composable
private fun AttributeAllocatorCard(
    profile: UserProfile,
    onAllocate: (androidx.datastore.preferences.core.Preferences.Key<Int>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Attributes",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (profile.unallocatedPoints > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(0.dp))
                            .background(XPGold.copy(alpha = 0.2f))
                            .border(1.dp, XPGold, RoundedCornerShape(0.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${profile.unallocatedPoints} Stat Points Available",
                            color = XPGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Level up to gain stat points",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            val stats = listOf(
                Triple("STR", "Strength (Squats)", Triple(profile.str, PrefsKeys.STR_STAT, profile.strUnlockProgress)),
                Triple("AGI", "Agility (Running)", Triple(profile.agi, PrefsKeys.AGI_STAT, profile.agiUnlockProgress)),
                Triple("INT", "Intelligence (Focus)", Triple(profile.int, PrefsKeys.INT_STAT, profile.intUnlockProgress)),
                Triple("VIT", "Vitality (Level Bonus)", Triple(profile.vit, PrefsKeys.VIT_STAT, "")),
                Triple("SEN", "Sense (Mindset)", Triple(profile.sen, PrefsKeys.SEN_STAT, profile.senUnlockProgress))
            )

            stats.forEach { (abbr, label, data) ->
                val (value, key, progressMsg) = data
                val isLocked = progressMsg.isNotEmpty()
                val capValue = when (key) {
                    PrefsKeys.STR_STAT -> profile.strCap
                    PrefsKeys.AGI_STAT -> profile.agiCap
                    PrefsKeys.INT_STAT -> profile.intCap
                    PrefsKeys.VIT_STAT -> profile.vitCap
                    PrefsKeys.SEN_STAT -> profile.senCap
                    else -> 99
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = abbr,
                                color = if (isLocked) TextSecondary.copy(alpha = 0.5f) else AppleBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = label,
                                color = if (isLocked) TextSecondary.copy(alpha = 0.6f) else TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$value / $capValue",
                                color = if (isLocked) TextSecondary.copy(alpha = 0.8f) else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (profile.unallocatedPoints > 0) {
                                if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = "Locked",
                                        tint = TextSecondary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    IconButton(
                                        onClick = { onAllocate(key) },
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(AppleBlue.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, AppleBlue, CircleShape)
                                    ) {
                                        Text("+", color = AppleBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }

                    if (isLocked) {
                        Text(
                            text = "🔒 $progressMsg",
                            color = XPGold.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 42.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPickerDialog(
    currentAvatarId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Hunter Class",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(AVATAR_OPTIONS.size) { index ->
                        val avatar = AVATAR_OPTIONS[index]
                        val isSelected = avatar.id == currentAvatarId
                        val grad = Brush.radialGradient(listOf(Color(avatar.startColor), Color(avatar.endColor)))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable { onSelect(avatar.id) },
                            shape = RoundedCornerShape(0.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) AppleBlue else Color.White.copy(alpha = 0.1f)
                            ),
                            colors = CardDefaults.cardColors(containerColor = DisciplineNavy)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(grad),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(avatar.emoji, fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = avatar.name,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = avatar.description,
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppleBlue)
            }
        },
        containerColor = DarkBg,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun SystemStoreInventoryCard(profile: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎒", fontSize = 18.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("SYSTEM STORE & INVENTORY", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Gold Balance: ${profile.gold} G  |  ${profile.inventory.size} unique items", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ShadowCitadelCard(shadowList: List<ShadowHunter>, onClick: () -> Unit) {
    val extractedCount = remember(shadowList) { shadowList.count { it.isExtracted } }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFF7C3AED).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👥", fontSize = 18.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("SHADOW CITADEL", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Shadows Extracted: $extractedCount / ${shadowList.size}", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TitlesCard(profile: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF59E0B).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 18.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("HONORARY TITLES & ACHIEVEMENTS", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (profile.activeTitle.isNotEmpty()) "Active Title: « ${profile.activeTitle} »" else "No Title Equipped",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SystemShopDialog(
    profile: UserProfile,
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("SYSTEM SHOP", color = TextPrimary, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("YOUR GOLD:", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${profile.gold} G", color = Color(0xFFF59E0B), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(SYSTEM_SHOP_ITEMS.size) { index ->
                        val item = SYSTEM_SHOP_ITEMS[index]
                        val quantityOwned = profile.inventory.getOrDefault(item.id, 0)
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                            border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.emoji, fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Text(item.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${item.cost} G", color = Color(0xFFF59E0B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.description, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Owned: $quantityOwned", color = TextSecondary, fontSize = 11.sp)
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.buyShopItem(item)
                                            }
                                        },
                                        enabled = profile.gold >= item.cost,
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppleBlue,
                                            disabledContainerColor = AppleBlue.copy(alpha = 0.2f)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Buy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppleBlue)
            }
        },
        containerColor = DarkBg,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun ShadowCitadelDialog(
    shadowList: List<ShadowHunter>,
    profile: UserProfile,
    repository: AppRepository,
    onExtractClick: (ShadowHunter) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("SHADOW CITADEL", color = TextPrimary, fontWeight = FontWeight.Black)
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.height(350.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(shadowList.size) { index ->
                    val shadow = shadowList[index]
                    val isAssigned = shadow.assignedStat != null

                    // Determine extraction eligibility
                    val canExtract = when (shadow.id) {
                        "shadow_igris" -> profile.squatsCount >= 100
                        "shadow_iron" -> profile.level >= 3
                        "shadow_tusk" -> profile.meditationSessionsCount >= 3
                        "shadow_beru" -> profile.focusSessionsCount >= 5
                        else -> false
                    }

                    val unlockRequirement = when (shadow.id) {
                        "shadow_igris" -> "Complete 100 squats to awaken (Current: ${profile.squatsCount})"
                        "shadow_iron" -> "Reach Hunter Level 3 to awaken"
                        "shadow_tusk" -> "Complete 3 Meditation sessions to awaken (Current: ${profile.meditationSessionsCount})"
                        "shadow_beru" -> "Complete 5 Focus sessions to awaken (Current: ${profile.focusSessionsCount})"
                        else -> ""
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                        border = BorderStroke(1.dp, if (shadow.isExtracted) Color(0xFF7C3AED).copy(alpha = 0.4f) else AppleBlue.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(shadow.emoji, fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(shadow.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Text(shadow.rank, color = Color(0xFF7C3AED), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (shadow.isExtracted) Color(0xFF7C3AED).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (shadow.isExtracted) "EXTRACTED" else "SEALED",
                                        color = if (shadow.isExtracted) Color(0xFF7C3AED) else TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(shadow.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                            
                            Spacer(Modifier.height(12.dp))
                            if (!shadow.isExtracted) {
                                if (canExtract) {
                                    Button(
                                        onClick = { onExtractClick(shadow) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Extract Shadow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text(
                                        text = "🔒 $unlockRequirement",
                                        color = Color.Red.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Assign stat block
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isAssigned) "Assigned: ${shadow.assignedStat}" else "Unassigned",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    
                                    val assignedStatKey = when (shadow.id) {
                                        "shadow_igris" -> "STR"
                                        "shadow_iron" -> "VIT"
                                        "shadow_tusk" -> "SEN"
                                        "shadow_beru" -> "INT"
                                        else -> null
                                    }
                                    
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.assignShadowToStat(shadow.id, if (isAssigned) null else assignedStatKey)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isAssigned) Color.Red else AppleBlue),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(if (isAssigned) "Unassign" else "Assign Buff", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppleBlue)
            }
        },
        containerColor = DarkBg,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
fun ShadowExtractionDialog(
    shadow: ShadowHunter,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0 = Intro, 1 = QTE, 2 = Success, 3 = Fail
    var progress by remember { mutableStateOf(0f) }
    var isGrowing by remember { mutableStateOf(true) }

    LaunchedEffect(step) {
        if (step == 1) {
            while (step == 1) {
                if (isGrowing) {
                    progress += 0.04f
                    if (progress >= 1f) {
                        progress = 1f
                        isGrowing = false
                    }
                } else {
                    progress -= 0.04f
                    if (progress <= 0f) {
                        progress = 0f
                        isGrowing = true
                    }
                }
                delay(30)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DisciplineNavy,
        tonalElevation = 8.dp,
        modifier = Modifier.border(BorderStroke(1.dp, AppleBlue.copy(alpha = 0.3f)), RoundedCornerShape(0.dp)),
        confirmButton = {},
        dismissButton = {},
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (step == 0) {
                    Text(
                        "SURGE OF MANA DETECTED",
                        fontWeight = FontWeight.Black,
                        color = Color.Red,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "The shadow of [${shadow.name}] has appeared from the void. Extract its mana to bind it to your army.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            step = 1
                            SoundManager.playLockoutAlert()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Begin Extraction", fontWeight = FontWeight.Bold)
                    }
                } else if (step == 1) {
                    Text(
                        "SYNCHRONIZE MANA",
                        fontWeight = FontWeight.Black,
                        color = AppleBlue,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .border(BorderStroke(4.dp, AppleBlue.copy(alpha = 0.3f)), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(BorderStroke(6.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)), CircleShape)
                        )
                        val activeSize = 40.dp + (120.dp * progress)
                        Box(
                            modifier = Modifier
                                .size(activeSize)
                                .border(BorderStroke(3.dp, Color.White), CircleShape)
                        )

                        Text(
                            text = shadow.emoji,
                            fontSize = 32.sp
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Tap 'ARISE' when the outer ring matches the purple target zone!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (progress in 0.5f..0.8f) {
                                step = 2
                                SoundManager.playQuestDone()
                                SoundManager.speak("Arise.")
                                onSuccess()
                            } else {
                                step = 3
                                SoundManager.playLockoutAlert()
                                SoundManager.speak("Mana extraction failed.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("ARISE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                } else if (step == 2) {
                    Text(
                        "SHADOW EXTRACTED",
                        fontWeight = FontWeight.Black,
                        color = AppleBlue,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "${shadow.emoji} [${shadow.name}] has sworn allegiance to the Shadow Monarch and joined your army.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Close Portal", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "EXTRACTION FAILED",
                        fontWeight = FontWeight.Black,
                        color = Color.Red,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "The shadow's mana dissipated. Strengthen your stats and try again.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { step = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Try Again")
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = DisciplineNavy),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun TitlesDialog(
    profile: UserProfile,
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("HONORARY TITLES", color = TextPrimary, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Select an active title to display on your profile and dashboard. Complete achievement triggers to unlock them.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                val defaultTitles = listOf(
                    "One Who Overcame Adversity" to "Complete any daily quest under 15% phone battery charge. Buff: +15% XP gain.",
                    "Demon Slayer" to "Complete 10 study timer focus cycles. Buff: Study session awards +20% XP.",
                    "Wolf Slayer" to "Log a total of 15.0 km of jogging distance. Buff: Reduces jogging distance caps by 10%.",
                    "Shadow Monarch" to "Extract all four legendary shadows. Buff: +10% to all attribute capacity caps."
                )

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(defaultTitles.size) { index ->
                        val (titleName, desc) = defaultTitles[index]
                        val isUnlocked = profile.unlockedTitles.contains(titleName)
                        val isActive = profile.activeTitle == titleName

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(0.dp),
                            colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                            border = BorderStroke(
                                1.dp,
                                if (isActive) Color(0xFF7C3AED) else if (isUnlocked) AppleBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = titleName,
                                        color = if (isUnlocked) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isUnlocked) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    repository.selectTitle(if (isActive) "" else titleName)
                                                }
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color.Red else AppleBlue
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text(if (isActive) "Unequip" else "Equip", fontSize = 10.sp)
                                        }
                                    } else {
                                        Text("🔒 Locked", color = Color.Red.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AppleBlue)
            }
        },
        containerColor = DarkBg,
        shape = RoundedCornerShape(0.dp)
    )
}

@Composable
private fun PhysiologicalProfileCard(profile: UserProfile, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
        border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📐", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("HUNTER OPTIMIZATION MATRIX", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Goal: ${profile.fitnessGoal}  |  BMI: " + String.format(java.util.Locale.US, "%.1f", profile.bmi),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = AppleBlue, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppleBlue.copy(alpha = 0.15f))
            Spacer(Modifier.height(16.dp))

            val trackName = when (profile.fitnessGoal) {
                "Weight Loss" -> "Shadow Assassin (Cut)"
                "Muscle Gain" -> "Iron Berserker (Bulk)"
                else -> "Zen Monarch (Balanced)"
            }

            val macroBreakdown = when (profile.fitnessGoal) {
                "Weight Loss" -> "Macros: 40% Protein | 30% Carbs | 30% Fats"
                "Muscle Gain" -> "Macros: 30% Protein | 50% Carbs | 20% Fats"
                else -> "Macros: 35% Protein | 40% Carbs | 25% Fats"
            }

            Text("Class Path: $trackName", color = Color(0xFF7C3AED), fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(4.dp))
            Text("Daily Target: ${profile.targetCalories} kcal  |  Water: " + String.format(java.util.Locale.US, "%.1f", profile.dailyWaterTargetLiters) + " L", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(macroBreakdown, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhysiologicalDetailsDialog(
    profile: UserProfile,
    repository: AppRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedGender by remember { mutableStateOf(profile.gender) }
    var weightText by remember { mutableStateOf(profile.weightKg.toString()) }
    var heightText by remember { mutableStateOf(profile.heightCm.toString()) }
    var ageText by remember { mutableStateOf(profile.ageYears.toString()) }
    var selectedGoal by remember { mutableStateOf(profile.fitnessGoal) }
    var goalMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("PHYSIOLOGICAL MATRIX", color = TextPrimary, fontWeight = FontWeight.Black)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Configure your physiological specs to calibrate BMR, BMI, target calories, and macro nutrition plans.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Column {
                    Text("GENDER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male", "Female").forEach { g ->
                            val isSelected = selectedGender.lowercase(java.util.Locale.US) == g.lowercase(java.util.Locale.US)
                            Button(
                                onClick = { selectedGender = g },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) AppleBlue else DisciplineNavy
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, if (isSelected) AppleBlue else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(g, fontSize = 12.sp, color = if (isSelected) Color.White else TextPrimary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        cursorColor = AppleBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height (cm)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        cursorColor = AppleBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Age (years)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleBlue,
                        cursorColor = AppleBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("FITNESS GOAL", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = goalMenuExpanded,
                            onExpandedChange = { goalMenuExpanded = !goalMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedGoal,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalMenuExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppleBlue,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = goalMenuExpanded,
                                onDismissRequest = { goalMenuExpanded = false }
                            ) {
                                listOf("Weight Loss", "Muscle Gain", "Maintenance").forEach { goal ->
                                    DropdownMenuItem(
                                        text = { Text(goal, color = TextPrimary) },
                                        onClick = {
                                            selectedGoal = goal
                                            goalMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val weight = weightText.toFloatOrNull() ?: profile.weightKg
                        val height = heightText.toFloatOrNull() ?: profile.heightCm
                        val age = ageText.toIntOrNull() ?: profile.ageYears
                        scope.launch {
                            repository.updatePersonalDetails(
                                gender = selectedGender,
                                weightKg = weight,
                                heightCm = height,
                                ageYears = age,
                                fitnessGoal = selectedGoal
                            )
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Calibrate")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        },
        containerColor = DarkBg,
        shape = RoundedCornerShape(0.dp)
    )
}





