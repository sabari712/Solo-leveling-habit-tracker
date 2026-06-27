package com.example.sololeveling90days.ui.exercise

import android.graphics.PointF
import kotlin.math.*

// ===========================================================================
// AngleCalculator — unchanged, core geometry
// ===========================================================================

/**
 * Calculates the angle at the midPoint (vertex) formed by firstPoint and lastPoint.
 * Returns angle in degrees (0-180).
 */
object AngleCalculator {
    fun calculate(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        val angle1 = atan2(
            (firstPoint.y - midPoint.y).toDouble(),
            (firstPoint.x - midPoint.x).toDouble()
        )
        val angle2 = atan2(
            (lastPoint.y - midPoint.y).toDouble(),
            (lastPoint.x - midPoint.x).toDouble()
        )
        var angle = abs(angle1 - angle2) * (180.0 / PI)
        if (angle > 180.0) {
            angle = 360.0 - angle
        }
        return angle
    }
}

// ===========================================================================
// One Euro Filter — industry-standard adaptive low-pass filter
// ===========================================================================

/**
 * One Euro Filter for real-time signal smoothing.
 *
 * Used by Meta Quest hand tracking, Apple ARKit, and Google MediaPipe.
 * Key insight: adapts cutoff frequency based on signal speed — smooth when
 * still, responsive when moving fast.
 *
 * Paper: Casiez et al., "1€ Filter: A Simple Speed-based Low-pass Filter
 *        for Noisy Input in Interactive Systems", CHI 2012.
 *
 * @param minCutoff  Minimum cutoff frequency (Hz). Lower = more smoothing
 *                   when signal is slow. Default 1.0 is good for pose angles.
 * @param beta       Speed coefficient. Higher = less lag during fast movements.
 *                   0.007 is tuned for exercise rep speeds (~1-2 Hz).
 * @param dCutoff    Cutoff frequency for the derivative filter. Default 1.0.
 */
class OneEuroFilter(
    private val minCutoff: Double = 1.0,
    private val beta: Double = 0.007,
    private val dCutoff: Double = 1.0
) {
    private var xPrev: Double = Double.NaN
    private var dxPrev: Double = 0.0
    private var tPrev: Long = 0L
    private var initialized = false

    /**
     * Filter a new raw value at the given timestamp.
     *
     * @param value  Raw signal value (e.g., angle in degrees)
     * @param timestampMs  Current time in milliseconds
     * @return Smoothed value
     */
    fun filter(value: Double, timestampMs: Long): Double {
        if (!initialized) {
            xPrev = value
            tPrev = timestampMs
            initialized = true
            return value
        }

        val dt = ((timestampMs - tPrev).toDouble() / 1000.0).coerceAtLeast(0.001)
        tPrev = timestampMs

        // Estimate derivative (speed of change)
        val dx = (value - xPrev) / dt
        val edx = smoothingFactor(dt, dCutoff)
        val dxFiltered = edx * dx + (1.0 - edx) * dxPrev
        dxPrev = dxFiltered

        // Adaptive cutoff based on speed
        val cutoff = minCutoff + beta * abs(dxFiltered)
        val alpha = smoothingFactor(dt, cutoff)

        // Apply low-pass filter
        val result = alpha * value + (1.0 - alpha) * xPrev
        xPrev = result
        return result
    }

    private fun smoothingFactor(dt: Double, cutoff: Double): Double {
        val tau = 1.0 / (2.0 * PI * cutoff)
        return 1.0 / (1.0 + tau / dt)
    }

    fun reset() {
        xPrev = Double.NaN
        dxPrev = 0.0
        tPrev = 0L
        initialized = false
    }
}

// ===========================================================================
// LandmarkSmoother — EMA on raw landmark (x,y) positions
// ===========================================================================

/**
 * Applies Exponential Moving Average (EMA) on landmark pixel coordinates
 * BEFORE angle calculation, eliminating sub-pixel jitter at the source.
 *
 * @param alpha  Smoothing factor (0-1). Higher = less smoothing / more responsive.
 *               0.6 is a good balance for 30fps pose detection.
 */
