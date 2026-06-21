package com.example.sololeveling90days.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.datastore.preferences.core.edit
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
private data class ProfileDb(
    val id: String,
    val display_name: String,
    val goal: String,
    val wake_time: String,
    val xp: Int,
    val level: Int,
    val rank: String,
    val current_streak: Int,
    val longest_streak: Int,
    val gate_passes: Int,
    val hard_mode_enabled: Boolean,
    val day_number: Int,
    val start_date: String,
    val last_completed_date: String,
    val comeback_available: Boolean,
    val comeback_deadline: String,
    val amoled_mode: Boolean
)

@Serializable
private data class QuestDb(
    val id: String,
    val user_id: String,
    val title: String,
    val description: String,
    val xp_reward: Int,
    val category: String,
    val is_completed: Boolean,
    val is_active: Boolean,
    val difficulty: String
)

@Serializable
private data class CompletedDateDb(
    val user_id: String,
    val date: String
)

@Serializable
private data class CollectedCardDb(
    val user_id: String,
    val card_id: String
)

@Serializable
private data class JoggingSessionDb(
    val id: String,
    val user_id: String,
    val date: String,
    val distance_km: Float,
    val duration_seconds: Long,
    val route_points: String
)

@Serializable
private data class DailyStepsDb(
    val user_id: String,
    val date: String,
    val steps: Int
)

@Serializable
private data class AdditionalTaskDb(
    val id: String,
    val user_id: String,
    val title: String,
    val emoji: String,
    val is_completed: Boolean,
    val xp_reward: Int,
    val created_date: String
)

@Serializable
private data class MoodEntryDb(
    val user_id: String,
    val date: String,
    val emoji: String,
    val label: String,
    val score: Int,
    val journal_text: String
)

@Serializable
private data class CalorieLogDb(
    val id: Long,
    val user_id: String,
    val date: String,
    val name: String,
    val calories: Int,
    val emoji: String
)

