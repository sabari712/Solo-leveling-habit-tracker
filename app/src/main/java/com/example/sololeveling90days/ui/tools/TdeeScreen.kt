package com.example.sololeveling90days.ui.tools

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sololeveling90days.data.*
import com.example.sololeveling90days.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdeeScreen(
    repository: AppRepository,
    onBack: () -> Unit
) {
    // User inputs
    var weightKg by remember { mutableStateOf("75") }
    var heightCm by remember { mutableStateOf("175") }
    var ageYears by remember { mutableStateOf("25") }
    var sex by remember { mutableStateOf("male") }
    var activityLevel by remember { mutableStateOf("moderate") }
    var dailyIntake by remember { mutableStateOf("2000") }
    var todayWeight by remember { mutableStateOf("") }

    // TDEE estimator state
    var estimator by remember { mutableStateOf<AdaptiveTdeeEstimator?>(null) }
    var formulaTdee by remember { mutableDoubleStateOf(0.0) }
    var currentTdee by remember { mutableDoubleStateOf(0.0) }
    var confidenceInterval by remember { mutableStateOf(Pair(0.0, 0.0)) }
    var daysLogged by remember { mutableIntStateOf(0) }
    var hasInitialized by remember { mutableStateOf(false) }
    var showSetup by remember { mutableStateOf(true) }

    // Pulsing glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ADAPTIVE TDEE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSetup) {
                // Setup card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "[SYSTEM]: METABOLIC CALIBRATION",
                            color = AppleBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Enter your stats to initialize the Kalman filter with a Mifflin-St Jeor prior.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(16.dp))

                        // Weight
                        OutlinedTextField(
                            value = weightKg,
                            onValueChange = { weightKg = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = tdeeFieldColors(),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Height
                        OutlinedTextField(
                            value = heightCm,
                            onValueChange = { heightCm = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = tdeeFieldColors(),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Age
                        OutlinedTextField(
                            value = ageYears,
                            onValueChange = { ageYears = it },
                            label = { Text("Age (years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = tdeeFieldColors(),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Sex selector
                        Text("Sex", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("male", "female").forEach { option ->
                                val selected = sex == option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) AppleBlue.copy(alpha = 0.2f) else AppleBlue.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) AppleBlue else Color(0xFF3C3C3E)
                                        )
                                        .clickable { sex = option }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        option.uppercase(),
                                        color = if (selected) AppleBlue else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        // Activity level selector
                        Text("Activity Level", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        val levels = listOf(
                            "sedentary" to "Sedentary",
                            "light" to "Light",
                            "moderate" to "Moderate",
                            "active" to "Active",
                            "very_active" to "Very Active"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            levels.forEach { (key, label) ->
                                val selected = activityLevel == key
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (selected) AppleBlue.copy(alpha = 0.15f) else AppleBlue.copy(alpha = 0.15f)
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) AppleBlue else Color(0xFF3C3C3E)
                                        )
                                        .clickable { activityLevel = key }
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        label.uppercase(),
                                        color = if (selected) AppleBlue else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Initialize button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppleBlue)
                                .clickable {
                                    val w = weightKg.toDoubleOrNull() ?: 75.0
                                    val h = heightCm.toDoubleOrNull() ?: 175.0
                                    val a = ageYears.toDoubleOrNull() ?: 25.0
                                    val tdee = mifflinStJeorTdee(w, h, a, sex, activityLevel)
                                    formulaTdee = tdee
                                    val est = AdaptiveTdeeEstimator(w, tdee)
                                    estimator = est
                                    currentTdee = est.currentTdee
                                    confidenceInterval = est.currentTdeeConfidenceInterval
                                    daysLogged = 0
                                    hasInitialized = true
                                    showSetup = false
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "INITIALIZE FILTER",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            } else if (hasInitialized) {
                // --- Results dashboard ---

                // Formula vs adaptive comparison
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = glowAlpha))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "[SYSTEM]: TDEE ESTIMATE",
                            color = AppleBlue,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(16.dp))

                        // Big TDEE number
                        Text(
                            "${currentTdee.toInt()}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 48.sp
                        )
                        Text(
                            "kcal/day (adaptive)",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // Stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox("FORMULA", "${formulaTdee.toInt()}", "kcal/day")
                            StatBox("DAYS LOGGED", "$daysLogged", "days")
                            StatBox("95% CI", "${confidenceInterval.first.toInt()}-${confidenceInterval.second.toInt()}", "kcal")
                        }

                        Spacer(Modifier.height(12.dp))

                        // Accuracy indicator
                        val diff = kotlin.math.abs(currentTdee - formulaTdee)
                        val diffLabel = if (currentTdee > formulaTdee) "+${diff.toInt()}" else "-${diff.toInt()}"
                        val diffColor = if (daysLogged < 7) TextSecondary
                            else if (diff < 50) SuccessGreen
                            else ActionOrange

                        Text(
                            "Adaptive vs Formula: $diffLabel kcal/day",
                            color = diffColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Log a day card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "LOG TODAY",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = dailyIntake,
                            onValueChange = { dailyIntake = it },
                            label = { Text("Calorie Intake (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = tdeeFieldColors(),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = todayWeight,
                            onValueChange = { todayWeight = it },
                            label = { Text("Morning Weight (kg) - optional") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = tdeeFieldColors(),
                            shape = RoundedCornerShape(0.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SuccessGreen)
                                .clickable {
                                    val intake = dailyIntake.toDoubleOrNull() ?: return@clickable
                                    val weight = todayWeight.toDoubleOrNull()
                                    estimator?.let { est ->
                                        est.step(intake, weight)
                                        currentTdee = est.currentTdee
                                        confidenceInterval = est.currentTdeeConfidenceInterval
                                        daysLogged = est.day
                                        todayWeight = ""
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SUBMIT DAY LOG",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // Recalibrate button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSetup = true },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = ActionOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "RECALIBRATE FILTER",
                            color = ActionOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DisciplineNavy),
                    border = BorderStroke(1.dp, AppleBlue.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "HOW IT WORKS",
                            color = TextSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        val infoText = buildString {
                            appendLine("The filter starts from the Mifflin-St Jeor formula as a prior.")
                            appendLine()
                            appendLine("Each day you log calories and (optionally) weight, the Kalman filter updates its TDEE estimate.")
                            appendLine()
                            appendLine("After ~2-4 weeks of consistent logging, the estimate becomes highly personalized.")
                            appendLine()
                            append("The 95% CI narrows as more data arrives, showing your confidence level.")
                        }
                        Text(infoText, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(unit, color = TextSecondary, fontSize = 10.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun tdeeFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = AppleBlue,
    unfocusedBorderColor = Color(0xFF3C3C3E),
    focusedLabelColor = AppleBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AppleBlue
)
