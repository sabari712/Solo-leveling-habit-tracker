package com.example.sololeveling90days.data

import kotlinx.serialization.Serializable

@Serializable
data class AdditionalTask(
    val id: String,
    val title: String,
    val emoji: String = "\u2B50",  // ⭐
    val isCompleted: Boolean = false,
    val xpReward: Int = 30,
    val createdDate: String = ""
)

@Serializable
enum class QuestDifficulty(val label: String, val xpMultiplier: Float, val emoji: String) {
    NORMAL("Normal", 1.0f, ""),
    HARD("Hard", 1.5f, "\uD83D\uDD25"),       // 🔥
    LEGENDARY("Legendary", 3.0f, "\u26A1")    // ⚡
}

@Serializable
data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val category: QuestCategory,
    val isCompleted: Boolean = false,
    val isActive: Boolean = true,
    val difficulty: QuestDifficulty = QuestDifficulty.NORMAL
)

@Serializable
enum class QuestCategory(val emoji: String, val label: String) {
    MORNING("\uD83C\uDF05", "Morning"),        // 🌅
    FITNESS("\uD83D\uDCAA", "Fitness"),        // 💪
    MINDSET("\uD83E\uDDE0", "Mindset"),        // 🧠
    NUTRITION("\uD83E\uDD57", "Nutrition"),    // 🥗
    LEARNING("\uD83D\uDCDA", "Learning"),      // 📚
    SLEEP("\uD83D\uDE34", "Sleep"),            // 😴
    PRODUCTIVITY("\u26A1", "Productivity"),    // ⚡
    WELLNESS("\uD83E\uDDD8", "Wellness")       // 🧘
}

@Serializable
data class MotivationalCard(
    val id: String,
    val quote: String,
    val author: String,
    val category: CardCategory,
    val isCollected: Boolean = false
)

@Serializable
enum class CardCategory(val label: String, val colorHex: String) {
    CONFIDENCE("Confidence", "7C3AED"),
    STRENGTH("Strength", "EA580C"),
    DISCIPLINE("Discipline", "DC2626"),
    WISDOM("Wisdom", "0891B2"),
    FOCUS("Focus", "10B981")
}

@Serializable
data class UserProfile(
    val name: String = "",
    val goal: String = "",
    val wakeTime: String = "6:00 AM",
    val streak: Int = 0,
    val totalXp: Int = 0,
    val hardMode: Boolean = false,
    val dayNumber: Int = 1,
    val startDate: String = "",
    val lastCompletedDate: String = "",
    val completedDates: List<String> = emptyList(),
    val gatePasses: Int = 0,
    val comebackAvailable: Boolean = false,
    val comebackDeadline: String = "",
    val isSubscribed: Boolean = true
)

val UserProfile.level: Int get() = levelFromXp(totalXp)

// Rank System: E -> D -> C -> B -> A -> S
enum class HunterRank(val label: String, val xpRequired: Int, val color: String) {
    E("E-Class", 0, "94A3B8"),
    D("D-Class", 1000, "10B981"),
    C("C-Class", 3000, "0891B2"),
    B("B-Class", 6000, "7C3AED"),
    A("A-Class", 10000, "F59E0B"),
    S("S-Class", 15000, "DC2626")
}

fun rankFromXp(xp: Int): HunterRank {
    return HunterRank.values().reversed().firstOrNull { xp >= it.xpRequired } ?: HunterRank.E
}

fun xpForNextRank(xp: Int): Int {
    val currentRank = rankFromXp(xp)
    val nextRank = HunterRank.values().firstOrNull { it.xpRequired > xp }
    return nextRank?.xpRequired?.minus(xp) ?: 0
}

fun xpProgressInRank(xp: Int): Float {
    val currentRank = rankFromXp(xp)
    val nextRank = HunterRank.values().firstOrNull { it.xpRequired > currentRank.xpRequired && it.xpRequired > xp }
        ?: return 1f
    val rangeStart = currentRank.xpRequired
    val rangeEnd = nextRank.xpRequired
    return ((xp - rangeStart).toFloat() / (rangeEnd - rangeStart)).coerceIn(0f, 1f)
}

// Keep level functions for backward compat
fun xpForLevel(level: Int): Int = level * 500

fun levelFromXp(xp: Int): Int {
    var lvl = 1
    var threshold = 500
    while (xp >= threshold) {
        lvl++
        threshold += lvl * 500
    }
    return lvl
}

fun xpProgressInLevel(xp: Int): Float {
    val level = levelFromXp(xp)
    var prevThreshold = 0
    var threshold = 500
    for (l in 1 until level) {
        prevThreshold = threshold
        threshold += (l + 1) * 500
    }
    val inLevelXp = xp - prevThreshold
    val levelRange = threshold - prevThreshold
    return (inLevelXp.toFloat() / levelRange).coerceIn(0f, 1f)
}

@Serializable
data class FoodEntry(
    val id: Long,
    val name: String,
    val calories: Int,
    val emoji: String
)

@Serializable
data class MoodJournalEntry(
    val emoji: String,
    val label: String,
    val score: Int,
    val text: String
)

data class SystemMessage(
    val trigger: String,
    val message: String
)

@Serializable
data class LatLngPoint(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class JoggingSession(
    val id: String,
    val date: String,
    val distanceKm: Float,
    val durationSeconds: Long,
    val routePoints: List<LatLngPoint> = emptyList()
)

@Serializable
data class DailySteps(
    val date: String,
    val steps: Int
)

