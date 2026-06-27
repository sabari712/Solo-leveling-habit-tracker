package com.example.sololeveling90days.data

import android.graphics.PointF
import kotlin.math.*

/**
 * Definition of a single exercise with its ML tracking configuration.
 */
data class ExerciseDefinition(
    val type: ExerciseType,
    val trackingMethod: TrackingMethod,
    val defaultTargetReps: Int,
    val muscleGroup: MuscleGroup,
    val difficulty: QuestDifficulty,
    val instructions: String,
    val cameraPosition: CameraPosition,
    val xpReward: Int
)

/**
 * Tracking configuration for a specific exercise — which landmarks and angle thresholds to use.
 */
data class ExerciseTrackingConfig(
    val exerciseType: ExerciseType,
    val trackingMethod: TrackingMethod,
    val primaryLandmarks: Triple<Int, Int, Int>,  // (point1, vertex, point2) for angle calc
    val upThreshold: Double,
    val downThreshold: Double,
    val cameraPosition: CameraPosition
)

// ML Kit PoseLandmark type constants (mirroring com.google.mlkit.vision.pose.PoseLandmark)
object LandmarkType {
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28
}

/**
 * Library of all 22 default exercises with their definitions.
 */
val EXERCISE_LIBRARY: List<ExerciseDefinition> = listOf(
    ExerciseDefinition(
        type = ExerciseType.PUSHUPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.CHEST,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. Lower your chest to the ground, then push back up.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 100
    ),
    ExerciseDefinition(
        type = ExerciseType.PULL_UPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 5,
        muscleGroup = MuscleGroup.BACK,
        difficulty = QuestDifficulty.LEGENDARY,
        instructions = "Place phone facing you. Pull your chin above the bar, then lower.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 150
    ),
    ExerciseDefinition(
        type = ExerciseType.SQUATS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 15,
        muscleGroup = MuscleGroup.LEGS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Lower your hips until thighs are parallel, then stand.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 80
    ),
    ExerciseDefinition(
        type = ExerciseType.LUNGES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.LEGS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Step forward and lower your back knee, then return.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 80
    ),
    ExerciseDefinition(
        type = ExerciseType.BICEP_CURLS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 12,
        muscleGroup = MuscleGroup.ARMS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Face the camera. Curl your arms up to your shoulders, then lower.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.SHOULDER_PRESS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.SHOULDERS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Face the camera. Press your arms overhead, then lower to shoulder level.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.SIT_UPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 15,
        muscleGroup = MuscleGroup.CORE,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Sit up fully, then lower back down.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.CRUNCHES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 20,
        muscleGroup = MuscleGroup.CORE,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Curl your shoulders off the ground, then lower.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.LEG_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 12,
        muscleGroup = MuscleGroup.CORE,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Raise your legs to 90 degrees, then lower.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.JUMPING_JACKS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 20,
        muscleGroup = MuscleGroup.CARDIO,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Face the camera. Jump while spreading arms and legs, then return.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.BURPEES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.FULL_BODY,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. Drop to a pushup, jump back up with arms overhead.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 120
    ),
    ExerciseDefinition(
        type = ExerciseType.HIGH_KNEES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 30,
        muscleGroup = MuscleGroup.CARDIO,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Face the camera. Run in place bringing knees to hip height.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.MOUNTAIN_CLIMBERS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 20,
        muscleGroup = MuscleGroup.FULL_BODY,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. In plank position, drive knees to chest alternately.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 100
    ),
    ExerciseDefinition(
        type = ExerciseType.GLUTE_BRIDGES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 15,
        muscleGroup = MuscleGroup.LEGS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Lie on back, raise hips to ceiling, then lower.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.CALF_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 20,
        muscleGroup = MuscleGroup.LEGS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Rise onto your toes, then lower heels.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 50
    ),
    ExerciseDefinition(
        type = ExerciseType.TRICEP_DIPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.ARMS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Lower body by bending elbows, then push back up.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.LATERAL_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 12,
        muscleGroup = MuscleGroup.SHOULDERS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Face the camera. Raise arms out to the sides to shoulder height, then lower.",
        cameraPosition = CameraPosition.FRONT,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.DEADLIFTS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 10,
        muscleGroup = MuscleGroup.BACK,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. Hinge at hips keeping back straight, then stand.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 100
    ),
    ExerciseDefinition(
        type = ExerciseType.BENT_OVER_ROWS,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 12,
        muscleGroup = MuscleGroup.BACK,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Bend forward, pull elbows back, then lower.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 70
    ),
    ExerciseDefinition(
        type = ExerciseType.FRONT_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        defaultTargetReps = 12,
        muscleGroup = MuscleGroup.SHOULDERS,
        difficulty = QuestDifficulty.NORMAL,
        instructions = "Place phone to your side. Raise arms straight in front to shoulder height.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 60
    ),
    ExerciseDefinition(
        type = ExerciseType.PLANK,
        trackingMethod = TrackingMethod.TIMED_HOLD,
        defaultTargetReps = 30,  // seconds
        muscleGroup = MuscleGroup.CORE,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. Hold plank position with body straight.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 100
    ),
    ExerciseDefinition(
        type = ExerciseType.WALL_SIT,
        trackingMethod = TrackingMethod.TIMED_HOLD,
        defaultTargetReps = 30,  // seconds
        muscleGroup = MuscleGroup.LEGS,
        difficulty = QuestDifficulty.HARD,
        instructions = "Place phone to your side. Sit against wall with thighs parallel to ground.",
        cameraPosition = CameraPosition.SIDE,
        xpReward = 100
    )
)

