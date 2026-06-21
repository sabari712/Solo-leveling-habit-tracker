package com.example.sololeveling90days.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.FoodEntry
import com.example.sololeveling90days.theme.*
import kotlinx.coroutines.launch


private val FOOD_DATABASE: Map<String, Pair<Int, String>> = mapOf(
    "rice" to Pair(206, "\uD83C\uDF5A"),
    "white rice" to Pair(206, "\uD83C\uDF5A"),
    "brown rice" to Pair(215, "\uD83C\uDF5A"),
    "chicken breast" to Pair(165, "\uD83C\uDF57"),
    "chicken" to Pair(239, "\uD83C\uDF57"),
    "egg" to Pair(78, "\uD83E\uDD5A"),
    "eggs" to Pair(156, "\uD83E\uDD5A"),
    "banana" to Pair(89, "\uD83C\uDF4C"),
    "apple" to Pair(95, "\uD83C\uDF4E"),
    "orange" to Pair(62, "\uD83C\uDF4A"),
    "pizza" to Pair(285, "\uD83C\uDF55"),
    "burger" to Pair(354, "\uD83C\uDF54"),
    "sandwich" to Pair(300, "\uD83E\uDD6A"),
    "pasta" to Pair(220, "\uD83C\uDF5D"),
    "salad" to Pair(105, "\uD83E\uDD57"),
    "bread" to Pair(79, "\uD83C\uDF5E"),
    "milk" to Pair(150, "\uD83E\uDD5B"),
    "yogurt" to Pair(100, "\uD83E\uDD5B"),
    "oats" to Pair(307, "\uD83C\uDF3E"),
    "oatmeal" to Pair(166, "\uD83C\uDF3E"),
    "coffee" to Pair(5, "\u2615"),
    "tea" to Pair(2, "\uD83C\uDF75"),
    "juice" to Pair(112, "\uD83E\uDD64"),
    "soda" to Pair(140, "\uD83E\uDD64"),
    "chocolate" to Pair(546, "\uD83C\uDF6B"),
    "chips" to Pair(152, "\uD83C\uDF5F"),
    "fries" to Pair(312, "\uD83C\uDF5F"),
    "fish" to Pair(206, "\uD83D\uDC1F"),
    "salmon" to Pair(208, "\uD83D\uDC1F"),
    "tuna" to Pair(132, "\uD83D\uDC1F"),
    "beef" to Pair(250, "\uD83E\uDD69"),
    "steak" to Pair(271, "\uD83E\uDD69"),
    "pork" to Pair(242, "\uD83E\uDD69"),
    "tofu" to Pair(76, "\uD83E\uDED8"),
    "lentils" to Pair(230, "\uD83E\uDED8"),
    "beans" to Pair(227, "\uD83E\uDED8"),
    "broccoli" to Pair(55, "\uD83E\uDD66"),
    "carrot" to Pair(41, "\uD83E\uDD55"),
    "potato" to Pair(163, "\uD83E\uDD54"),
    "sweet potato" to Pair(103, "\uD83C\uDF60"),
    "avocado" to Pair(240, "\uD83E\uDD51"),
    "almonds" to Pair(164, "\uD83E\uDD5C"),
    "peanut butter" to Pair(188, "\uD83E\uDD5C"),
    "butter" to Pair(102, "\uD83E\uDDC8"),
    "cheese" to Pair(113, "\uD83E\uDDC0"),
    "ice cream" to Pair(207, "\uD83C\uDF68"),
    "cake" to Pair(350, "\uD83C\uDF82"),
    "cookie" to Pair(148, "\uD83C\uDF6A"),
    "donut" to Pair(253, "\uD83C\uDF69"),
    "waffle" to Pair(218, "\uD83E\uDDC7"),
    "pancake" to Pair(175, "\uD83E\uDD5E"),
    "cereal" to Pair(150, "\uD83E\uDD63"),
    "soup" to Pair(130, "\uD83C\uDF5C"),
    "noodles" to Pair(220, "\uD83C\uDF5C"),
    "sushi" to Pair(297, "\uD83C\uDF71"),
    "wrap" to Pair(280, "\uD83C\uDF2F"),
    "smoothie" to Pair(150, "\uD83E\uDD64"),
    "protein shake" to Pair(200, "\uD83D\uDCAA"),
    "water" to Pair(0, "\uD83D\uDCA7"),
)

