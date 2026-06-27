package com.example.sololeveling90days.data

import kotlinx.serialization.Serializable

@Serializable
enum class ExerciseType(val label: String, val emoji: String) {
    PUSHUPS("Push-ups", "\uD83D\uDCAA"),
    PULL_UPS("Pull-ups", "\uD83C\uDFCB\uFE0F"),
    SQUATS("Squats", "\uD83E\uDDB5"),
    LUNGES("Lunges", "\uD83D\uDEB6"),
    BICEP_CURLS("Bicep Curls", "\uD83D\uDCAA"),
    SHOULDER_PRESS("Shoulder Press", "\uD83C\uDFCB\uFE0F"),
    SIT_UPS("Sit-ups", "\uD83D\uDD04"),
    CRUNCHES("Crunches", "\uD83D\uDD04"),
    LEG_RAISES("Leg Raises", "\uD83E\uDDB5"),
    JUMPING_JACKS("Jumping Jacks", "\u2B50"),
    BURPEES("Burpees", "\uD83D\uDD25"),
    HIGH_KNEES("High Knees", "\uD83C\uDFC3"),
    MOUNTAIN_CLIMBERS("Mountain Climbers", "\u26F0\uFE0F"),
    GLUTE_BRIDGES("Glute Bridges", "\uD83C\uDF51"),
    CALF_RAISES("Calf Raises", "\uD83E\uDDB6"),
    TRICEP_DIPS("Tricep Dips", "\uD83D\uDCAA"),
    LATERAL_RAISES("Lateral Raises", "\uD83C\uDFCB\uFE0F"),
    DEADLIFTS("Deadlifts", "\uD83C\uDFCB\uFE0F"),
    BENT_OVER_ROWS("Bent-over Rows", "\uD83C\uDFCB\uFE0F"),
    FRONT_RAISES("Front Raises", "\uD83C\uDFCB\uFE0F"),
    PLANK("Plank", "\u23F1\uFE0F"),
    WALL_SIT("Wall Sit", "\uD83E\uDDF1")
}

@Serializable
enum class TrackingMethod { REP_COUNT, TIMED_HOLD }

@Serializable
enum class MuscleGroup(val label: String, val emoji: String) {
    CHEST("Chest", "\uD83E\uDEC1"),
    BACK("Back", "\uD83D\uDD19"),
    SHOULDERS("Shoulders", "\uD83C\uDFCB\uFE0F"),
    ARMS("Arms", "\uD83D\uDCAA"),
    CORE("Core", "\uD83C\uDFAF"),
    LEGS("Legs", "\uD83E\uDDB5"),
    FULL_BODY("Full Body", "\uD83D\uDD25"),
    CARDIO("Cardio", "\u2764\uFE0F")
}

@Serializable
enum class CameraPosition { FRONT, SIDE }

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
    val difficulty: QuestDifficulty = QuestDifficulty.NORMAL,
    val exerciseType: ExerciseType? = null,
    val targetReps: Int = 0,
    val isVerified: Boolean = false
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
    val isSubscribed: Boolean = true,
    val weightKg: Float = 70f,
    val isPenaltyActive: Boolean = false,
    val penaltyReason: String = "",
    val str: Int = 10,
    val agi: Int = 10,
    val int: Int = 10,
    val vit: Int = 10,
    val sen: Int = 10,
    val unallocatedPoints: Int = 0,
    val avatarResId: String = "avatar_monarch",
    val squatsCount: Int = 0,
    val joggingDistanceTotal: Float = 0f,
    val focusSessionsCount: Int = 0,
    val meditationSessionsCount: Int = 0,
    val moodJournalsCount: Int = 0,
    val gold: Int = 0,
    val activeTitle: String = "",
    val unlockedTitles: List<String> = emptyList(),
    val inventory: Map<String, Int> = emptyMap(),
    val equippedGear: List<String> = emptyList(),
    val gender: String = "Male",
    val heightCm: Float = 175f,
    val ageYears: Int = 25,
    val fitnessGoal: String = "Maintenance"
)

val UserProfile.level: Int get() = levelFromXp(totalXp)

val UserProfile.bmi: Float get() = if (heightCm > 0f) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

val UserProfile.bmr: Float get() {
    return if (gender.lowercase(java.util.Locale.US) == "female") {
        10f * weightKg + 6.25f * heightCm - 5f * ageYears - 161f
    } else {
        10f * weightKg + 6.25f * heightCm - 5f * ageYears + 5f
    }
}

val UserProfile.maintenanceCalories: Int get() {
    val multiplier = if (streak > 5) 1.725f else if (streak > 2) 1.55f else 1.375f
    return (bmr * multiplier).toInt()
}

