package com.example.sololeveling90days.ui.social

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuildScreen(
    repository: AppRepository,
    onNavigateToProfile: () -> Unit
) {
    val authRepository = repository.authRepository
    val socialRepository = repository.socialRepository
    val scope = rememberCoroutineScope()
    
    val isAuthenticated = authRepository.isAuthenticated()
    
    var guild by remember { mutableStateOf<Guild?>(null) }
    var members by remember { mutableStateOf<List<GuildLeaderboardEntry>>(emptyList()) }
    var messages by remember { mutableStateOf<List<GuildMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Members, 1 = Leaderboard, 2 = Chat
    
    // Create/Join states
    var isCreateMode by remember { mutableStateOf(false) }
    var guildName by remember { mutableStateOf("") }
    var guildDesc by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var actionLoading by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    
    // Chat state
    var chatMessageText by remember { mutableStateOf("") }
    var isSendingMsg by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // Clipboard for copying code
    val clipboardManager = LocalClipboardManager.current

    // Auto-refresh loop for Chat and Leaderboard when in guild
    LaunchedEffect(guild, activeTab) {
        if (guild != null) {
            val currentGuildId = guild!!.id
            while (true) {
                try {
                    if (activeTab == 2) {
                        val newMessages = socialRepository.getGuildMessages(currentGuildId)
                        if (newMessages.size != messages.size) {
                            messages = newMessages
                            // Scroll to bottom on new message
                            if (newMessages.isNotEmpty()) {
                                scope.launch {
                                    lazyListState.animateScrollToItem(newMessages.lastIndex)
                                }
                            }
                        }
                    } else {
                        members = socialRepository.getGuildLeaderboard(currentGuildId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(4000) // Refresh every 4 seconds
            }
        }
    }

    // Initial load
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            try {
                isLoading = true
                val fetchedGuild = socialRepository.getCurrentGuild()
                guild = fetchedGuild
                if (fetchedGuild != null) {
                    members = socialRepository.getGuildLeaderboard(fetchedGuild.id)
                    messages = socialRepository.getGuildMessages(fetchedGuild.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (!isAuthenticated) "Guild Portal" else guild?.name ?: "Hunters Guild",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    if (guild != null) {
                        IconButton(onClick = {
                            scope.launch {
                                isLoading = true
                                val fetchedGuild = socialRepository.getCurrentGuild()
                                guild = fetchedGuild
                                if (fetchedGuild != null) {
                                    members = socialRepository.getGuildLeaderboard(fetchedGuild.id)
                                    messages = socialRepository.getGuildMessages(fetchedGuild.id)
                                }
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = AppleBlue,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (!isAuthenticated) {
                // Locked screen for guest mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Guild Feature Locked",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Connect with other hunters, join private guilds, share daily accountability, and compete on the weekly leaderboard by registering an account.",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNavigateToProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Log In or Register",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else if (guild == null) {
                // Join/Create options screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Group,
                            contentDescription = null,
                            tint = AppleBlue,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isCreateMode) "Found a Guild" else "Join a Guild",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Text(
                        text = if (isCreateMode) "Assemble your ultimate raid team to grind habits together." else "Grind habits alongside other hunters for maximum synergy.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isCreateMode) {
                                // Create Form
                                OutlinedTextField(
                                    value = guildName,
                                    onValueChange = { guildName = it; actionError = null },
                                    label = { Text("Guild Name") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleBlue,
                                        cursorColor = AppleBlue,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = AppleBlue
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = guildDesc,
                                    onValueChange = { guildDesc = it },
                                    label = { Text("Guild Description") },
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleBlue,
                                        cursorColor = AppleBlue,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = AppleBlue
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // Join Form
                                OutlinedTextField(
                                    value = inviteCode,
                                    onValueChange = { inviteCode = it.uppercase(); actionError = null },
                                    label = { Text("6-Character Invite Code") },
                                    placeholder = { Text("e.g. G2H4AX") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppleBlue,
                                        cursorColor = AppleBlue,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = AppleBlue
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (actionError != null) {
                                Text(
                                    text = actionError!!,
                                    color = HardRed,
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    if (isCreateMode) {
                                        if (guildName.isBlank()) {
                                            actionError = "Guild name cannot be blank."
                                            return@Button
                                        }
                                        actionLoading = true
                                        scope.launch {
                                            val created = socialRepository.createGuild(guildName, guildDesc)
                                            if (created != null) {
                                                guild = created
                                                members = socialRepository.getGuildLeaderboard(created.id)
                                                messages = socialRepository.getGuildMessages(created.id)
                                            } else {
                                                actionError = "Failed to create guild. Name might already be taken."
                                            }
                                            actionLoading = false
                                        }
                                    } else {
                                        if (inviteCode.isBlank()) {
                                            actionError = "Please enter an invite code."
                                            return@Button
                                        }
                                        actionLoading = true
                                        scope.launch {
                                            val success = socialRepository.joinGuild(inviteCode)
                                            if (success) {
                                                val fetchedGuild = socialRepository.getCurrentGuild()
                                                guild = fetchedGuild
                                                if (fetchedGuild != null) {
                                                    members = socialRepository.getGuildLeaderboard(fetchedGuild.id)
                                                    messages = socialRepository.getGuildMessages(fetchedGuild.id)
                                                }
                                            } else {
                                                actionError = "Guild not found. Double-check your code."
                                            }
                                            actionLoading = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                enabled = !actionLoading
                            ) {
                                if (actionLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = if (isCreateMode) "Create Guild" else "Join Guild",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (isCreateMode) "Want to join an existing guild? Join here" else "Want to start your own guild? Create here",
                        color = AppleBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                isCreateMode = !isCreateMode
                                actionError = null
                            }
                            .padding(8.dp)
                    )
                }
            } else {
                // Guild Dashboard
                Column(modifier = Modifier.fillMaxSize()) {
                    // Small Guild Card at top
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C2C2E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = guild?.description?.ifBlank { "No description provided." } ?: "",
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Invite Code Badge
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(guild?.inviteCode ?: ""))
                                    }
                                ) {
                                    Text("Invite Code", color = TextSecondary, fontSize = 10.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = guild?.inviteCode ?: "",
                                            color = AppleBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = AppleBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tab bar
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = DarkBg,
                        contentColor = AppleBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                color = AppleBlue
                            )
                        },
                        divider = {
                            HorizontalDivider(color = Color(0xFF2C2C2E))
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Members", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Outlined.Group, contentDescription = null) }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Outlined.Leaderboard, contentDescription = null) }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("Chat", fontWeight = FontWeight.Bold) },
                            icon = { Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (activeTab) {
                            0 -> MembersTab(
                                members = members,
                                currentUserId = authRepository.currentUserId() ?: "",
                                onLeaveGuild = {
                                    scope.launch {
                                        isLoading = true
                                        val left = socialRepository.leaveGuild()
                                        if (left) {
                                            guild = null
                                        }
                                        isLoading = false
                                    }
                                }
                            )
                            1 -> LeaderboardTab(
                                members = members,
                                currentUserId = authRepository.currentUserId() ?: ""
                            )
                            2 -> ChatTab(
                                messages = messages,
                                currentUserId = authRepository.currentUserId() ?: "",
                                chatText = chatMessageText,
                                onChatTextChange = { chatMessageText = it },
                                onSendMessage = {
                                    if (chatMessageText.isNotBlank()) {
                                        isSendingMsg = true
                                        val text = chatMessageText
                                        chatMessageText = ""
                                        scope.launch {
                                            val sent = socialRepository.sendGuildMessage(guild!!.id, text)
                                            if (sent) {
                                                messages = socialRepository.getGuildMessages(guild!!.id)
                                                // Scroll to bottom
                                                if (messages.isNotEmpty()) {
                                                    lazyListState.animateScrollToItem(messages.lastIndex)
                                                }
                                            }
                                            isSendingMsg = false
                                        }
                                    }
                                },
                                isSending = isSendingMsg,
                                listState = lazyListState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MembersTab(
    members: List<GuildLeaderboardEntry>,
    currentUserId: String,
    onLeaveGuild: () -> Unit
) {
    var showLeaveDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                val isMe = member.userId == currentUserId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isMe) AppleBlue.copy(alpha = 0.5f) else Color(0xFF2C2C2E)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(AppleBlue.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.rank.take(1),
                                    color = AppleBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = if (isMe) "${member.displayName} (You)" else member.displayName ?: "Hunter",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Lvl ${member.level} · ${member.rank}",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Streak
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${member.currentStreak}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("\uD83D\uDD25", fontSize = 16.sp) // 🔥
                        }
                    }
                }
            }
        }

        Divider(color = Color(0xFF2C2C2E))

        // Leave Guild Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = { showLeaveDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = HardRed)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Leave Guild", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Guild", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to leave this guild? Your weekly progress will no longer contribute to their scoreboard.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        onLeaveGuild()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HardRed)
                ) {
                    Text("Leave", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkCard
        )
    }
}

@Composable
private fun LeaderboardTab(
    members: List<GuildLeaderboardEntry>,
    currentUserId: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(members.size) { index ->
            val member = members[index]
            val isMe = member.userId == currentUserId
            val rankPos = index + 1
            
            val positionColor = when (rankPos) {
                1 -> Color(0xFFF59E0B) // Gold
                2 -> Color(0xFF94A3B8) // Silver
                3 -> Color(0xFFB45309) // Bronze
                else -> Color.Transparent
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isMe) AppleBlue.copy(alpha = 0.5f) else Color(0xFF2C2C2E)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Rank position tag
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (positionColor != Color.Transparent) positionColor else Color(0xFF2C2C2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rankPos",
                                color = if (positionColor != Color.Transparent) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (isMe) "${member.displayName} (You)" else member.displayName ?: "Hunter",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${member.rank} · Streak ${member.currentStreak}d",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Weekly XP earned
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${member.weeklyXp} XP",
                            color = AppleBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "This Week",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatTab(
    messages: List<GuildMessage>,
    currentUserId: String,
    chatText: String,
    onChatTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isSending: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome to the Guild Chat!\nSay something to motivate your team.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.userId == currentUserId
                    ChatBubble(message = msg, isMe = isMe)
                }
            }
        }

        // Input bar
        Surface(
            color = Color(0xFF1C1C1E),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatText,
                    onValueChange = onChatTextChange,
                    placeholder = { Text("Send quest update...", color = TextSecondary) },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AppleBlue,
                        unfocusedBorderColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = onSendMessage,
                    enabled = chatText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (chatText.isNotBlank()) AppleBlue else Color(0xFF2C2C2E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Send",
                        tint = if (chatText.isNotBlank()) Color.White else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: GuildMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                text = message.senderName,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 16.dp
                    )
                )
                .background(if (isMe) AppleBlue else Color(0xFF2C2C2E))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