fun estimateCalories(food: String): Pair<Int, String>? {
    val query = food.lowercase().trim()
    // Direct match
    FOOD_DATABASE[query]?.let { return it }
    // Partial match
    for ((key, value) in FOOD_DATABASE) {
        if (query.contains(key) || key.contains(query)) return value
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieTrackerScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var foodInput by rememberSaveable { mutableStateOf("") }
    var estimationResult by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var estimationFood by remember { mutableStateOf("") }
    var notFound by remember { mutableStateOf(false) }
    val foodLog by repository.calorieLog.collectAsStateWithLifecycle(initialValue = emptyList())
    var idCounter by remember { mutableStateOf(0L) }

    LaunchedEffect(foodLog) {
        val maxId = foodLog.maxOfOrNull { it.id } ?: 0L
        if (maxId > idCounter) {
            idCounter = maxId
        }
    }

    val totalCalories = foodLog.sumOf { it.calories }

    val calorieColor = when {
        totalCalories == 0 -> TextSecondary
        totalCalories < 1500 -> CardWisdom
        totalCalories < 2000 -> SuccessGreen
        totalCalories < 2500 -> XPGold
        else -> HardRedLight
    }

    fun estimate() {
        val result = estimateCalories(foodInput)
        estimationFood = foodInput
        if (result != null) {
            estimationResult = result
            notFound = false
        } else {
            estimationResult = null
            notFound = true
        }
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Calorie Tracker", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
        ) {
            // Top stats card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryPurple.copy(alpha = 0.3f),
                                CardFocus.copy(alpha = 0.2f)
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Today's Total", color = TextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$totalCalories kcal",
                            color = calorieColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                        Text(
                            "${foodLog.size} item${if (foodLog.size != 1) "s" else ""} logged",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    // Calorie gauge
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { (totalCalories / 2000f).coerceIn(0f, 1f) },
                            modifier = Modifier.size(72.dp),
                            color = calorieColor,
                            strokeWidth = 7.dp,
                            trackColor = DarkCard
                        )
                        Text(
                            "${((totalCalories / 2000f) * 100).toInt()}%",
                            color = calorieColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search section
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Estimate Food Calories",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = foodInput,
                                onValueChange = {
                                    foodInput = it
                                    estimationResult = null
                                    notFound = false
                                },
                                placeholder = { Text("e.g. chicken breast, banana\u2026", color = TextMuted) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
                                },
                                trailingIcon = {
                                    if (foodInput.isNotEmpty()) {
                                        IconButton(onClick = {
                                            foodInput = ""
                                            estimationResult = null
                                            notFound = false
                                        }) {
                                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary)
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Search
                                ),
                                keyboardActions = KeyboardActions(onSearch = { estimate() }),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = PrimaryPurple,
                                    unfocusedBorderColor = TextMuted,
                                    cursorColor = PrimaryPurple,
                                    focusedContainerColor = DarkBg,
                                    unfocusedContainerColor = DarkBg
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { estimate() },
                                enabled = foodInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("\uD83D\uDD0D  Estimate Calories", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            // Result
                            if (estimationResult != null) {
                                Spacer(Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardFocus.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(estimationResult!!.second, fontSize = 32.sp)
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                estimationFood.replaceFirstChar { it.uppercase() },
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                "\u2248 ${estimationResult!!.first} kcal",
                                                color = CardFocus,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                val entry = FoodEntry(
                                                    id = ++idCounter,
                                                    name = estimationFood.replaceFirstChar { it.uppercase() },
                                                    calories = estimationResult!!.first,
                                                    emoji = estimationResult!!.second
                                                )
                                                scope.launch {
                                                    repository.saveCalorieLog(foodLog + entry)
                                                }
                                                foodInput = ""
                                                estimationResult = null
                                                notFound = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CardFocus),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text("+ Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            if (notFound) {
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = HardRed.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "\u2753 Food not found. Try a common name like 'rice' or 'egg'.",
                                        color = HardRedLight,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Food log header
                if (foodLog.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today's Food Log",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                             TextButton(onClick = {
                                 scope.launch {
                                     repository.saveCalorieLog(emptyList())
                                 }
                             }) {
                                 Text("Clear All", color = HardRedLight, fontSize = 12.sp)
                             }
                        }
                    }

                     items(foodLog, key = { it.id }) { entry ->
                         FoodLogItem(
                             entry = entry,
                             onDelete = {
                                 scope.launch {
                                     repository.saveCalorieLog(foodLog.filter { it.id != entry.id })
                                 }
                             }
                         )
                     }

                    item {
                        // Total bar
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "$totalCalories kcal",
                                    color = calorieColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83E\uDD57", fontSize = 52.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("No foods logged yet", color = TextSecondary, fontSize = 15.sp)
                                Text("Search and add foods above", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun FoodLogItem(
    entry: FoodEntry,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardFocus.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    "1 serving",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Text(
                "${entry.calories}",
                color = XPGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                " kcal",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(HardRed.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove",
                    tint = HardRedLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
