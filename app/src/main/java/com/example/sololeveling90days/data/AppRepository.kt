package com.example.sololeveling90days.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.MutablePreferences
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
            isSubscribed = if (com.example.sololeveling90days.BuildConfig.DEBUG) true else (prefs[PrefsKeys.IS_SUBSCRIBED] ?: false),
            isPenaltyActive = prefs[PrefsKeys.PENALTY_ACTIVE] ?: false,
            penaltyReason = prefs[PrefsKeys.PENALTY_REASON] ?: "",
            weightKg = prefs[PrefsKeys.USER_WEIGHT] ?: 70f,
            str = prefs[PrefsKeys.STR_STAT] ?: 10,
            agi = prefs[PrefsKeys.AGI_STAT] ?: 10,
            int = prefs[PrefsKeys.INT_STAT] ?: 10,
            vit = prefs[PrefsKeys.VIT_STAT] ?: 10,
            sen = prefs[PrefsKeys.SEN_STAT] ?: 10,
            unallocatedPoints = prefs[PrefsKeys.UNALLOCATED_POINTS] ?: 0,
            avatarResId = prefs[PrefsKeys.AVATAR_RES_ID] ?: "avatar_monarch",
            squatsCount = prefs[PrefsKeys.SQUATS_COUNT] ?: 0,
            joggingDistanceTotal = prefs[PrefsKeys.JOGGING_DISTANCE_TOTAL] ?: 0f,
            focusSessionsCount = prefs[PrefsKeys.FOCUS_SESSIONS_COUNT] ?: 0,
            meditationSessionsCount = prefs[PrefsKeys.MEDITATION_SESSIONS_COUNT] ?: 0,
            moodJournalsCount = prefs[PrefsKeys.MOOD_JOURNALS_COUNT] ?: 0,
            gold = prefs[PrefsKeys.GOLD_BALANCE] ?: 0,
            activeTitle = prefs[PrefsKeys.ACTIVE_TITLE] ?: "",
            unlockedTitles = try {
                val raw = prefs[PrefsKeys.UNLOCKED_TITLES_JSON] ?: "[]"
                json.decodeFromString<List<String>>(raw)
            } catch (e: Exception) { emptyList() },
            inventory = try {
                val raw = prefs[PrefsKeys.INVENTORY_JSON] ?: "{}"
                json.decodeFromString<Map<String, Int>>(raw)
            } catch (e: Exception) { emptyMap() },
            equippedGear = try {
                val raw = prefs[PrefsKeys.EQUIPPED_GEAR_JSON] ?: "[]"
                json.decodeFromString<List<String>>(raw)
            } catch (e: Exception) { emptyList() },
            gender = prefs[PrefsKeys.USER_GENDER] ?: "Male",
            heightCm = prefs[PrefsKeys.USER_HEIGHT] ?: 175f,
            ageYears = prefs[PrefsKeys.USER_AGE] ?: 25,
            fitnessGoal = prefs[PrefsKeys.USER_FITNESS_GOAL] ?: "Maintenance"
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
            val questsForGoal = getQuestsForGoal(goal, 1)
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


    private fun MutablePreferences.addXpAndCheckLevelUp(xp: Int) {
        val oldXp = this[PrefsKeys.TOTAL_XP] ?: 0
        val oldLevel = levelFromXp(oldXp)
        val newXp = (oldXp + xp).coerceAtLeast(0)
        this[PrefsKeys.TOTAL_XP] = newXp

        val newLevel = levelFromXp(newXp)
        if (newLevel > oldLevel) {
            val diff = newLevel - oldLevel
            val currentPoints = this[PrefsKeys.UNALLOCATED_POINTS] ?: 0
            this[PrefsKeys.UNALLOCATED_POINTS] = currentPoints + (diff * 5)

            val currentVit = this[PrefsKeys.VIT_STAT] ?: 10
            this[PrefsKeys.VIT_STAT] = currentVit + diff

            SoundManager.playLevelUp()
            SoundManager.speak("System Message: You have leveled up! Five stat points have been granted.")
        }
    }

    suspend fun awardXP(xp: Int) {
        context.dataStore.edit { prefs ->
            prefs.addXpAndCheckLevelUp(xp)
        }
        syncProfile()
    }

    suspend fun allocateStatPoint(statKey: androidx.datastore.preferences.core.Preferences.Key<Int>) {
        context.dataStore.edit { prefs ->
            val unallocated = prefs[PrefsKeys.UNALLOCATED_POINTS] ?: 0
            if (unallocated > 0) {
                val cap = when (statKey) {
                    PrefsKeys.STR_STAT -> 10 + ((prefs[PrefsKeys.SQUATS_COUNT] ?: 0) / 50)
                    PrefsKeys.AGI_STAT -> 10 + ((prefs[PrefsKeys.JOGGING_DISTANCE_TOTAL] ?: 0f) / 2f).toInt()
                    PrefsKeys.INT_STAT -> 10 + ((prefs[PrefsKeys.FOCUS_SESSIONS_COUNT] ?: 0) / 2)
                    PrefsKeys.VIT_STAT -> 10 + (levelFromXp(prefs[PrefsKeys.TOTAL_XP] ?: 0) * 2)
                    PrefsKeys.SEN_STAT -> 10 + (((prefs[PrefsKeys.MEDITATION_SESSIONS_COUNT] ?: 0) + (prefs[PrefsKeys.MOOD_JOURNALS_COUNT] ?: 0)) / 3)
                    else -> Int.MAX_VALUE
                }

                val currentVal = prefs[statKey] ?: 10
                if (currentVal < cap) {
                    prefs[PrefsKeys.UNALLOCATED_POINTS] = unallocated - 1
                    prefs[statKey] = currentVal + 1
                    SoundManager.playQuestDone()
                } else {
                    SoundManager.playLockoutAlert()
                    val statName = when (statKey) {
                        PrefsKeys.STR_STAT -> "Strength"
                        PrefsKeys.AGI_STAT -> "Agility"
                        PrefsKeys.INT_STAT -> "Intelligence"
                        PrefsKeys.VIT_STAT -> "Vitality"
                        PrefsKeys.SEN_STAT -> "Sense"
                        else -> "Attribute"
                    }
                    SoundManager.speak("Warning: $statName limit reached. Complete training to expand capacity.")
                }
            }
        }
        syncProfile()
    }

    suspend fun updateAvatar(avatarId: String) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.AVATAR_RES_ID] = avatarId
        }
        syncProfile()
    }

    suspend fun incrementStat(statKey: androidx.datastore.preferences.core.Preferences.Key<Int>, amount: Int = 1) {
        context.dataStore.edit { prefs ->
            val currentVal = prefs[statKey] ?: 10
            prefs[statKey] = currentVal + amount
        }
        syncProfile()
    }

    suspend fun completeFocusSession() {
        context.dataStore.edit { prefs ->
            val currentCount = prefs[PrefsKeys.FOCUS_SESSIONS_COUNT] ?: 0
            val newCount = currentCount + 1
            prefs[PrefsKeys.FOCUS_SESSIONS_COUNT] = newCount

            val intCap = 10 + (newCount / 2)
            val currentInt = prefs[PrefsKeys.INT_STAT] ?: 10
            if (currentInt < intCap) {
                prefs[PrefsKeys.INT_STAT] = currentInt + 1
                SoundManager.speak("System Message: Your Intelligence has increased!")
            }

            prefs.addXpAndCheckLevelUp(50)
            SoundManager.playQuestDone()
            SoundManager.speakQueue("Focus session complete. Intelligence and XP points awarded.")
        }
        syncProfile()
    }

    suspend fun completeMeditationSession(durationMinutes: Int) {
        context.dataStore.edit { prefs ->
            val currentCount = prefs[PrefsKeys.MEDITATION_SESSIONS_COUNT] ?: 0
            val newCount = currentCount + 1
            prefs[PrefsKeys.MEDITATION_SESSIONS_COUNT] = newCount

            val moodJournals = prefs[PrefsKeys.MOOD_JOURNALS_COUNT] ?: 0
            val senCap = 10 + ((newCount + moodJournals) / 3)
            val currentSen = prefs[PrefsKeys.SEN_STAT] ?: 10
            if (currentSen < senCap) {
                prefs[PrefsKeys.SEN_STAT] = currentSen + 1
                SoundManager.speak("System Message: Your Sense has increased!")
            }

            prefs.addXpAndCheckLevelUp(durationMinutes * 10)
            SoundManager.playQuestDone()
            SoundManager.speakQueue("Meditation complete. Sense and XP points awarded.")
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

            prefs.addXpAndCheckLevelUp(200)
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

                val currentJournals = prefs[PrefsKeys.MOOD_JOURNALS_COUNT] ?: 0
                val newJournals = currentJournals + 1
                prefs[PrefsKeys.MOOD_JOURNALS_COUNT] = newJournals

                val meditationSessions = prefs[PrefsKeys.MEDITATION_SESSIONS_COUNT] ?: 0
                val senCap = 10 + ((meditationSessions + newJournals) / 3)
                val currentSen = prefs[PrefsKeys.SEN_STAT] ?: 10
                if (currentSen < senCap) {
                    prefs[PrefsKeys.SEN_STAT] = currentSen + 1
                    SoundManager.speak("System Message: Your Sense has increased!")
                }

                prefs.addXpAndCheckLevelUp(30)
                SoundManager.playQuestDone()
                SoundManager.speakQueue("Mood logged. Sense and XP points awarded.")
            }
        }
        scope.launch {
            syncManager.pushMoodEntry(entry, LocalDate.now().toString())
        }
        syncProfile()
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

    suspend fun addExerciseQuest(exerciseType: ExerciseType, targetReps: Int): Boolean {
        val definition = getExerciseDefinition(exerciseType)
        val quest = createExerciseQuest(definition, targetReps)
        return addCustomQuest(quest)
    }

    suspend fun verifyExerciseQuest(questId: String, repsCompleted: Int, wasVerified: Boolean) {
        val today = LocalDate.now().toString()
        var xpToAward = 0
        context.dataStore.edit { prefs ->
            val raw = prefs[PrefsKeys.QUESTS_JSON] ?: return@edit
            val quests = try { json.decodeFromString<List<Quest>>(raw) } catch (e: Exception) { return@edit }
            val updated = quests.map {
                if (it.id == questId) {
                    val baseXp = it.xpReward
                    val finalXp = if (wasVerified) (baseXp * 1.5).toInt() else baseXp
                    if (!it.isCompleted) {
                        xpToAward = finalXp
                    }
                    it.copy(
                        isCompleted = true,
                        isVerified = wasVerified,
                        xpReward = finalXp
                    )
                } else {
                    it
                }
            }
            prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(updated)

            val quest = quests.firstOrNull { it.id == questId }
            if (quest != null) {
                if (quest.exerciseType == ExerciseType.SQUATS) {
                    val currentSquats = prefs[PrefsKeys.SQUATS_COUNT] ?: 0
                    prefs[PrefsKeys.SQUATS_COUNT] = currentSquats + repsCompleted

                    val strCap = 10 + ((currentSquats + repsCompleted) / 50)
                    val currentStr = prefs[PrefsKeys.STR_STAT] ?: 10
                    if (currentStr < strCap) {
                        prefs[PrefsKeys.STR_STAT] = currentStr + 1
                        SoundManager.speak("System Message: Your Strength has increased!")
                    }
                } else if (quest.category == QuestCategory.FITNESS) {
                    val currentStr = prefs[PrefsKeys.STR_STAT] ?: 10
                    val strCap = 10 + ((prefs[PrefsKeys.SQUATS_COUNT] ?: 0) / 50)
                    if (currentStr < strCap) {
                        prefs[PrefsKeys.STR_STAT] = currentStr + 1
                        SoundManager.speak("System Message: Your Strength has increased!")
                    }
                }
            }
        }
        if (xpToAward > 0) {
            awardXP(xpToAward)
        }
        recordQuestCompletion(questId, today, true)
        syncQuests()
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
            prefs[PrefsKeys.ACTIVE_DUNGEONS_JSON] = json.encodeToString(INITIAL_DUNGEONS)
            
            val lastDateStr = prefs[PrefsKeys.LAST_COMPLETED_DATE] ?: ""
            if (lastDateStr.isNotEmpty()) {
                try {
                    val today = LocalDate.now()
                    val lastDate = LocalDate.parse(lastDateStr)
                    if (lastDate.isBefore(today.minusDays(1))) {
                        // User missed yesterday's tasks (or multiple days)!
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

                            prefs.addXpAndCheckLevelUp(200)
                        } else {
                            // Calculate exact consecutive inactive days
                            val daysMissed = java.time.temporal.ChronoUnit.DAYS.between(lastDate, today).toInt() - 1
                            val currentStreak = prefs[PrefsKeys.STREAK] ?: 0
                            val passes = prefs[PrefsKeys.GATE_PASSES] ?: 0

                            // Try to protect with Gate Passes if possible
                            if (passes >= daysMissed && daysMissed > 0) {
                                prefs[PrefsKeys.GATE_PASSES] = passes - daysMissed
                                systemMessage = "gate_pass_used"
                            } else {
                                // Streak is lost!
                                prefs[PrefsKeys.STREAK] = 0

                                // Calculate scaled XP penalty
                                val hardMode = prefs[PrefsKeys.HARD_MODE] ?: false
                                val dayNum = prefs[PrefsKeys.DAY_NUMBER] ?: 1
                                var xpDeduction = 0

                                for (i in 1..daysMissed) {
                                    val missedDayNum = (dayNum - i).coerceAtLeast(1)
                                    if (hardMode) {
                                        // Hard Mode: 150 XP per day, 300 XP if it was a Boss Day
                                        val isBoss = missedDayNum > 0 && (missedDayNum % 7 == 0 || missedDayNum == 15 || missedDayNum == 30 || missedDayNum == 60 || missedDayNum == 90)
                                        xpDeduction += if (isBoss) 300 else 150
                                    } else {
                                        // Standard Mode: 50 XP per missed day
                                        xpDeduction += 50
                                    }
                                }

                                val currentXp = prefs[PrefsKeys.TOTAL_XP] ?: 0
                                prefs[PrefsKeys.TOTAL_XP] = (currentXp - xpDeduction).coerceAtLeast(0)

                                // Trigger Penalty Mode Lockout & Comeback screen
                                prefs[PrefsKeys.PENALTY_ACTIVE] = true
                                prefs[PrefsKeys.PENALTY_REASON] = "You were inactive for $daysMissed day(s). The System has locked your access."
                                prefs[PrefsKeys.COMEBACK_AVAILABLE] = true
                                val deadline = LocalDateTime.now().plusHours(2).toString()
                                prefs[PrefsKeys.COMEBACK_DEADLINE] = deadline
                                systemMessage = "comeback"
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
            
            // Reset and regenerate quests for the new day
            val goal = prefs[PrefsKeys.USER_GOAL] ?: ""
            val dayNum = prefs[PrefsKeys.DAY_NUMBER] ?: 1
            val quests = getQuestsForGoal(goal, dayNum).toMutableList()
            val isHardMode = prefs[PrefsKeys.HARD_MODE] ?: false

            if (isHardMode) {
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
            prefs[PrefsKeys.QUESTS_JSON] = json.encodeToString(quests)

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
            // When comeback is dismissed/skipped, penalty lockout becomes active
            prefs[PrefsKeys.PENALTY_ACTIVE] = true
            prefs[PrefsKeys.PENALTY_REASON] = "Comeback quest skipped. The System has locked your access."
        }
        syncProfile()
    }

    suspend fun completePenaltyQuest() {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.PENALTY_ACTIVE] = false
            prefs[PrefsKeys.PENALTY_REASON] = ""
            // Clear any lingering comeback state
            prefs[PrefsKeys.COMEBACK_AVAILABLE] = false
            prefs[PrefsKeys.COMEBACK_DEADLINE] = ""
            // Reset streak to 0, start fresh
            prefs[PrefsKeys.STREAK] = 0
            // Set last completed date to today to prevent immediate re-trigger
            prefs[PrefsKeys.LAST_COMPLETED_DATE] = LocalDate.now().toString()
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
                prefs.addXpAndCheckLevelUp(xpReward)
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

            val currentDistance = prefs[PrefsKeys.JOGGING_DISTANCE_TOTAL] ?: 0f
            val newDistance = currentDistance + session.distanceKm
            prefs[PrefsKeys.JOGGING_DISTANCE_TOTAL] = newDistance

            val agiCap = 10 + (newDistance / 2f).toInt()
            val currentAgi = prefs[PrefsKeys.AGI_STAT] ?: 10
            if (currentAgi < agiCap) {
                prefs[PrefsKeys.AGI_STAT] = currentAgi + 1
                SoundManager.speak("System Message: Your Agility has increased!")
            }
        }
        // Award XP for the activity session
        if (session.xpEarned > 0) {
            awardXP(session.xpEarned)
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

    val shadowArmy: Flow<List<ShadowHunter>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.SHADOW_ARMY_JSON]
            if (raw == null) {
                INITIAL_SHADOWS
            } else {
                json.decodeFromString<List<ShadowHunter>>(raw)
            }
        } catch (e: Exception) { INITIAL_SHADOWS }
    }

    val activeDungeons: Flow<List<DungeonRaid>> = context.dataStore.data.map { prefs ->
        try {
            val raw = prefs[PrefsKeys.ACTIVE_DUNGEONS_JSON]
            if (raw == null) {
                INITIAL_DUNGEONS
            } else {
                json.decodeFromString<List<DungeonRaid>>(raw)
            }
        } catch (e: Exception) { INITIAL_DUNGEONS }
    }

    suspend fun buyShopItem(item: ShopItem): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentGold = prefs[PrefsKeys.GOLD_BALANCE] ?: 0
            if (currentGold >= item.cost) {
                prefs[PrefsKeys.GOLD_BALANCE] = currentGold - item.cost
                success = true

                when (item.id) {
                    "shop_gate_pass" -> {
                        val currentPasses = prefs[PrefsKeys.GATE_PASSES] ?: 0
                        prefs[PrefsKeys.GATE_PASSES] = currentPasses + 1
                    }
                    "shop_xp_booster" -> {
                        val currentExpiry = prefs[PrefsKeys.XP_BOOSTER_EXPIRY] ?: 0L
                        val now = System.currentTimeMillis()
                        val newExpiry = Math.max(currentExpiry, now) + 24 * 60 * 60 * 1000L
                        prefs[PrefsKeys.XP_BOOSTER_EXPIRY] = newExpiry
                    }
                    "shop_stat_reset" -> {
                        val currentStr = prefs[PrefsKeys.STR_STAT] ?: 10
                        val currentAgi = prefs[PrefsKeys.AGI_STAT] ?: 10
                        val currentInt = prefs[PrefsKeys.INT_STAT] ?: 10
                        val currentSen = prefs[PrefsKeys.SEN_STAT] ?: 10

                        val allocatedPoints = (currentStr - 10) + (currentAgi - 10) + (currentInt - 10) + (currentSen - 10)
                        if (allocatedPoints > 0) {
                            val unallocated = prefs[PrefsKeys.UNALLOCATED_POINTS] ?: 0
                            prefs[PrefsKeys.UNALLOCATED_POINTS] = unallocated + allocatedPoints

                            prefs[PrefsKeys.STR_STAT] = 10
                            prefs[PrefsKeys.AGI_STAT] = 10
                            prefs[PrefsKeys.INT_STAT] = 10
                            prefs[PrefsKeys.SEN_STAT] = 10
                        }
                    }
                    "shop_monarch_elixir" -> {
                        val rawInv = prefs[PrefsKeys.INVENTORY_JSON] ?: "{}"
                        val inv = try { json.decodeFromString<Map<String, Int>>(rawInv).toMutableMap() } catch(e: Exception) { mutableMapOf() }
                        val currentCount = inv.getOrDefault("shop_monarch_elixir", 0)
                        inv["shop_monarch_elixir"] = currentCount + 1
                        prefs[PrefsKeys.INVENTORY_JSON] = json.encodeToString(inv)

                        // Permanently add to caps
                        // (We'll also allow user to use it from profile, let's apply cap bonus in calculations)
                    }
                }
                SoundManager.playQuestDone()
                SoundManager.speak("System Message: ${item.name} purchased successfully.")
            } else {
                SoundManager.playLockoutAlert()
                SoundManager.speak("System Message: Insufficient gold balance.")
            }
        }
        syncProfile()
        return success
    }

    suspend fun equipGear(gearId: String) {
        context.dataStore.edit { prefs ->
            val rawGear = prefs[PrefsKeys.EQUIPPED_GEAR_JSON] ?: "[]"
            val equipped = try { json.decodeFromString<List<String>>(rawGear).toMutableList() } catch(e: Exception) { mutableListOf() }

            val newGear = SYSTEM_GEAR_ITEMS.firstOrNull { it.id == gearId } ?: return@edit
            equipped.removeAll { equippedId ->
                val g = SYSTEM_GEAR_ITEMS.firstOrNull { it.id == equippedId }
                g != null && g.statType == newGear.statType
            }
            equipped.add(gearId)
            prefs[PrefsKeys.EQUIPPED_GEAR_JSON] = json.encodeToString(equipped)
            SoundManager.playQuestDone()
            SoundManager.speak("Equipped ${newGear.name}.")
        }
        syncProfile()
    }

    suspend fun unequipGear(gearId: String) {
        context.dataStore.edit { prefs ->
            val rawGear = prefs[PrefsKeys.EQUIPPED_GEAR_JSON] ?: "[]"
            val equipped = try { json.decodeFromString<List<String>>(rawGear).toMutableList() } catch(e: Exception) { mutableListOf() }
            if (equipped.remove(gearId)) {
                prefs[PrefsKeys.EQUIPPED_GEAR_JSON] = json.encodeToString(equipped)
                SoundManager.playQuestDone()
                SoundManager.speak("Unequipped gear.")
            }
        }
        syncProfile()
    }

    suspend fun assignShadowToStat(shadowId: String, statCategory: String?) {
        context.dataStore.edit { prefs ->
            val rawShadows = prefs[PrefsKeys.SHADOW_ARMY_JSON] ?: json.encodeToString(INITIAL_SHADOWS)
            val shadows = try { json.decodeFromString<List<ShadowHunter>>(rawShadows) } catch(e: Exception) { INITIAL_SHADOWS }

            val updated = shadows.map {
                if (it.id == shadowId) {
                    it.copy(assignedStat = statCategory)
                } else if (statCategory != null && it.assignedStat == statCategory) {
                    it.copy(assignedStat = null)
                } else {
                    it
                }
            }
            prefs[PrefsKeys.SHADOW_ARMY_JSON] = json.encodeToString(updated)
            SoundManager.playQuestDone()
            val name = shadows.firstOrNull { it.id == shadowId }?.name ?: "Shadow"
            if (statCategory != null) {
                SoundManager.speak("$name assigned to $statCategory.")
            } else {
                SoundManager.speak("$name unassigned.")
            }
        }
        syncProfile()
    }

    suspend fun extractShadow(shadowId: String): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val rawShadows = prefs[PrefsKeys.SHADOW_ARMY_JSON] ?: json.encodeToString(INITIAL_SHADOWS)
            val shadows = try { json.decodeFromString<List<ShadowHunter>>(rawShadows).toMutableList() } catch(e: Exception) { INITIAL_SHADOWS.toMutableList() }

            val index = shadows.indexOfFirst { it.id == shadowId }
            if (index >= 0 && !shadows[index].isExtracted) {
                shadows[index] = shadows[index].copy(isExtracted = true)
                prefs[PrefsKeys.SHADOW_ARMY_JSON] = json.encodeToString(shadows)
                success = true

                if (shadows.all { it.isExtracted }) {
                    prefs.unlockTitle("Shadow Monarch")
                }
            }
        }
        syncProfile()
        return success
    }

    private fun MutablePreferences.unlockTitle(title: String) {
        val rawTitles = this[PrefsKeys.UNLOCKED_TITLES_JSON] ?: "[]"
        val titles = try { json.decodeFromString<List<String>>(rawTitles).toMutableList() } catch(e: Exception) { mutableListOf() }
        if (!titles.contains(title)) {
            titles.add(title)
            this[PrefsKeys.UNLOCKED_TITLES_JSON] = json.encodeToString(titles)
            SoundManager.speak("System Message: New Title Unlocked: $title!")
        }
    }

    suspend fun selectTitle(title: String) {
        context.dataStore.edit { prefs ->
            val rawTitles = prefs[PrefsKeys.UNLOCKED_TITLES_JSON] ?: "[]"
            val titles = try { json.decodeFromString<List<String>>(rawTitles) } catch(e: Exception) { emptyList() }
            if (titles.contains(title) || title.isEmpty()) {
                prefs[PrefsKeys.ACTIVE_TITLE] = title
                SoundManager.playQuestDone()
                if (title.isNotEmpty()) {
                    SoundManager.speak("Title equipped: $title.")
                } else {
                    SoundManager.speak("Title unequipped.")
                }
            }
        }
        syncProfile()
    }

    suspend fun executeDungeonRaid(dungeonId: String): DungeonRaid? {
        var updatedRaid: DungeonRaid? = null
        context.dataStore.edit { prefs ->
            val rawDungeons = prefs[PrefsKeys.ACTIVE_DUNGEONS_JSON] ?: json.encodeToString(INITIAL_DUNGEONS)
            val dungeons = try { json.decodeFromString<List<DungeonRaid>>(rawDungeons).toMutableList() } catch(e: Exception) { INITIAL_DUNGEONS.toMutableList() }

            val index = dungeons.indexOfFirst { it.id == dungeonId }
            if (index < 0) return@edit

            val raid = dungeons[index]
            if (raid.isDefeated) return@edit

            val currentStr = prefs[PrefsKeys.STR_STAT] ?: 10
            val currentAgi = prefs[PrefsKeys.AGI_STAT] ?: 10
            val rawGear = prefs[PrefsKeys.EQUIPPED_GEAR_JSON] ?: "[]"
            val equipped = try { json.decodeFromString<List<String>>(rawGear) } catch(e: Exception) { emptyList() }

            var gearBonusStr = 0
            var gearBonusAgi = 0
            equipped.forEach { gearId ->
                val gear = SYSTEM_GEAR_ITEMS.firstOrNull { it.id == gearId }
                if (gear != null) {
                    if (gear.statType == "STR") gearBonusStr += gear.bonusValue
                    if (gear.statType == "AGI") gearBonusAgi += gear.bonusValue
                }
            }

            val totalStr = currentStr + gearBonusStr
            val totalAgi = currentAgi + gearBonusAgi

            val rawShadows = prefs[PrefsKeys.SHADOW_ARMY_JSON] ?: json.encodeToString(INITIAL_SHADOWS)
            val shadows = try { json.decodeFromString<List<ShadowHunter>>(rawShadows) } catch(e: Exception) { INITIAL_SHADOWS }
            val extractedShadows = shadows.filter { it.isExtracted }

            val logs = mutableListOf<String>()
            logs.add("⚔️ Raid Party enters the Gate: ${raid.name}")
            logs.add("👥 Hunters and ${extractedShadows.size} Shadows prepare for battle...")

            var bossHp = raid.bossMaxHp
            var round = 1

            while (bossHp > 0) {
                logs.add("── ROUND $round ──")
                val baseDmg = (totalStr + totalAgi) * 2
                val playerDmg = (baseDmg * (0.8 + Math.random() * 0.4)).toInt()
                bossHp -= playerDmg
                logs.add("🗡️ You strike the ${raid.bossName} dealing $playerDmg damage!")

                if (bossHp <= 0) break

                extractedShadows.forEach { shadow ->
                    val shadowDmg = when (shadow.rank) {
                        "Knight" -> 40 + (0..20).random()
                        "Elite Knight" -> 70 + (0..30).random()
                        "Commander" -> 120 + (0..50).random()
                        else -> 30
                    }
                    bossHp -= shadowDmg
                    logs.add("🔥 [${shadow.name}] slashes with shadow fire dealing $shadowDmg damage!")
                }

                if (bossHp <= 0) break

                val bossRetaliation = (20..50).random()
                logs.add("💥 ${raid.bossName} counterattacks, dealing damage to party!")

                round++
                if (round > 10) {
                    bossHp = 0
                }
            }

            logs.add("🏆 Victory! The Dungeon Boss [${raid.bossName}] has been slain.")

            val xpGained = raid.xpReward
            val goldGained = raid.goldReward

            val boosterExpiry = prefs[PrefsKeys.XP_BOOSTER_EXPIRY] ?: 0L
            val finalXp = if (System.currentTimeMillis() < boosterExpiry) xpGained * 2 else xpGained

            prefs.addXpAndCheckLevelUp(finalXp)
            val currentGold = prefs[PrefsKeys.GOLD_BALANCE] ?: 0
            prefs[PrefsKeys.GOLD_BALANCE] = currentGold + goldGained
            logs.add("💰 Rewards Acquired: +$goldGained Gold, +$finalXp XP!")

            if (Math.random() < 0.25) {
                val randomGear = SYSTEM_GEAR_ITEMS.random()
                val rawInv = prefs[PrefsKeys.INVENTORY_JSON] ?: "{}"
                val inv = try { json.decodeFromString<Map<String, Int>>(rawInv).toMutableMap() } catch(e: Exception) { mutableMapOf() }
                inv[randomGear.id] = inv.getOrDefault(randomGear.id, 0) + 1
                prefs[PrefsKeys.INVENTORY_JSON] = json.encodeToString(inv)
                logs.add("🎁 Loot Drop: Unlocked [${randomGear.emoji} ${randomGear.name}] in your inventory!")
                SoundManager.speak("System Message: Rare loot drop acquired!")
            }

            val completedRaid = raid.copy(
                bossCurrentHp = 0,
                isDefeated = true,
                combatLogs = logs,
                wasAttemptedToday = true
            )
            dungeons[index] = completedRaid
            prefs[PrefsKeys.ACTIVE_DUNGEONS_JSON] = json.encodeToString(dungeons)
            updatedRaid = completedRaid
        }
        syncProfile()
        return updatedRaid
    }

    suspend fun updatePersonalDetails(
        gender: String,
        weightKg: Float,
        heightCm: Float,
        ageYears: Int,
        fitnessGoal: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.USER_GENDER] = gender
            prefs[PrefsKeys.USER_WEIGHT] = weightKg
            prefs[PrefsKeys.USER_HEIGHT] = heightCm
            prefs[PrefsKeys.USER_AGE] = ageYears
            prefs[PrefsKeys.USER_FITNESS_GOAL] = fitnessGoal
        }
        syncProfile()
    }
}