/**
 * ML tracking configurations — maps each exercise to the specific landmarks and angle thresholds.
 */
val EXERCISE_TRACKING_CONFIGS: Map<ExerciseType, ExerciseTrackingConfig> = mapOf(
    ExerciseType.PUSHUPS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.PUSHUPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.PULL_UPS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.PULL_UPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 70.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.SQUATS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.SQUATS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.LUNGES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.LUNGES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.BICEP_CURLS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.BICEP_CURLS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 40.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.SHOULDER_PRESS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.SHOULDER_PRESS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.SIT_UPS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.SIT_UPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 140.0, downThreshold = 80.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.CRUNCHES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.CRUNCHES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 140.0, downThreshold = 100.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.LEG_RAISES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.LEG_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_ANKLE),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.JUMPING_JACKS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.JUMPING_JACKS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_WRIST),
        upThreshold = 120.0, downThreshold = 60.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.BURPEES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.BURPEES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 160.0, downThreshold = 80.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.HIGH_KNEES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.HIGH_KNEES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 140.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.MOUNTAIN_CLIMBERS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.MOUNTAIN_CLIMBERS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.GLUTE_BRIDGES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.GLUTE_BRIDGES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 160.0, downThreshold = 100.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.CALF_RAISES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.CALF_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE, LandmarkType.LEFT_HIP),
        upThreshold = 170.0, downThreshold = 155.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.TRICEP_DIPS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.TRICEP_DIPS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.LATERAL_RAISES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.LATERAL_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_WRIST),
        upThreshold = 150.0, downThreshold = 40.0,
        cameraPosition = CameraPosition.FRONT
    ),
    ExerciseType.DEADLIFTS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.DEADLIFTS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
        upThreshold = 160.0, downThreshold = 90.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.BENT_OVER_ROWS to ExerciseTrackingConfig(
        exerciseType = ExerciseType.BENT_OVER_ROWS,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
        upThreshold = 160.0, downThreshold = 70.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.FRONT_RAISES to ExerciseTrackingConfig(
        exerciseType = ExerciseType.FRONT_RAISES,
        trackingMethod = TrackingMethod.REP_COUNT,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_WRIST),
        upThreshold = 150.0, downThreshold = 40.0,
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.PLANK to ExerciseTrackingConfig(
        exerciseType = ExerciseType.PLANK,
        trackingMethod = TrackingMethod.TIMED_HOLD,
        primaryLandmarks = Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP, LandmarkType.LEFT_ANKLE),
        upThreshold = 180.0, downThreshold = 160.0,  // body should be straight: 160-180 degrees
        cameraPosition = CameraPosition.SIDE
    ),
    ExerciseType.WALL_SIT to ExerciseTrackingConfig(
        exerciseType = ExerciseType.WALL_SIT,
        trackingMethod = TrackingMethod.TIMED_HOLD,
        primaryLandmarks = Triple(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE),
        upThreshold = 100.0, downThreshold = 80.0,  // knee should be ~90 degrees: 80-100
        cameraPosition = CameraPosition.SIDE
    )
)

/** Find an exercise definition by type. */
fun getExerciseDefinition(type: ExerciseType): ExerciseDefinition =
    EXERCISE_LIBRARY.first { it.type == type }

/** Get all exercises for a given muscle group. */
fun getExercisesForMuscleGroup(muscleGroup: MuscleGroup): List<ExerciseDefinition> =
    EXERCISE_LIBRARY.filter { it.muscleGroup == muscleGroup }

/** Create a Quest from an exercise definition with custom target reps. */
fun createExerciseQuest(definition: ExerciseDefinition, targetReps: Int = definition.defaultTargetReps): Quest {
    val unit = if (definition.trackingMethod == TrackingMethod.TIMED_HOLD) "seconds" else "reps"
    return Quest(
        id = "ex_${definition.type.name.lowercase()}_${System.currentTimeMillis()}",
        title = definition.type.label,
        description = "$targetReps $unit - ${definition.instructions}",
        xpReward = definition.xpReward,
        category = QuestCategory.FITNESS,
        difficulty = definition.difficulty,
        exerciseType = definition.type,
        targetReps = targetReps
    )
}