class LandmarkSmoother(private val alpha: Float = 0.6f) {
    private val smoothed = mutableMapOf<Int, PointF>()

    /**
     * Smooth a landmark position by its type ID.
     *
     * @param landmarkType  ML Kit PoseLandmark type constant
     * @param raw           Raw detected position
     * @return Smoothed position
     */
    fun smooth(landmarkType: Int, raw: PointF): PointF {
        val prev = smoothed[landmarkType]
        if (prev == null) {
            smoothed[landmarkType] = PointF(raw.x, raw.y)
            return raw
        }
        val sx = alpha * raw.x + (1f - alpha) * prev.x
        val sy = alpha * raw.y + (1f - alpha) * prev.y
        val result = PointF(sx, sy)
        smoothed[landmarkType] = result
        return result
    }

    fun reset() {
        smoothed.clear()
    }
}

// ===========================================================================
// AngleSmoother — kept for backward compat, marked deprecated
// ===========================================================================

/**
 * Simple rolling average window smoother.
 * @deprecated Use [OneEuroFilter] instead for adaptive smoothing with less lag.
 */
@Deprecated("Use OneEuroFilter for better smoothing with less lag", replaceWith = ReplaceWith("OneEuroFilter()"))
class AngleSmoother(private val windowSize: Int = 5) {
    private val values = ArrayDeque<Double>()

    fun smooth(rawAngle: Double): Double {
        values.addLast(rawAngle)
        if (values.size > windowSize) {
            values.removeFirst()
        }
        return values.average()
    }

    fun reset() {
        values.clear()
    }
}

// ===========================================================================
// RepCounter — state machine with debounce
// ===========================================================================

enum class ExerciseState { NONE, UP, DOWN }

class RepCounter(
    private val upThreshold: Double,
    private val downThreshold: Double,
    private val debounceFrames: Int = 3
) {
    var count: Int = 0
        private set
    var state: ExerciseState = ExerciseState.NONE
        private set

    private var pendingState: ExerciseState = ExerciseState.NONE
    private var pendingFrameCount: Int = 0

    /**
     * Update with a new angle value. Returns the current rep count.
     */
    fun update(angle: Double): Int {
        val candidateState = when {
            angle > upThreshold -> ExerciseState.UP
            angle < downThreshold -> ExerciseState.DOWN
            else -> state  // in dead zone, keep current state
        }

        if (candidateState != state) {
            if (candidateState == pendingState) {
                pendingFrameCount++
                if (pendingFrameCount >= debounceFrames) {
                    // Transition confirmed
                    if (candidateState == ExerciseState.UP && state == ExerciseState.DOWN) {
                        count++
                    }
                    state = candidateState
                    pendingFrameCount = 0
                }
            } else {
                pendingState = candidateState
                pendingFrameCount = 1
            }
        } else {
            pendingFrameCount = 0
        }

        return count
    }

    fun reset() {
        count = 0
        state = ExerciseState.NONE
        pendingState = ExerciseState.NONE
        pendingFrameCount = 0
    }
}

// ===========================================================================
// RepValidator — reject phantom reps from noise
// ===========================================================================

/**
 * Validates rep transitions to reject phantom reps caused by noise.
 *
 * A valid rep requires at least [minRepDurationMs] between UP→DOWN→UP
 * transitions. If state transitions happen faster than this, they are
 * noise (jitter), not real reps.
 *
 * @param minRepDurationMs  Minimum time for a full rep cycle. Default 400ms
 *                          (~2.5 reps/sec max, which covers explosive movements).
 */
