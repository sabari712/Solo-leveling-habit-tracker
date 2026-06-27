package com.example.sololeveling90days.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.SoundManager
import com.example.sololeveling90days.data.PrefsKeys
import com.example.sololeveling90days.data.LandmarkType
import com.example.sololeveling90days.theme.*
import com.example.sololeveling90days.ui.exercise.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// Theme colors
private val PenaltyRed = Color(0xFFE53935)
private val AlertAmber = Color(0xFFFFB300)
private val DarkOverlay = Color(0xE60A0A0F)

@Composable
fun PenaltyScreen(
    repository: AppRepository,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        SoundManager.playLockoutAlert()
        SoundManager.speak("Warning. The system has detected consecutive inactivity. Penalty Zone Lockout active. Complete thirty squats to awaken.")
    }

    // Setup state
    var isChallengeStarted by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Tracking states
    var detectedPose by remember { mutableStateOf<Pose?>(null) }
    var currentReps by remember { mutableStateOf(0) }
    val targetReps = 30
    var currentAngle by remember { mutableStateOf(0.0) }
    var feedbackText by remember { mutableStateOf("Get into position") }
    var feedbackIsGood by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }
    var trackingSide by remember { mutableStateOf("L") }

    // Tracking Engines (Squats: Up=160, Down=90)
    val repCounter = remember { RepCounter(160.0, 90.0) }
    val repValidator = remember { RepValidator(minRepDurationMs = 500L) }
    val oneEuroFilter = remember { OneEuroFilter(minCutoff = 1.0, beta = 0.007, dCutoff = 1.0) }
    val landmarkSmoother = remember { LandmarkSmoother(alpha = 0.6f) }

    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAnim"
    )

    // Process pose updates (Squat tracking)
    LaunchedEffect(detectedPose, isChallengeStarted, isUnlocked) {
        if (!isChallengeStarted || isUnlocked) return@LaunchedEffect
        val pose = detectedPose ?: return@LaunchedEffect
        val now = System.currentTimeMillis()

        // Bilateral tracking logic for Squats (HIP -> KNEE -> ANKLE)
        val lm1Type = LandmarkType.LEFT_HIP
        val lm2Type = LandmarkType.LEFT_KNEE
        val lm3Type = LandmarkType.LEFT_ANKLE

        val lm1Left = pose.getPoseLandmark(lm1Type)
        val lm2Left = pose.getPoseLandmark(lm2Type)
        val lm3Left = pose.getPoseLandmark(lm3Type)

        val lm1RightType = LEFT_TO_RIGHT_LANDMARK[lm1Type] ?: lm1Type
        val lm2RightType = LEFT_TO_RIGHT_LANDMARK[lm2Type] ?: lm2Type
        val lm3RightType = LEFT_TO_RIGHT_LANDMARK[lm3Type] ?: lm3Type

        val lm1Right = pose.getPoseLandmark(lm1RightType)
        val lm2Right = pose.getPoseLandmark(lm2RightType)
        val lm3Right = pose.getPoseLandmark(lm3RightType)

        val leftConfidence = minOf(
            lm1Left?.inFrameLikelihood ?: 0f,
            lm2Left?.inFrameLikelihood ?: 0f,
            lm3Left?.inFrameLikelihood ?: 0f
        )
        val rightConfidence = minOf(
            lm1Right?.inFrameLikelihood ?: 0f,
            lm2Right?.inFrameLikelihood ?: 0f,
            lm3Right?.inFrameLikelihood ?: 0f
        )

        // Resolve best side
        val resolved = when {
            leftConfidence >= 0.7f && rightConfidence >= 0.7f && lm1Left != null && lm2Left != null && lm3Left != null
                && lm1Right != null && lm2Right != null && lm3Right != null -> {
                Triple(
                    PointF((lm1Left.position.x + lm1Right.position.x) / 2f, (lm1Left.position.y + lm1Right.position.y) / 2f),
                    PointF((lm2Left.position.x + lm2Right.position.x) / 2f, (lm2Left.position.y + lm2Right.position.y) / 2f),
                    PointF((lm3Left.position.x + lm3Right.position.x) / 2f, (lm3Left.position.y + lm3Right.position.y) / 2f)
                ) to "AVG"
            }
            leftConfidence >= 0.5f && lm1Left != null && lm2Left != null && lm3Left != null -> {
                Triple(lm1Left.position, lm2Left.position, lm3Left.position) to "L"
            }
            rightConfidence >= 0.5f && lm1Right != null && lm2Right != null && lm3Right != null -> {
                Triple(lm1Right.position, lm2Right.position, lm3Right.position) to "R"
            }
            else -> null
        }

        if (resolved != null) {
            val (points, side) = resolved
            val (p1, p2, p3) = points
            trackingSide = side

            // Smooth positions
            val s1 = landmarkSmoother.smooth(lm1Type, p1)
            val s2 = landmarkSmoother.smooth(lm2Type, p2)
            val s3 = landmarkSmoother.smooth(lm3Type, p3)

            // Compute and filter angle
            val rawAngle = AngleCalculator.calculate(s1, s2, s3)
            val filteredAngle = oneEuroFilter.filter(rawAngle, now)
            currentAngle = filteredAngle

            val rawCount = repCounter.update(filteredAngle)
            val validCount = repValidator.validate(rawCount, now)
            if (validCount > currentReps) {
                scope.launch {
                    repository.incrementStat(PrefsKeys.SQUATS_COUNT, 1)
                }
                SoundManager.playQuestDone()
                SoundManager.speakQueue("$validCount")
            }
            currentReps = validCount

            val feedback = FormAnalyzer.analyze(filteredAngle, 160.0, 90.0, repCounter.state)
            feedbackText = feedback.message
            feedbackIsGood = feedback.isGood

            if (validCount >= targetReps) {
                isUnlocked = true
            }
        } else {
            feedbackText = "Make sure your full body is visible"
            feedbackIsGood = false
        }
    }

    // Auto-unlock execution when reps are completed
    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            delay(1500)
            repository.completePenaltyQuest()
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (isChallengeStarted && !isUnlocked) {
            // Camera Challenge View
            if (!hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = PenaltyRed, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Camera permission is required to verify squats.", color = Color.White, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = PenaltyRed)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                // Camera Frame
                val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                val poseDetector = remember {
                    val options = AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                        .build()
                    PoseDetection.getClient(options)
                }
                val cameraController = remember {
                    LifecycleCameraController(context).apply {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        imageAnalysisTargetSize = CameraController.OutputSize(Size(640, 480))
                        imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    }
                }

                LaunchedEffect(Unit) {
                    cameraController.setImageAnalysisAnalyzer(cameraExecutor) { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                            poseDetector.process(image)
                                .addOnSuccessListener { pose -> detectedPose = pose }
                                .addOnCompleteListener { imageProxy.close() }
                        } else {
                            imageProxy.close()
                        }
                    }
                    try {
                        cameraController.bindToLifecycle(lifecycleOwner)
                    } catch (_: Exception) {}
                }

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            controller = cameraController
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Skeleton Overlay
                PoseOverlay(
                    pose = detectedPose,
                    activeLandmarks = setOf(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE,
                        LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE),
                    isFrontCamera = true,
                    modifier = Modifier.fillMaxSize()
                )

                // HUD Overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PENALTY TRIAL",
                            color = PenaltyRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                "Angle: ${currentAngle.toInt()}° [$trackingSide]",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Center Rep Counter
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "$currentReps / $targetReps",
                                color = PenaltyRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 72.sp
                            )
                            Text(
                                text = "SQUATS COMPLETED",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Bottom Feedback
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (feedbackIsGood) GrowthEmerald.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = feedbackText,
                                color = if (feedbackIsGood) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Debug Skip Button
                        if (com.example.sololeveling90days.BuildConfig.DEBUG) {
                            Button(
                                onClick = { isUnlocked = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("DEBUG: Skip Penalty Trial")
                            }
                        }
                    }
                }
            }
        } else if (isUnlocked) {
            // Success Celebration
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F1A14)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("✓", color = GrowthEmerald, fontSize = 72.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "SYSTEM RESTORED",
                        color = GrowthEmerald,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 3.sp
                    )
                    Text(
                        "Hunter, the Penalty Zone trial has been cleared. Do not neglect your daily quests again.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            // Lockout Intro Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F0202), Color(0xFF1E0303), Color(0xFF0A0101))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PenaltyRed,
                        modifier = Modifier.size(64.dp * pulse)
                    )

                    Text(
                        text = "[SYSTEM WARNING]",
                        color = PenaltyRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = "PENALTY ZONE ACTIVE",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    // Penalty Description Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF240404))
                            .border(1.dp, PenaltyRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "LOCKOUT STATUS",
                                color = PenaltyRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "You have failed to maintain consistency. Access to all system modules (Dungeons, Tools, Chat) has been suspended.",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Divider(color = PenaltyRed.copy(alpha = 0.2f))
                            Text(
                                "REQUIRED TRIAL: Complete 30 Camera-Verified Squats to clear the penalty and unlock the System.",
                                color = AlertAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                isChallengeStarted = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PenaltyRed)
                    ) {
                        Text("ENTER PENALTY ZONE TRIAL", fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}
