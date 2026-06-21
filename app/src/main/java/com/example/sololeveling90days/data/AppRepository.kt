package com.example.sololeveling90days.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppRepository(private val context: Context) {

    val authRepository = AuthRepository()
    val syncManager = SyncManager(context, authRepository)
    val socialRepository = SocialRepository(authRepository)
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    val isGuestMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefsKeys.GUEST_MODE] ?: false
    }

    suspend fun setGuestMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.GUEST_MODE] = enabled
        }
    }

    fun syncProfile() {
        scope.launch {
            try {
                val profile = userProfile.first()
                syncManager.pushUserProfile(profile)
            } catch (e: Exception) {}
        }
    }

    fun syncQuests() {
        scope.launch {
            try {
                val list = quests.first()
                syncManager.pushQuests(list)
            } catch (e: Exception) {}
        }
    }

    fun syncCompletedDates() {
        scope.launch {
            try {
                val raw = context.dataStore.data.map { it[PrefsKeys.COMPLETED_DATES_JSON] ?: "[]" }.first()
                val list = json.decodeFromString<List<String>>(raw)
                syncManager.pushCompletedDates(list)
            } catch (e: Exception) {}
        }
    }

    fun syncCollectedCards() {
        scope.launch {
            try {
                val raw = context.dataStore.data.map { it[PrefsKeys.COLLECTED_CARDS_JSON] ?: "[]" }.first()
                val list = json.decodeFromString<List<String>>(raw)
                syncManager.pushCollectedCards(list)
            } catch (e: Exception) {}
        }
    }

    fun syncJoggingSessions() {
        scope.launch {
            try {
                val raw = context.dataStore.data.map { it[PrefsKeys.JOGGING_SESSIONS_JSON] ?: "[]" }.first()
                val list = json.decodeFromString<List<JoggingSession>>(raw)
                syncManager.pushJoggingSessions(list)
            } catch (e: Exception) {}
        }
    }

    fun syncDailySteps() {
        scope.launch {
            try {
                val raw = context.dataStore.data.map { it[PrefsKeys.DAILY_STEPS_JSON] ?: "[]" }.first()
                val list = json.decodeFromString<List<DailySteps>>(raw)
                syncManager.pushDailySteps(list)
            } catch (e: Exception) {}
        }
    }

    fun syncAdditionalTasks() {
        scope.launch {
            try {
                val raw = context.dataStore.data.map { it[PrefsKeys.ADDITIONAL_TASKS_JSON] ?: "[]" }.first()
                val list = json.decodeFromString<List<AdditionalTask>>(raw)
                syncManager.pushAdditionalTasks(list)
            } catch (e: Exception) {}
        }
    }

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    val userProfile: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            name = prefs[PrefsKeys.USER_NAME] ?: "",
            goal = prefs[PrefsKeys.USER_GOAL] ?: "",
            wakeTime = prefs[PrefsKeys.USER_WAKE_TIME] ?: "6:00 AM",
            streak = prefs[PrefsKeys.STREAK] ?: 0,
            totalXp = prefs[PrefsKeys.TOTAL_XP] ?: 0,
            hardMode = prefs[PrefsKeys.HARD_MODE] ?: false,
            dayNumber = prefs[PrefsKeys.DAY_NUMBER] ?: 1,
            startDate = prefs[PrefsKeys.START_DATE] ?: "",
            lastCompletedDate = prefs[PrefsKeys.LAST_COMPLETED_DATE] ?: "",
            completedDates = try {
                val raw = prefs[PrefsKeys.COMPLETED_DATES_JSON] ?: "[]"
                json.decodeFromString<List<String>>(raw)
            } catch (e: Exception) { emptyList() },
            gatePasses = prefs[PrefsKeys.GATE_PASSES] ?: 0,
            comebackAvailable = prefs[PrefsKeys.COMEBACK_AVAILABLE] ?: false,
            comebackDeadline = prefs[PrefsKeys.COMEBACK_DEADLINE] ?: "",
            isSubscribed = if (com.example.sololeveling90days.BuildConfig.DEBUG) true else (prefs[PrefsKeys.IS_SUBSCRIBED] ?: false)
        )
    }

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefsKeys.ONBOARDING_DONE] ?: false
    }

    val quests: Flow<List<Quest>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: return@map DEFAULT_QUESTS.take(6)
            json.decodeFromString<List<Quest>>(raw)
        } catch (e: Exception) { DEFAULT_QUESTS.take(6) }
    }

    val collectedCards: Flow<List<String>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.COLLECTED_CARDS_JSON] ?: "[]"
            json.decodeFromString<List<String>>(raw)
        } catch (e: Exception) { emptyList() }
    }

    val focusTimerWork: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PrefsKeys.FOCUS_TIMER_WORK] ?: 25
    }

    val focusTimerBreak: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PrefsKeys.FOCUS_TIMER_BREAK] ?: 5
    }

    val calorieLog: Flow<List<FoodEntry>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.CALORIE_LOG_JSON] ?: "[]"
            json.decodeFromString<List<FoodEntry>>(raw)
        } catch (e: Exception) { emptyList() }
    }

    val moodEntry: Flow<MoodJournalEntry?> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.MOOD_ENTRY_JSON] ?: return@map null
            json.decodeFromString<MoodJournalEntry>(raw)
        } catch (e: Exception) { null }
    }

    val amoledMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PrefsKeys.AMOLED_MODE] ?: false
    }

    // Returns XP bonus if login bonus awarded today, null otherwise
    suspend fun checkAndAwardLoginBonus(): Int? {
        var bonus: Int? = null
        context.dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val lastLogin = prefs[PrefsKeys.LAST_LOGIN_DATE] ?: ""
            if (lastLogin != today) {
                val xpBonus = (10..25).random()
                val currentXp = (prefs[PrefsKeys.TOTAL_XP] ?: 0) + xpBonus
                prefs[PrefsKeys.TOTAL_XP] = currentXp
                prefs[PrefsKeys.LAST_LOGIN_DATE] = today
                prefs[PrefsKeys.LAST_LOGIN_BONUS_XP] = xpBonus
                bonus = xpBonus
            }
        }
        return bonus
    }

    suspend fun completeOnboarding(name: String, goal: String, wakeTime: String) {
        val today = LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.ONBOARDING_DONE] = true
            prefs[PrefsKeys.USER_NAME] = name
            prefs[PrefsKeys.USER_GOAL] = goal
            prefs[PrefsKeys.USER_WAKE_TIME] = wakeTime
            prefs[PrefsKeys.START_DATE] = today
            prefs[PrefsKeys.DAY_NUMBER] = 1
            prefs[PrefsKeys.GATE_PASSES] = 0
            val questsForGoal = getQuestsForGoal(goal)
            prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(questsForGoal)
        }
        syncProfile()
        syncQuests()
    }

    suspend fun toggleQuestCompletion(questId: String, completed: Boolean) {
        val today = LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: return@edit
            val quests = try { json.decodeFromString<List<Quest>>(raw) } catch (e: Exception) { return@edit }
            val updated = quests.map { if (it.id == questId) it.copy(isCompleted = completed) else it }
            prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(updated)
        }
        recordQuestCompletion(questId, today, completed)
        syncQuests()
    }

    val questCompletions: Flow<Map<String, List<String>>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.QUEST_COMPLETIONS_JSON] ?: "{}"
            json.decodeFromString<Map<String, List<String>>>(raw)
        } catch (e: Exception) { emptyMap() }
    }

    suspend fun recordQuestCompletion(questId: String, date: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.QUEST_COMPLETIONS_JSON] ?: "{}"
            val map = try { json.decodeFromString<Map<String, List<String>>>(raw).mapValues { it.value.toMutableList() }.toMutableMap() } catch (e: Exception) { mutableMapOf() }
            val list = map.getOrPut(questId) { mutableListOf() }
            if (completed) {
                if (!list.contains(date)) list.add(date)
            } else {
                list.remove(date)
            }
            prefs[PrefsKeys.QUEST_COMPLETIONS_JSON] = json.encodeToString(map)
        }
    }


    suspend fun awardXP(xp: Int) {
        context.dataStore.edit { prefs ->
            val currentXp = (prefs[PrefsKeys.TOTAL_XP] ?: 0) + xp
            prefs[PrefsKeys.TOTAL_XP] = currentXp
        }
        syncProfile()
    }

    suspend fun completeDayIfAllDone() {
        context.dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val lastDate = prefs[PrefsKeys.LAST_COMPLETED_DATE] ?: ""
            if (lastDate == today) return@edit

            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: return@edit
            val quests = try { json.decodeFromString<List<Quest>>(raw) } catch (e: Exception) { return@edit }
            val allDone = quests.filter { it.isActive }.all { it.isCompleted }
            if (!allDone) return@edit

            val yesterday = LocalDate.now().minusDays(1).toString()
            val currentStreak = prefs[PrefsKeys.STREAK] ?: 0
            val newStreak = if (lastDate == yesterday || lastDate.isEmpty()) currentStreak + 1 else 1
            prefs[PrefsKeys.STREAK] = newStreak
            prefs[PrefsKeys.LAST_COMPLETED_DATE] = today

            // Award gate pass every 10 streak days
            if (newStreak > 0 && newStreak % 10 == 0) {
                val passes = (prefs[PrefsKeys.GATE_PASSES] ?: 0)
                if (passes < 2) prefs[PrefsKeys.GATE_PASSES] = passes + 1
            }

            val completedRaw = prefs[PrefsKeys.COMPLETED_DATES_JSON] ?: "[]"
            val completedDates = try { Json.decodeFromString<MutableList<String>>(completedRaw) } catch (e: Exception) { mutableListOf() }
            if (!completedDates.contains(today)) completedDates.add(today)
            prefs[PrefsKeys.COMPLETED_DATES_JSON] = Json.encodeToString(completedDates)

            val dayNum = prefs[PrefsKeys.DAY_NUMBER] ?: 1
            prefs[PrefsKeys.DAY_NUMBER] = (dayNum + 1).coerceAtMost(90)

            val totalXp = (prefs[PrefsKeys.TOTAL_XP] ?: 0) + 200
            prefs[PrefsKeys.TOTAL_XP] = totalXp
        }
        syncProfile()
        syncCompletedDates()
    }

    suspend fun setHardMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.HARD_MODE] = enabled
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: ""
            if (raw.isNotEmpty()) {
                try {
                    val quests = json.decodeFromString<List<Quest>>(raw).toMutableList()
                    if (enabled) {
                        if (quests.none { it.id == "hardmode_trial" }) {
                            quests.add(
                                Quest(
                                    id = "hardmode_trial",
                                    title = "\uD83D\uDC80 [HARD MODE] The Hunter's Trial",
                                    description = "Perform a 3-minute cold shower & 50 pushups. Absolute discipline.",
                                    xpReward = 150,
                                    category = QuestCategory.FITNESS,
                                    isCompleted = false,
                                    difficulty = QuestDifficulty.LEGENDARY,
                                    isActive = true
                                )
                            )
                        }
                    } else {
                        quests.removeAll { it.id == "hardmode_trial" }
                    }
                    prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(quests)
                } catch (e: Exception) {}
            }
        }
        syncProfile()
        syncQuests()
    }

    suspend fun collectCard(cardId: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.COLLECTED_CARDS_JSON] ?: "[]"
            val collected = try { Json.decodeFromString<MutableList<String>>(raw) } catch (e: Exception) { mutableListOf() }
            if (!collected.contains(cardId)) {
                collected.add(cardId)
                prefs[PrefsKeys.COLLECTED_CARDS_JSON] = Json.encodeToString(collected)
            }
        }
        syncCollectedCards()
    }

    suspend fun saveFocusTimerSettings(workMinutes: Int, breakMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.FOCUS_TIMER_WORK] = workMinutes
            prefs[PrefsKeys.FOCUS_TIMER_BREAK] = breakMinutes
        }
    }

    suspend fun saveCalorieLog(log: List<FoodEntry>) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.CALORIE_LOG_JSON] = json.encodeToString(log)
        }
        scope.launch {
            syncManager.pushCalorieLog(log, LocalDate.now().toString())
        }
    }

    suspend fun saveMoodEntry(entry: MoodJournalEntry?) {
        context.dataStore.edit { prefs ->
            if (entry == null) {
                prefs.remove(PrefsKeys.MOOD_ENTRY_JSON)
            } else {
                prefs[PrefsKeys.MOOD_ENTRY_JSON] = json.encodeToString(entry)
            }
        }
        scope.launch {
            syncManager.pushMoodEntry(entry, LocalDate.now().toString())
        }
    }

    suspend fun addCustomQuest(quest: Quest): Boolean {
        var added = false
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: "[]"
            val quests = try { Json.decodeFromString<MutableList<Quest>>(raw) } catch (e: Exception) { mutableListOf() }
            quests.add(quest)
            prefs[PrefsKeys.QUESTS_JSON] = Json.encodeToString(quests)
            added = true
        }
        syncQuests()
        return added
    }

    suspend fun removeQuest(questId: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: return@edit
            val quests = try { Json.decodeFromString<MutableList<Quest>>(raw) } catch (e: Exception) { return@edit }
            quests.removeAll { it.id == questId }
            prefs[PrefsKeys.QUESTS_JSON] = Json.encodeToString(quests)
        }
        syncQuests()
    }

    suspend fun verifyStreakAndPenalties(): String? {
        var systemMessage: String? = null
        context.dataStore.edit { prefs ->
            val todayStr = LocalDate.now().toString()
            val lastChecked = prefs[PrefsKeys.LAST_CHECKED_DATE] ?: ""
            
            if (lastChecked.isEmpty()) {
                // First initialization
                prefs[PrefsKeys.LAST_CHECKED_DATE] = todayStr
                return@edit
            }
            
            if (lastChecked == todayStr) {
                // Already checked today
                return@edit
            }
            
            // Date changed! Perform daily transition
            prefs[PrefsKeys.LAST_CHECKED_DATE] = todayStr
            
            val lastDateStr = prefs[PrefsKeys.LAST_COMPLETED_DATE] ?: ""
            if (lastDateStr.isNotEmpty()) {
                try {
                    val today = LocalDate.now()
                    val lastDate = LocalDate.parse(lastDateStr)
                    if (lastDate.isBefore(today.minusDays(1))) {
                        // User missed yesterday's tasks!
                        val raw = prefs[PrefsKeys.QUESTS_JSON] ?: ""
                        val quests = if (raw.isNotEmpty()) {
                            try { json.decodeFromString<List<Quest>>(raw) } catch (e: Exception) { emptyList() }
                        } else emptyList()
                        
                        val allDone = quests.isNotEmpty() && quests.filter { it.isActive }.all { it.isCompleted }
                        if (allDone) {
                            // Retroactively complete the day
                            val yesterday = today.minusDays(1).toString()
                            val currentStreak = prefs[PrefsKeys.STREAK] ?: 0
                            val newStreak = currentStreak + 1
                            prefs[PrefsKeys.STREAK] = newStreak
                            prefs[PrefsKeys.LAST_COMPLETED_DATE] = yesterday
                            
                            if (newStreak > 0 && newStreak % 10 == 0) {
                                val passes = (prefs[PrefsKeys.GATE_PASSES] ?: 0)
                                if (passes < 2) prefs[PrefsKeys.GATE_PASSES] = passes + 1
                            }
                            
                            val completedRaw = prefs[PrefsKeys.COMPLETED_DATES_JSON] ?: "[]"
                            val completedDates = try { Json.decodeFromString<MutableList<String>>(completedRaw) } catch (e: Exception) { mutableListOf() }
                            if (!completedDates.contains(yesterday)) completedDates.add(yesterday)
                            prefs[PrefsKeys.COMPLETED_DATES_JSON] = Json.encodeToString(completedDates)
                            
                            val dayNum = prefs[PrefsKeys.DAY_NUMBER] ?: 1
                            prefs[PrefsKeys.DAY_NUMBER] = (dayNum + 1).coerceAtMost(90)
                            
                            val totalXp = (prefs[PrefsKeys.TOTAL_XP] ?: 0) + 200
                            prefs[PrefsKeys.TOTAL_XP] = totalXp
                        } else {
                            // Apply penalty
                            val currentStreak = prefs[PrefsKeys.STREAK] ?: 0
                            if (currentStreak > 0) {
                                val passes = prefs[PrefsKeys.GATE_PASSES] ?: 0
                                if (passes > 0) {
                                    prefs[PrefsKeys.GATE_PASSES] = passes - 1
                                    systemMessage = "gate_pass_used"
                                } else {
                                    prefs[PrefsKeys.COMEBACK_AVAILABLE] = true
                                    val deadline = LocalDateTime.now().plusHours(2).toString()
                                    prefs[PrefsKeys.COMEBACK_DEADLINE] = deadline
                                    prefs[PrefsKeys.STREAK] = 0
                                    
                                    val hardMode = prefs[PrefsKeys.HARD_MODE] ?: false
                                    if (hardMode) {
                                        val xp = prefs[PrefsKeys.TOTAL_XP] ?: 0
                                        prefs[PrefsKeys.TOTAL_XP] = (xp - 150).coerceAtLeast(0)
                                    }
                                    systemMessage = "comeback"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
            
            // Reset quests to unchecked for the new day
            val rawQuests = prefs[PrefsKeys.QUESTS_JSON] ?: ""
            if (rawQuests.isNotEmpty()) {
                try {
                    val quests = json.decodeFromString<List<Quest>>(rawQuests).toMutableList()
                    val isHardMode = prefs[PrefsKeys.HARD_MODE] ?: false
                    
                    // Inject or update hardmode trial quest
                    if (isHardMode) {
                        if (quests.none { it.id == "hardmode_trial" }) {
                            quests.add(
                                Quest(
                                    id = "hardmode_trial",
                                    title = "\uD83D\uDC80 [HARD MODE] The Hunter's Trial",
                                    description = "Perform a 3-minute cold shower & 50 pushups. Absolute discipline.",
                                    xpReward = 150,
                                    category = QuestCategory.FITNESS,
                                    isCompleted = false,
                                    difficulty = QuestDifficulty.LEGENDARY,
                                    isActive = true
                                )
                            )
                        }
                    }
                    
                    val resetQuests = quests.map {
                        if (it.id == "hardmode_trial") {
                            it.copy(isCompleted = false, isActive = isHardMode)
                        } else {
                            it.copy(isCompleted = false)
                        }
                    }
                    prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(resetQuests)
                } catch (e: Exception) {}
            }
            
            // Clear daily temporary logs
            prefs.remove(PrefsKeys.CALORIE_LOG_JSON)
            prefs.remove(PrefsKeys.MOOD_ENTRY_JSON)
        }
        if (systemMessage != null) {
            syncProfile()
            syncQuests()
        }
        return systemMessage
    }

    suspend fun completeComebackQuest() {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.COMEBACK_AVAILABLE] = false
            prefs[PrefsKeys.COMEBACK_DEADLINE] = ""
            // Restore 50% streak - give them 5 back as a token
            val cur = prefs[PrefsKeys.STREAK] ?: 0
            prefs[PrefsKeys.STREAK] = cur + 5
            prefs[PrefsKeys.LAST_COMPLETED_DATE] = LocalDate.now().toString()
        }
        syncProfile()
    }

    suspend fun dismissComeback() {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.COMEBACK_AVAILABLE] = false
            prefs[PrefsKeys.COMEBACK_DEADLINE] = ""
        }
        syncProfile()
    }

    suspend fun applyHardModePenalty() {
        context.dataStore.edit { prefs ->
            val xp = prefs[PrefsKeys.TOTAL_XP] ?: 0
            prefs[PrefsKeys.TOTAL_XP] = (xp - 150).coerceAtLeast(0)
            prefs[PrefsKeys.STREAK] = 0
        }
        syncProfile()
    }

    suspend fun setSubscribed(subscribed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.IS_SUBSCRIBED] = subscribed
        }
    }

    val timerTargetEpoch: Flow<Long> = context.dataStore.data.map { it[PrefsKeys.TIMER_TARGET_EPOCH] ?: 0L }
    val timerIsRunning: Flow<Boolean> = context.dataStore.data.map { it[PrefsKeys.TIMER_IS_RUNNING] ?: false }
    val timerMode: Flow<String> = context.dataStore.data.map { it[PrefsKeys.TIMER_MODE] ?: "WORK" }
    val timerSavedRemaining: Flow<Long> = context.dataStore.data.map { it[PrefsKeys.TIMER_SAVED_REMAINING] ?: 0L }

    suspend fun saveTimerState(isRunning: Boolean, targetEpoch: Long, mode: String, remaining: Long) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.TIMER_IS_RUNNING] = isRunning
            prefs[PrefsKeys.TIMER_TARGET_EPOCH] = targetEpoch
            prefs[PrefsKeys.TIMER_MODE] = mode
            prefs[PrefsKeys.TIMER_SAVED_REMAINING] = remaining
        }
    }

    suspend fun resetApp() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    // ─────────────────────────────────────────────────
    //  Additional Tasks (bonus tasks — don't affect streak)
    // ─────────────────────────────────────────────────

    val additionalTasks: Flow<List<AdditionalTask>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] ?: "[]"
            json.decodeFromString<List<AdditionalTask>>(raw)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addAdditionalTask(task: AdditionalTask) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] ?: "[]"
            val tasks = try { json.decodeFromString<MutableList<AdditionalTask>>(raw) } catch (e: Exception) { mutableListOf() }
            tasks.add(task)
            prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] = json.encodeToString(tasks)
        }
        syncAdditionalTasks()
    }

    suspend fun toggleAdditionalTask(taskId: String, completed: Boolean) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] ?: return@edit
            val tasks = try { json.decodeFromString<MutableList<AdditionalTask>>(raw) } catch (e: Exception) { return@edit }
            val updated = tasks.map { if (it.id == taskId) it.copy(isCompleted = completed) else it }
            prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] = json.encodeToString(updated)
            // Award XP when completing
            if (completed) {
                val xpReward = tasks.firstOrNull { it.id == taskId }?.xpReward ?: 30
                val currentXp = (prefs[PrefsKeys.TOTAL_XP] ?: 0) + xpReward
                prefs[PrefsKeys.TOTAL_XP] = currentXp
            }
        }
        syncAdditionalTasks()
        if (completed) syncProfile()
    }

    suspend fun deleteAdditionalTask(taskId: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] ?: return@edit
            val tasks = try { json.decodeFromString<MutableList<AdditionalTask>>(raw) } catch (e: Exception) { return@edit }
            tasks.removeAll { it.id == taskId }
            prefs[PrefsKeys.ADDITIONAL_TASKS_JSON] = json.encodeToString(tasks)
        }
        syncAdditionalTasks()
    }

    /** Returns how many basic tasks are still incomplete today (used by notification worker) */
    suspend fun countIncompleteBasicTasks(): Int {
        var count = 0
        context.dataStore.data.map { prefs ->
            try {
                val raw = prefs[PrefsKeys.QUESTS_JSON] ?: "[]"
                val quests = json.decodeFromString<List<Quest>>(raw)
                quests.count { it.isActive && !it.isCompleted }
            } catch (e: Exception) { 0 }
        }.collect { count = it; }
        return count
    }

    /** Returns true if all basic tasks are done today */
    suspend fun areAllBasicTasksDone(): Boolean {
        var allDone = false
        context.dataStore.data.map { prefs ->
            try {
                val raw = prefs[PrefsKeys.QUESTS_JSON] ?: "[]"
                val quests = json.decodeFromString<List<Quest>>(raw)
                val activeQuests = quests.filter { it.isActive }
                activeQuests.isNotEmpty() && activeQuests.all { it.isCompleted }
            } catch (e: Exception) { false }
        }.collect { allDone = it }
        return allDone
    }

    val joggingSessions: Flow<List<JoggingSession>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.JOGGING_SESSIONS_JSON] ?: "[]"
            json.decodeFromString<List<JoggingSession>>(raw)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addJoggingSession(session: JoggingSession) {
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.JOGGING_SESSIONS_JSON] ?: "[]"
            val sessions = try { json.decodeFromString<MutableList<JoggingSession>>(raw) } catch (e: Exception) { mutableListOf() }
            sessions.add(session)
            prefs[PrefsKeys.JOGGING_SESSIONS_JSON] = json.encodeToString(sessions)
        }
        syncJoggingSessions()
    }

    val dailySteps: Flow<List<DailySteps>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.DAILY_STEPS_JSON] ?: "[]"
            json.decodeFromString<List<DailySteps>>(raw)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun updateStepsForToday(sensorSteps: Int) {
        context.dataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            
            // If the day changed, reset the baseline steps
            var baseline = prefs[PrefsKeys.STEPS_START_OF_DAY]
            if (baseline == null) {
                prefs[PrefsKeys.STEPS_START_OF_DAY] = sensorSteps
                baseline = sensorSteps
            }
            
            val todaySteps = (sensorSteps - baseline).coerceAtLeast(0)
            
            val raw = prefs[PrefsKeys.DAILY_STEPS_JSON] ?: "[]"
            val list = try { json.decodeFromString<MutableList<DailySteps>>(raw) } catch (e: Exception) { mutableListOf() }
            
            val index = list.indexOfFirst { it.date == today }
            if (index >= 0) {
                list[index] = list[index].copy(steps = todaySteps)
            } else {
                list.add(DailySteps(today, todaySteps))
            }
            prefs[PrefsKeys.DAILY_STEPS_JSON] = json.encodeToString(list)
        }
        syncDailySteps()
    }
}