class SyncManager(
    private val context: Context,
    private val authRepository: AuthRepository
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val postgrest = SupabaseClient.client.postgrest

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    suspend fun pushUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbProfile = ProfileDb(
                id = userId,
                display_name = profile.name,
                goal = profile.goal,
                wake_time = profile.wakeTime,
                xp = profile.totalXp,
                level = profile.level,
                rank = rankFromXp(profile.totalXp).label,
                current_streak = profile.streak,
                longest_streak = profile.streak, // simplifed
                gate_passes = profile.gatePasses,
                hard_mode_enabled = profile.hardMode,
                day_number = profile.dayNumber,
                start_date = profile.startDate,
                last_completed_date = profile.lastCompletedDate,
                comeback_available = profile.comebackAvailable,
                comeback_deadline = profile.comebackDeadline,
                amoled_mode = false // read from settings later if needed
            )
            postgrest["profiles"].upsert(dbProfile)
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push user profile", e)
        }
    }

    suspend fun pushQuests(quests: List<Quest>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            // Delete old ones first or upsert. Let's map and upsert
            val dbQuests = quests.map {
                QuestDb(
                    id = it.id,
                    user_id = userId,
                    title = it.title,
                    description = it.description,
                    xp_reward = it.xpReward,
                    category = it.category.name,
                    is_completed = it.isCompleted,
                    is_active = it.isActive,
                    difficulty = it.difficulty.name
                )
            }
            if (dbQuests.isNotEmpty()) {
                postgrest["quests"].upsert(dbQuests)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push quests", e)
        }
    }

    suspend fun pushCompletedDates(dates: List<String>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbDates = dates.map { CompletedDateDb(userId, it) }
            if (dbDates.isNotEmpty()) {
                postgrest["completed_dates"].upsert(dbDates)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push completed dates", e)
        }
    }

    suspend fun pushCollectedCards(cards: List<String>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbCards = cards.map { CollectedCardDb(userId, it) }
            if (dbCards.isNotEmpty()) {
                postgrest["collected_cards"].upsert(dbCards)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push collected cards", e)
        }
    }

    suspend fun pushJoggingSessions(sessions: List<JoggingSession>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbSessions = sessions.map {
                JoggingSessionDb(
                    id = it.id,
                    user_id = userId,
                    date = it.date,
                    distance_km = it.distanceKm,
                    duration_seconds = it.durationSeconds,
                    route_points = json.encodeToString(it.routePoints)
                )
            }
            if (dbSessions.isNotEmpty()) {
                postgrest["jogging_sessions"].upsert(dbSessions)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push jogging sessions", e)
        }
    }

    suspend fun pushDailySteps(steps: List<DailySteps>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbSteps = steps.map {
                DailyStepsDb(userId, it.date, it.steps)
            }
            if (dbSteps.isNotEmpty()) {
                postgrest["daily_steps"].upsert(dbSteps)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push daily steps", e)
        }
    }

    suspend fun pushAdditionalTasks(tasks: List<AdditionalTask>) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            val dbTasks = tasks.map {
                AdditionalTaskDb(
                    id = it.id,
                    user_id = userId,
                    title = it.title,
                    emoji = it.emoji,
                    is_completed = it.isCompleted,
                    xp_reward = it.xpReward,
                    created_date = it.createdDate
                )
            }
            if (dbTasks.isNotEmpty()) {
                postgrest["additional_tasks"].upsert(dbTasks)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push additional tasks", e)
        }
    }

    suspend fun pushMoodEntry(entry: MoodJournalEntry?, date: String) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            if (entry == null) {
                postgrest["mood_entries"].delete {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }
            } else {
                val dbMood = MoodEntryDb(
                    user_id = userId,
                    date = date,
                    emoji = entry.emoji,
                    label = entry.label,
                    score = entry.score,
                    journal_text = entry.text
                )
                postgrest["mood_entries"].upsert(dbMood)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push mood entry", e)
        }
    }

    suspend fun pushCalorieLog(log: List<FoodEntry>, date: String) = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            // Delete all entries for today first, then insert new ones
            postgrest["calorie_log"].delete {
                filter {
                    eq("user_id", userId)
                    eq("date", date)
                }
            }
            val dbLog = log.map {
                CalorieLogDb(
                    id = it.id,
                    user_id = userId,
                    date = date,
                    name = it.name,
                    calories = it.calories,
                    emoji = it.emoji
                )
            }
            if (dbLog.isNotEmpty()) {
                postgrest["calorie_log"].insert(dbLog)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to push calorie log", e)
        }
    }

    suspend fun pullFromCloud() = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext
        if (!isOnline()) return@withContext

        try {
            // 1. Profile
            val profileDb = postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<ProfileDb>()

            if (profileDb != null) {
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.USER_NAME] = profileDb.display_name
                    prefs[PrefsKeys.USER_GOAL] = profileDb.goal
                    prefs[PrefsKeys.USER_WAKE_TIME] = profileDb.wake_time
                    prefs[PrefsKeys.STREAK] = profileDb.current_streak
                    prefs[PrefsKeys.TOTAL_XP] = profileDb.xp
                    prefs[PrefsKeys.HARD_MODE] = profileDb.hard_mode_enabled
                    prefs[PrefsKeys.DAY_NUMBER] = profileDb.day_number
                    prefs[PrefsKeys.START_DATE] = profileDb.start_date
                    prefs[PrefsKeys.LAST_COMPLETED_DATE] = profileDb.last_completed_date
                    prefs[PrefsKeys.GATE_PASSES] = profileDb.gate_passes
                    prefs[PrefsKeys.COMEBACK_AVAILABLE] = profileDb.comeback_available
                    prefs[PrefsKeys.COMEBACK_DEADLINE] = profileDb.comeback_deadline
                    prefs[PrefsKeys.AMOLED_MODE] = profileDb.amoled_mode
                    prefs[PrefsKeys.ONBOARDING_DONE] = true
                }
            }

            // 2. Quests
            val questsDb = postgrest["quests"].select {
                filter { eq("user_id", userId) }
            }.decodeList<QuestDb>()

            if (questsDb.isNotEmpty()) {
                val quests = questsDb.map {
                    Quest(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        xpReward = it.xp_reward,
                        category = try { QuestCategory.valueOf(it.category) } catch(e: Exception) { QuestCategory.WELLNESS },
                        isCompleted = it.is_completed,
                        isActive = it.is_active,
                        difficulty = try { QuestDifficulty.valueOf(it.difficulty) } catch(e: Exception) { QuestDifficulty.NORMAL }
                    )
                }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(quests)
                }
            }

            // 3. Completed Dates
            val completedDatesDb = postgrest["completed_dates"].select {
                filter { eq("user_id", userId) }
            }.decodeList<CompletedDateDb>()

            if (completedDatesDb.isNotEmpty()) {
                val dates = completedDatesDb.map { it.date }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.COMPLETED_DATES_JSON] = json.encodeToString(dates)
                }
            }

            // 4. Collected Cards
            val collectedCardsDb = postgrest["collected_cards"].select {
                filter { eq("user_id", userId) }
            }.decodeList<CollectedCardDb>()

            if (collectedCardsDb.isNotEmpty()) {
                val cards = collectedCardsDb.map { it.card_id }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.COLLECTED_CARDS_JSON] = json.encodeToString(cards)
                }
            }

            // 5. Jogging Sessions
            val joggingSessionsDb = postgrest["jogging_sessions"].select {
                filter { eq("user_id", userId) }
            }.decodeList<JoggingSessionDb>()

            if (joggingSessionsDb.isNotEmpty()) {
                val sessions = joggingSessionsDb.map {
                    JoggingSession(
                        id = it.id,
                        date = it.date,
                        distanceKm = it.distance_km,
                        durationSeconds = it.duration_seconds,
                        routePoints = try { json.decodeFromString<List<LatLngPoint>>(it.route_points) } catch(e: Exception) { emptyList() }
                    )
                }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.JOGGING_SESSIONS_JSON] = json.encodeToString(sessions)
                }
            }

            // 6. Daily Steps
            val dailyStepsDb = postgrest["daily_steps"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DailyStepsDb>()

            if (dailyStepsDb.isNotEmpty()) {
                val steps = dailyStepsDb.map { DailySteps(it.date, it.steps) }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.DAILY_STEPS_JSON] = json.encodeToString(steps)
                }
            }

            // 7. Additional Tasks
            val additionalTasksDb = postgrest["additional_tasks"].select {
                filter { eq("user_id", userId) }
            }.decodeList<AdditionalTaskDb>()

            if (additionalTasksDb.isNotEmpty()) {
                val tasks = additionalTasksDb.map {
                    AdditionalTask(
                        id = it.id,
                        title = it.title,
                        emoji = it.emoji,
                        isCompleted = it.is_completed,
                        xpReward = it.xp_reward,
                        createdDate = it.created_date
                    )
                }
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] = json.encodeToString(tasks)
                }
            }

            // 8. Today's calorie log & mood entry
            val todayStr = LocalDate.now().toString()

            val calorieLogDb = postgrest["calorie_log"].select {
                filter {
                    eq("user_id", userId)
                    eq("date", todayStr)
                }
            }.decodeList<CalorieLogDb>()

            val foodEntries = calorieLogDb.map {
                FoodEntry(it.id, it.name, it.calories, it.emoji)
            }
            context.dataStore.edit { prefs ->
                prefs[PrefsKeys.CALORIE_LOG_JSON] = json.encodeToString(foodEntries)
            }

            val moodEntryDb = postgrest["mood_entries"].select {
                filter {
                    eq("user_id", userId)
                    eq("date", todayStr)
                }
            }.decodeSingleOrNull<MoodEntryDb>()

            if (moodEntryDb != null) {
                val entry = MoodJournalEntry(moodEntryDb.emoji, moodEntryDb.label, moodEntryDb.score, moodEntryDb.journal_text)
                context.dataStore.edit { prefs ->
                    prefs[PrefsKeys.MOOD_ENTRY_JSON] = json.encodeToString(entry)
                }
            } else {
                context.dataStore.edit { prefs ->
                    prefs.remove(PrefsKeys.MOOD_ENTRY_JSON)
                }
            }

        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull data from cloud", e)
        }
    }
}