val UserProfile.targetCalories: Int get() {
    return when (fitnessGoal) {
        "Weight Loss" -> (maintenanceCalories - 450).coerceAtLeast(1200)
        "Muscle Gain" -> maintenanceCalories + 350
        else -> maintenanceCalories
    }
}

val UserProfile.dailyWaterTargetLiters: Float get() = weightKg * 0.035f

fun UserProfile.gearBonus(statType: String): Int {
    var bonus = 0
    equippedGear.forEach { gearId ->
        val gear = SYSTEM_GEAR_ITEMS.firstOrNull { it.id == gearId }
        if (gear != null && gear.statType == statType) {
            bonus += gear.bonusValue
        }
    }
    return bonus
}

val UserProfile.effectiveStr: Int get() = str + gearBonus("STR")
val UserProfile.effectiveAgi: Int get() = agi + gearBonus("AGI")
val UserProfile.effectiveInt: Int get() = int + gearBonus("INT")
val UserProfile.effectiveVit: Int get() = vit + gearBonus("VIT")
val UserProfile.effectiveSen: Int get() = sen + gearBonus("SEN")

val UserProfile.strCap: Int get() = 10 + (squatsCount / 50)
val UserProfile.agiCap: Int get() = 10 + (joggingDistanceTotal / 2f).toInt()
val UserProfile.intCap: Int get() = 10 + (focusSessionsCount / 2)
val UserProfile.vitCap: Int get() = 10 + (level * 2)
val UserProfile.senCap: Int get() = 10 + ((meditationSessionsCount + moodJournalsCount) / 3)

val UserProfile.strUnlockProgress: String get() {
    val nextLevel = str + 1
    val repsNeeded = (nextLevel - 10) * 50
    val diff = repsNeeded - squatsCount
    return if (diff <= 0) "" else "Complete $diff more squats to raise cap."
}
val UserProfile.agiUnlockProgress: String get() {
    val nextLevel = agi + 1
    val distanceNeeded = (nextLevel - 10) * 2f
    val diff = distanceNeeded - joggingDistanceTotal
    return if (diff <= 0) "" else "Run " + String.format(java.util.Locale.US, "%.1f", diff) + " km more to raise cap."
}
val UserProfile.intUnlockProgress: String get() {
    val nextLevel = int + 1
    val sessionsNeeded = (nextLevel - 10) * 2
    val diff = sessionsNeeded - focusSessionsCount
    return if (diff <= 0) "" else "Complete $diff more Focus Sessions to raise cap."
}
val UserProfile.senUnlockProgress: String get() {
    val nextLevel = sen + 1
    val entriesNeeded = (nextLevel - 10) * 3
    val currentDone = meditationSessionsCount + moodJournalsCount
    val diff = entriesNeeded - currentDone
    return if (diff <= 0) "" else "Log $diff more Meditation/Journals to raise cap."
}

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
enum class ActivityType(val label: String, val emoji: String, val met: Double, val xpPerKm: Int) {
    WALKING("Walking", "\uD83D\uDEB6", 3.5, 30),
    JOGGING("Jogging", "\uD83C\uDFC3", 7.0, 50),
    RUNNING("Running", "\uD83D\uDCA8", 9.8, 70),
    SPRINTING("Sprinting", "\u26A1", 14.5, 100),
    CYCLING("Cycling", "\uD83D\uDEB4", 7.5, 40),
    HIKING("Hiking", "\uD83E\uDD7E", 6.0, 60);

    /** Calories burned = MET * weight_kg * duration_hours */
    fun caloriesBurned(weightKg: Float, durationSeconds: Long): Int {
        val hours = durationSeconds / 3600.0
        return (met * weightKg * hours).toInt()
    }

    /** XP earned = xpPerKm * distance, capped at 500 per session */
    fun xpEarned(distanceKm: Float): Int {
        return (xpPerKm * distanceKm).toInt().coerceAtMost(500)
    }

    /** Whether to show speed (km/h) instead of pace (min/km) */
    val showSpeed: Boolean get() = this == CYCLING || this == SPRINTING
}

@Serializable
data class JoggingSession(
    val id: String,
    val date: String,
    val distanceKm: Float,
    val durationSeconds: Long,
    val routePoints: List<LatLngPoint> = emptyList(),
    val activityType: ActivityType = ActivityType.JOGGING,
    val caloriesBurned: Int = 0,
    val avgPaceSecondsPerKm: Int = 0,
    val xpEarned: Int = 0
)

@Serializable
data class DailySteps(
    val date: String,
    val steps: Int
)

