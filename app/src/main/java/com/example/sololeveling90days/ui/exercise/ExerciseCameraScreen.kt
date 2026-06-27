package com.example.sololeveling90days.ui.exercise
import com.example.sololeveling90days.theme.*

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwitchCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sololeveling90days.data.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

// Theme colors
private val DarkBg = Color(0xFF0A0A0F)
private val NeonBlue = Color(0xFF4FC3F7)
private val NeonPurple = Color(0xFF7C3AED)
private val AccentGold = Color(0xFFF59E0B)
private val GoodGreen = Color(0xFF00E676)
private val WarningRed = Color(0xFFFF5252)

// Confidence thresholds
private const val PRIMARY_CONFIDENCE = 0.7f
private const val FALLBACK_CONFIDENCE = 0.5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCameraScreen(
    exerciseTypeName: String,
    targetReps: Int,
    questId: String = "",
    onComplete: (repsCompleted: Int, durationSeconds: Long, wasVerified: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exerciseType = remember { ExerciseType.valueOf(exerciseTypeName) }
    val definition = remember { getExerciseDefinition(exerciseType) }
    val trackingConfig = remember { EXERCISE_TRACKING_CONFIGS[exerciseType] }

    // Camera permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }

    // Tracking state
    var detectedPose by remember { mutableStateOf<Pose?>(null) }
    var currentReps by remember { mutableStateOf(0) }
    var currentAngle by remember { mutableStateOf(0.0) }
    var feedbackText by remember { mutableStateOf("Get into position") }
    var feedbackIsGood by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var startTimeMs by remember { mutableStateOf(0L) }
    var elapsedSeconds by remember { mutableStateOf(0L) }
    var holdSeconds by remember { mutableStateOf(0) }
    var trackingSide by remember { mutableStateOf("L") } // "L", "R", or "AVG"

    // Industry-grade ML engine instances
    val repCounter = remember {
        trackingConfig?.let { RepCounter(it.upThreshold, it.downThreshold) }
    }
    val repValidator = remember { RepValidator(minRepDurationMs = 400L) }
    val timedHoldTracker = remember {
        trackingConfig?.let { TimedHoldTracker(it.downThreshold, it.upThreshold) }
    }
    val oneEuroFilter = remember { OneEuroFilter(minCutoff = 1.0, beta = 0.007, dCutoff = 1.0) }
    val landmarkSmoother = remember { LandmarkSmoother(alpha = 0.6f) }

    // Timer for elapsed time
    LaunchedEffect(startTimeMs) {
        if (startTimeMs > 0 && !isCompleted) {
            while (true) {
                delay(1000)
                elapsedSeconds = (System.currentTimeMillis() - startTimeMs) / 1000
            }
        }
    }

    // Process pose updates — BILATERAL + ADAPTIVE CONFIDENCE + FILTERED
    LaunchedEffect(detectedPose) {
        val pose = detectedPose ?: return@LaunchedEffect
        val config = trackingConfig ?: return@LaunchedEffect
        if (isCompleted) return@LaunchedEffect

        if (startTimeMs == 0L) startTimeMs = System.currentTimeMillis()
        val now = System.currentTimeMillis()

        val (lm1Type, lm2Type, lm3Type) = config.primaryLandmarks

        // --- BILATERAL LANDMARK RESOLUTION ---
        // Try primary (left) side first, then fallback to right side, then average
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

        val leftGood = leftConfidence >= PRIMARY_CONFIDENCE
        val rightGood = rightConfidence >= PRIMARY_CONFIDENCE
        val leftOk = leftConfidence >= FALLBACK_CONFIDENCE
        val rightOk = rightConfidence >= FALLBACK_CONFIDENCE

        // Resolve which landmarks to use
        data class ResolvedLandmarks(val p1: PointF, val p2: PointF, val p3: PointF, val side: String)

        val resolved: ResolvedLandmarks? = when {
            // Both sides good — average them for maximum stability
            leftGood && rightGood && lm1Left != null && lm2Left != null && lm3Left != null
                && lm1Right != null && lm2Right != null && lm3Right != null -> {
                ResolvedLandmarks(
                    p1 = PointF((lm1Left.position.x + lm1Right.position.x) / 2f, (lm1Left.position.y + lm1Right.position.y) / 2f),
                    p2 = PointF((lm2Left.position.x + lm2Right.position.x) / 2f, (lm2Left.position.y + lm2Right.position.y) / 2f),
                    p3 = PointF((lm3Left.position.x + lm3Right.position.x) / 2f, (lm3Left.position.y + lm3Right.position.y) / 2f),
                    side = "AVG"
                )
            }
            // Left side good
            leftGood && lm1Left != null && lm2Left != null && lm3Left != null -> {
                ResolvedLandmarks(lm1Left.position, lm2Left.position, lm3Left.position, "L")
            }
            // Right side good
            rightGood && lm1Right != null && lm2Right != null && lm3Right != null -> {
                ResolvedLandmarks(lm1Right.position, lm2Right.position, lm3Right.position, "R")
            }
            // Left side fallback (lower confidence)
            leftOk && lm1Left != null && lm2Left != null && lm3Left != null -> {
                ResolvedLandmarks(lm1Left.position, lm2Left.position, lm3Left.position, "L")
            }
            // Right side fallback
            rightOk && lm1Right != null && lm2Right != null && lm3Right != null -> {
                ResolvedLandmarks(lm1Right.position, lm2Right.position, lm3Right.position, "R")
            }
            else -> null
        }

        if (resolved != null) {
            trackingSide = resolved.side

            // --- LANDMARK SMOOTHING (EMA on raw positions) ---
            val s1 = landmarkSmoother.smooth(lm1Type, resolved.p1)
            val s2 = landmarkSmoother.smooth(lm2Type, resolved.p2)
            val s3 = landmarkSmoother.smooth(lm3Type, resolved.p3)

            // --- ANGLE CALCULATION on smoothed landmarks ---
            val rawAngle = AngleCalculator.calculate(s1, s2, s3)

            // --- ONE EURO FILTER on angle ---
            val filteredAngle = oneEuroFilter.filter(rawAngle, now)
            currentAngle = filteredAngle

            when (config.trackingMethod) {
                TrackingMethod.REP_COUNT -> {
                    val rawCount = repCounter?.update(filteredAngle) ?: 0
                    // --- REP VALIDATION (reject phantom reps) ---
                    val validCount = repValidator.validate(rawCount, now)
                    currentReps = validCount

                    val feedback = FormAnalyzer.analyze(
                        filteredAngle, config.upThreshold, config.downThreshold,
                        repCounter?.state ?: ExerciseState.NONE
                    )
                    feedbackText = feedback.message
                    feedbackIsGood = feedback.isGood

                    if (validCount >= targetReps) {
                        isCompleted = true
                    }
                }
                TrackingMethod.TIMED_HOLD -> {
                    timedHoldTracker?.update(filteredAngle, now)
                    holdSeconds = timedHoldTracker?.holdTimeSeconds ?: 0
                    currentReps = holdSeconds
                    val feedback = FormAnalyzer.analyzeHold(
                        filteredAngle, config.downThreshold, config.upThreshold,
                        timedHoldTracker?.isInCorrectForm == true
                    )
                    feedbackText = feedback.message
                    feedbackIsGood = feedback.isGood

                    if (holdSeconds >= targetReps) {
                        isCompleted = true
                    }
                }
            }
        } else {
            feedbackText = "Make sure your full body is visible"
            feedbackIsGood = false
        }
    }

    // Auto-navigate on completion
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            delay(1500) // Show celebration briefly
            val duration = if (startTimeMs > 0) (System.currentTimeMillis() - startTimeMs) / 1000 else 0L
            onComplete(currentReps, duration, true)
        }
    }

    // Active landmarks for highlighting (include both sides now)
    val activeLandmarkSet = remember(trackingConfig) {
        trackingConfig?.let {
            val (l1, l2, l3) = it.primaryLandmarks
            val set = mutableSetOf(l1, l2, l3)
            // Add right-side equivalents
            LEFT_TO_RIGHT_LANDMARK[l1]?.let { r -> set.add(r) }
            LEFT_TO_RIGHT_LANDMARK[l2]?.let { r -> set.add(r) }
            LEFT_TO_RIGHT_LANDMARK[l3]?.let { r -> set.add(r) }
            set
        } ?: emptySet()
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Camera permission required\nfor exercise tracking",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Grant Permission")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) {
                    Text("Back to Quests", color = NeonBlue)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // CameraX Preview
            val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

            // --- ACCURATE POSE DETECTOR (industry-grade) ---
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
                    // --- RESOLUTION CONTROL: 640x480 is the sweet spot for ML Kit ---
                    imageAnalysisTargetSize = CameraController.OutputSize(Size(640, 480))
                    imageAnalysisBackpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                }
            }

            LaunchedEffect(lensFacing) {
                try {
                    cameraController.cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()
                } catch (e: Exception) {
                    val fallbackLens = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        CameraSelector.LENS_FACING_FRONT
                    }
                    try {
                        cameraController.cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(fallbackLens)
                            .build()
                        lensFacing = fallbackLens
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            LaunchedEffect(Unit) {
                cameraController.setImageAnalysisAnalyzer(
                    cameraExecutor
                ) { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                        poseDetector.process(image)
                            .addOnSuccessListener { pose ->
                                detectedPose = pose
                            }
                            .addOnFailureListener {
                                // log error
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
                try {
                    cameraController.bindToLifecycle(lifecycleOwner)
                } catch (e: Exception) {
                    try {
                        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraController.bindToLifecycle(lifecycleOwner)
                        lensFacing = CameraSelector.LENS_FACING_BACK
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
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

            // Canvas Overlay for skeletal tracking
            PoseOverlay(
                pose = detectedPose,
                activeLandmarks = activeLandmarkSet,
                isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT,
                modifier = Modifier.fillMaxSize()
            )

            // UI HUD Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Back & Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchCamera,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = definition.type.label,
                                color = NeonBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Time: ${elapsedSeconds}s",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Center: Big rep counter or celebration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(24.dp)
                        ) {
                            Text(
                                "QUEST COMPLETE",
                                color = AccentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "SYSTEM VERIFIED",
                                color = GoodGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Rep progress indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "$currentReps / $targetReps",
                                color = NeonPurple,
                                fontWeight = FontWeight.Black,
                                fontSize = 64.sp
                            )
                            val unitLabel = if (definition.trackingMethod == TrackingMethod.TIMED_HOLD) "seconds" else "reps"
                            Text(
                                text = unitLabel.uppercase(),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentAngle > 0) {
                                Text(
                                    text = "Angle: ${currentAngle.toInt()}° [$trackingSide]",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Bottom HUD: Feedback
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Form Feedback
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (feedbackIsGood) GoodGreen.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = feedbackText,
                            color = if (feedbackIsGood) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
