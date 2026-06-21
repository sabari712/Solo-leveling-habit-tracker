package com.example.sololeveling90days.ui.cards

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*

// ---------------------------------------------------------------------------
// Data helpers
// ---------------------------------------------------------------------------

@Composable
private fun cardColorForCategory(category: CardCategory): Color = when (category) {
    CardCategory.CONFIDENCE -> CardConfidence
    CardCategory.STRENGTH   -> CardStrength
    CardCategory.DISCIPLINE -> CardDiscipline
    CardCategory.WISDOM     -> CardWisdom
    CardCategory.FOCUS      -> CardFocus
}

private val ALL_FILTER = "All"
private val CATEGORY_FILTERS = listOf(ALL_FILTER) + CardCategory.values().map { it.label }

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardLibraryScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val collectedCards by repository.collectedCards.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFilter by remember { mutableStateOf(ALL_FILTER) }

    val filteredCards = remember(selectedFilter) {
        if (selectedFilter == ALL_FILTER) MOTIVATIONAL_CARDS
        else MOTIVATIONAL_CARDS.filter { it.category.label == selectedFilter }
    }

    val collectedCount = collectedCards.size

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Card Library",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Collected counter ──────────────────────────────────────────
            CollectedCounter(
                collected = collectedCount,
                total = MOTIVATIONAL_CARDS.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Category filter tabs ───────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = CATEGORY_FILTERS.indexOf(selectedFilter).coerceAtLeast(0),
                containerColor = DarkBg,
                contentColor = PrimaryPurpleLight,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    val idx = CATEGORY_FILTERS.indexOf(selectedFilter).coerceAtLeast(0)
                    if (idx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                            color = PrimaryPurple
                        )
                    }
                },
                divider = {}
            ) {
                CATEGORY_FILTERS.forEachIndexed { index, filter ->
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        text = {
                            Text(
                                text = filter,
                                fontSize = 13.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFilter == filter) PrimaryPurpleLight else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Card grid ─────────────────────────────────────────────────
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCards, key = { it.id }) { card ->
                    val isCollected = collectedCards.contains(card.id)
                    MotivationalCardItem(card = card, isCollected = isCollected)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun CollectedCounter(collected: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total > 0) collected.toFloat() / total else 0f

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CollectionsBookmark,
                    contentDescription = null,
                    tint = XPGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$collected / $total Collected",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Complete quests to unlock cards",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            // Circular-ish progress indicator
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(44.dp),
                    color = XPGold,
                    trackColor = DarkCardAlt,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MotivationalCardItem(card: MotivationalCard, isCollected: Boolean) {
    val accent = cardColorForCategory(card.category)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            accent.copy(alpha = 0.85f),
            accent.copy(alpha = 0.45f),
            DarkCard
        )
    )

    Box(
        modifier = Modifier
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(
                width = if (isCollected) 1.5.dp else 0.5.dp,
                color = if (isCollected) accent.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Background gradient (blurred when locked)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .then(if (!isCollected) Modifier.blur(6.dp) else Modifier)
        )

        if (isCollected) {
            // ── Unlocked card content ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Category badge
                CategoryBadge(label = card.category.label, accent = accent)

                // Quote
                Text(
                    text = "\"${card.quote}\"",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // Author
                Text(
                    text = "\u2014 ${card.author}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // ── Locked card overlay ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = card.category.label,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Complete quests\nto unlock",
                        color = TextMuted,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}