class RepValidator(
    private val minRepDurationMs: Long = 400L
) {
    var validatedCount: Int = 0
        private set

    private var lastRepTimeMs: Long = 0L
    private var lastRawCount: Int = 0

    /**
     * Call after RepCounter.update(). Returns the validated rep count.
     *
     * @param rawCount  Current raw count from RepCounter
     * @param currentTimeMs  Current system time in milliseconds
     */
    fun validate(rawCount: Int, currentTimeMs: Long): Int {
        if (rawCount > lastRawCount) {
            // A new raw rep was detected
            val elapsed = currentTimeMs - lastRepTimeMs
            if (elapsed >= minRepDurationMs || lastRepTimeMs == 0L) {
                // Valid rep — enough time has passed
                validatedCount++
                lastRepTimeMs = currentTimeMs
            }
            // Else: too fast → phantom rep, ignore it
            lastRawCount = rawCount
        }
        return validatedCount
    }

    fun reset() {
        validatedCount = 0
        lastRepTimeMs = 0L
        lastRawCount = 0
    }
}

// ===========================================================================
// TimedHoldTracker — unchanged, counts hold duration
// ===========================================================================

/**
 * Tracks timed holds (plank, wall sit). Counts seconds where form is correct.
 */
class TimedHoldTracker(
    private val minAngle: Double,
    private val maxAngle: Double
) {
    var holdTimeMs: Long = 0L
        private set
    var isInCorrectForm: Boolean = false
        private set

    private var lastUpdateTimeMs: Long = 0L

    fun update(angle: Double, currentTimeMs: Long) {
        isInCorrectForm = angle in minAngle..maxAngle

        if (isInCorrectForm && lastUpdateTimeMs > 0) {
            val delta = currentTimeMs - lastUpdateTimeMs
            if (delta in 1..500) { // sanity check: max 500ms between frames
                holdTimeMs += delta
            }
        }
        lastUpdateTimeMs = currentTimeMs
    }

    val holdTimeSeconds: Int get() = (holdTimeMs / 1000).toInt()

    fun reset() {
        holdTimeMs = 0L
        isInCorrectForm = false
        lastUpdateTimeMs = 0L
    }
}

// ===========================================================================
// FormFeedback + FormAnalyzer — enhanced with secondary angle checks
// ===========================================================================

data class FormFeedback(
    val message: String,
    val isGood: Boolean
)

object FormAnalyzer {
    fun analyze(angle: Double, upThreshold: Double, downThreshold: Double, state: ExerciseState): FormFeedback {
        return when {
            state == ExerciseState.NONE -> FormFeedback("Get into position", false)
            angle > upThreshold + 10 -> FormFeedback("Great form!", true)
            angle < downThreshold - 10 -> FormFeedback("Good depth!", true)
            angle in (downThreshold - 5)..(downThreshold + 15) && state == ExerciseState.DOWN ->
                FormFeedback("Hold it!", true)
            angle in (upThreshold - 15)..(upThreshold + 5) && state == ExerciseState.UP ->
                FormFeedback("Full extension!", true)
            else -> FormFeedback("Keep going!", true)
        }
    }

    fun analyzeHold(angle: Double, minAngle: Double, maxAngle: Double, isInForm: Boolean): FormFeedback {
        return when {
            isInForm -> FormFeedback("Perfect form! Hold it!", true)
            angle < minAngle -> FormFeedback("Raise your hips higher", false)
            angle > maxAngle -> FormFeedback("Lower your body", false)
            else -> FormFeedback("Adjust your position", false)
        }
    }
}

// ===========================================================================
// BilateralLandmarkResolver — use best available side
// ===========================================================================

/**
 * Maps LEFT landmark types to their RIGHT equivalents for bilateral tracking.
 */
val LEFT_TO_RIGHT_LANDMARK: Map<Int, Int> = mapOf(
    11 to 12,  // LEFT_SHOULDER -> RIGHT_SHOULDER
    13 to 14,  // LEFT_ELBOW -> RIGHT_ELBOW
    15 to 16,  // LEFT_WRIST -> RIGHT_WRIST
    23 to 24,  // LEFT_HIP -> RIGHT_HIP
    25 to 26,  // LEFT_KNEE -> RIGHT_KNEE
    27 to 28   // LEFT_ANKLE -> RIGHT_ANKLE
)