private val INITIAL_SHADOWS = listOf(
    ShadowHunter("shadow_igris", "Igris", "Knight", "Commander of the Shadow Infantry. Boosts STR workout XP by 15%.", "⚔️"),
    ShadowHunter("shadow_iron", "Iron", "Knight", "Gigantic Shield Knight. Boosts VIT sleep/hydration task XP by 15%.", "🛡️"),
    ShadowHunter("shadow_tusk", "Tusk", "Elite Knight", "High Orc Spellcaster. Boosts SEN meditation/mood XP by 15%.", "🔮"),
    ShadowHunter("shadow_beru", "Beru", "Commander", "The Ant King. Boosts INT study timer session XP by 20%.", "🐜")
)

private val INITIAL_DUNGEONS = listOf(
    DungeonRaid("dungeon_d", "D-Rank Gate: Goblin Nest", "D-Class", "Goblin Shaman", 400, 400, false, 80, 100),
    DungeonRaid("dungeon_c", "C-Rank Gate: Golem Cave", "C-Class", "Steel Golem", 1000, 1000, false, 150, 200),
    DungeonRaid("dungeon_b", "B-Rank Gate: Elven Sanctuary", "B-Class", "Ice Elf Warlord", 2200, 2200, false, 250, 400),
    DungeonRaid("dungeon_s", "S-Rank Gate: Dragon's Lair", "S-Class", "Antares the Fire Dragon", 6000, 6000, false, 600, 1000)
)


