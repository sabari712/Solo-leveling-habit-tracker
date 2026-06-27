package com.example.sololeveling90days.ui.exercise

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

private val NeonBlue = Color(0xFF4FC3F7)
private val NeonPurple = Color(0xFF7C3AED)
private val NeonGreen = Color(0xFF00E676)
private val NeonYellow = Color(0xFFF59E0B)
private val GlowWhite = Color(0xCCFFFFFF)
private val LowConfidenceRed = Color(0xFFFF5252)

// Skeleton connections as pairs of PoseLandmark types
private val SKELETON_CONNECTIONS = listOf(
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
    PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
    PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
    PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
    PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
    PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
    PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
    PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
    PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
    PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
    PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE,
)

@Composable
fun PoseOverlay(
    pose: Pose?,
    activeLandmarks: Set<Int> = emptySet(),  // highlighted tracked landmarks
    isFrontCamera: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        pose ?: return@Canvas
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return@Canvas

        // Mirror horizontally around center if front camera to match mirrored preview
        val scaleX = if (isFrontCamera) -1f else 1f
        if (isFrontCamera) {
            drawContext.transform.scale(scaleX, 1f, center)
        }

        // Draw skeleton connections with confidence-based opacity
        SKELETON_CONNECTIONS.forEach { (startType, endType) ->
            val startLm = pose.getPoseLandmark(startType)
            val endLm = pose.getPoseLandmark(endType)
            if (startLm != null && endLm != null) {
                val minConf = minOf(startLm.inFrameLikelihood, endLm.inFrameLikelihood)
                if (minConf > 0.3f) {  // Show even low-confidence connections dimly
                    val startOffset = Offset(startLm.position.x, startLm.position.y)
                    val endOffset = Offset(endLm.position.x, endLm.position.y)

                    val isActive = startType in activeLandmarks || endType in activeLandmarks
                    val lineColor = when {
                        isActive && minConf >= 0.7f -> NeonPurple
                        isActive -> NeonPurple.copy(alpha = 0.5f)
                        minConf >= 0.7f -> NeonBlue
                        else -> NeonBlue.copy(alpha = 0.4f)
                    }

                    // Glow effect (wider, semi-transparent line behind)
                    drawLine(
                        color = lineColor.copy(alpha = 0.25f * minConf),
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )
                    // Main line — thickness scales with confidence
                    drawLine(
                        color = lineColor,
                        start = startOffset,
                        end = endOffset,
                        strokeWidth = 3f + 3f * minConf,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw landmark dots — color and size reflect confidence
        landmarks.forEach { landmark ->
            val conf = landmark.inFrameLikelihood
            if (conf > 0.3f) {
                val offset = Offset(landmark.position.x, landmark.position.y)
                val isActive = landmark.landmarkType in activeLandmarks

                val dotColor = when {
                    isActive && conf >= 0.7f -> NeonYellow
                    isActive -> NeonYellow.copy(alpha = 0.6f)
                    conf >= 0.7f -> NeonGreen
                    conf >= 0.5f -> NeonGreen.copy(alpha = 0.6f)
                    else -> LowConfidenceRed.copy(alpha = 0.4f)
                }

                // Size scales: active + high confidence = largest
                val dotSize = when {
                    isActive && conf >= 0.7f -> 14f
                    isActive -> 10f
                    conf >= 0.7f -> 8f
                    else -> 5f
                }

                // Glow ring for high-confidence active landmarks
                if (isActive && conf >= 0.7f) {
                    drawCircle(
                        color = dotColor.copy(alpha = 0.3f),
                        radius = dotSize + 8f,
                        center = offset
                    )
                }
                // Outer glow
                drawCircle(
                    color = dotColor.copy(alpha = 0.4f),
                    radius = dotSize + 4f,
                    center = offset
                )
                // Core dot
                drawCircle(
                    color = dotColor,
                    radius = dotSize,
                    center = offset
                )
                // Inner bright spot
                drawCircle(
                    color = GlowWhite.copy(alpha = conf),
                    radius = dotSize * 0.35f,
                    center = offset
                )
            }
        }
    }
}