@Serializable
data class Guild(
    val id: String,
    val name: String,
    val description: String = "",
    @kotlinx.serialization.SerialName("created_by") val createdBy: String? = null,
    @kotlinx.serialization.SerialName("invite_code") val inviteCode: String
)

@Serializable
data class GuildMember(
    @kotlinx.serialization.SerialName("guild_id") val guildId: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("joined_at") val joinedAt: String? = null
)

@Serializable
data class GuildMessage(
    val id: String? = null,
    @kotlinx.serialization.SerialName("guild_id") val guildId: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("sender_name") val senderName: String,
    val message: String,
    @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class GuildLeaderboardEntry(
    @kotlinx.serialization.SerialName("guild_id") val guildId: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("display_name") val displayName: String? = "Hunter",
    val level: Int = 1,
    val rank: String = "E-Class",
    @kotlinx.serialization.SerialName("current_streak") val currentStreak: Int = 0,
    @kotlinx.serialization.SerialName("weekly_xp") val weeklyXp: Int = 0
)

data class AvatarOption(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val startColor: Long,
    val endColor: Long
)

val AVATAR_OPTIONS = listOf(
    AvatarOption("avatar_monarch", "Shadow Monarch", "👑", "The Sovereign of Shadows. Commands pure darkness.", 0xFF7C3AED, 0xFF1E1B4B),
    AvatarOption("avatar_necromancer", "Necromancer", "💀", "A dark wizard skilled in raising the fallen.", 0xFF6366F1, 0xFF0F172A),
    AvatarOption("avatar_assassin", "Shadow Assassin", "🗡️", "A swift lethal strike from the void.", 0xFF475569, 0xFF0F172A),
    AvatarOption("avatar_berserker", "Berserker", "🪓", "Channels raw fury and unbreakable defense.", 0xFFEF4444, 0xFF450A0A),
    AvatarOption("avatar_healer", "Divine Healer", "✨", "Rejuvenating magic from the High Heavens.", 0xFF10B981, 0xFF064E3B),
    AvatarOption("avatar_ranger", "Shadow Ranger", "🏹", "Sniper from the dark with perfect precision.", 0xFF84CC16, 0xFF1E3A8A),
    AvatarOption("avatar_mage", "High Mage", "⚡", "Unleashes lightning storms upon the battlefield.", 0xFF06B6D4, 0xFF172554),
    AvatarOption("avatar_tank", "Unmovable Shield", "🛡️", "The ultimate front line. Steel armor defense.", 0xFFF59E0B, 0xFF78350F)
)

@Serializable
data class ShadowHunter(
    val id: String,
    val name: String,
    val rank: String,
    val description: String,
    val emoji: String,
    val isExtracted: Boolean = false,
    val assignedStat: String? = null
)

@Serializable
data class DungeonRaid(
    val id: String,
    val name: String,
    val rank: String,
    val bossName: String,
    val bossMaxHp: Int,
    val bossCurrentHp: Int,
    val isDefeated: Boolean = false,
    val goldReward: Int,
    val xpReward: Int,
    val combatLogs: List<String> = emptyList(),
    val wasAttemptedToday: Boolean = false
)

@Serializable
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val cost: Int,
    val emoji: String
)

@Serializable
data class GearItem(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val statType: String,
    val bonusValue: Int
)

val SYSTEM_SHOP_ITEMS = listOf(
    ShopItem("shop_gate_pass", "Gate Pass", "Acquire one pass to skip consecutive inactivity penalties and avoid penalty zone lockout.", 200, "🎫"),
    ShopItem("shop_xp_booster", "XP Booster Scroll", "Acquire a high-grade scroll that doubles all XP awards for the next 24 hours.", 150, "📜"),
    ShopItem("shop_stat_reset", "Stat Reset Scroll", "Acquire a rare system reset scroll that returns all allocated stat points to unallocated.", 250, "🔄"),
    ShopItem("shop_monarch_elixir", "Monarch's Elixir", "Acquire a legendary elixir that permanently raises the cap limit of all attributes by +1.", 400, "🧪")
)

val SYSTEM_GEAR_ITEMS = listOf(
    GearItem("gear_venom_fang", "Kasaka's Venom Fang", "A dagger crafted from Kasaka's tooth. Infuses swiftness.", "🗡️", "AGI", 3),
    GearItem("gear_demon_sword", "Demon King's Shortsword", "Unleashes the fury of the Demon King. Boosts strength.", "⚔️", "STR", 4),
    GearItem("gear_orc_staff", "High Orc Spell Staff", "Infused with High Orc shaman magic. Boosts intelligence.", "🔮", "INT", 4),
    GearItem("gear_monarch_ring", "Monarch's Ring", "A glowing ring carrying pure mana energy. Boosts sense.", "💍", "SEN", 3)
)

