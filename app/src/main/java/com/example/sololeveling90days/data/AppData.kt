package com.example.sololeveling90days.data

val DEFAULT_QUESTS = listOf(
    Quest("q1", "Morning Walk", "Take a 20-minute walk outside", 50, QuestCategory.MORNING, difficulty = QuestDifficulty.NORMAL),
    Quest("q2", "Cold Shower", "End your shower with 60 seconds cold water", 75, QuestCategory.WELLNESS, difficulty = QuestDifficulty.HARD),
    Quest("q3", "No Social Media Before 10AM", "Protect your morning focus", 60, QuestCategory.MINDSET, difficulty = QuestDifficulty.NORMAL),
    Quest("q4", "Drink 3L of Water", "Stay hydrated throughout the day", 40, QuestCategory.NUTRITION, difficulty = QuestDifficulty.NORMAL),
    Quest("q5", "Read for 20 Minutes", "Read a book (not social media)", 60, QuestCategory.LEARNING, difficulty = QuestDifficulty.NORMAL),
    Quest("q6", "10-Minute Meditation", "Sit in silence and focus on your breath", 70, QuestCategory.WELLNESS, difficulty = QuestDifficulty.NORMAL),
    Quest("q7", "Workout / Exercise", "30+ minutes of physical activity", 100, QuestCategory.FITNESS, difficulty = QuestDifficulty.HARD),
    Quest("q8", "Journal Entry", "Write 3 things you're grateful for", 50, QuestCategory.MINDSET, difficulty = QuestDifficulty.NORMAL),
    Quest("q9", "Plan Tomorrow", "Write out your top 3 tasks for tomorrow night", 40, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.NORMAL),
    Quest("q10", "Sleep Before 11PM", "Get at least 7 hours of sleep", 80, QuestCategory.SLEEP, difficulty = QuestDifficulty.HARD),
)

val BOSS_QUESTS = listOf(
    Quest("boss1", "Gate Opened: Full Body Push", "50 push-ups + 50 squats + 1 mile run. The gate demands more.", 300, QuestCategory.FITNESS, difficulty = QuestDifficulty.LEGENDARY),
    Quest("boss2", "Gate Opened: Iron Will", "No phone, no junk food, no excuses. Complete all quests before 8 PM.", 300, QuestCategory.MINDSET, difficulty = QuestDifficulty.LEGENDARY),
    Quest("boss3", "Gate Opened: The Scholar", "Read 30 minutes + write 500 words + plan next 7 days.", 300, QuestCategory.LEARNING, difficulty = QuestDifficulty.LEGENDARY),
)

val MOTIVATIONAL_CARDS = listOf(
    MotivationalCard("c1", "We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle", CardCategory.DISCIPLINE),
    MotivationalCard("c2", "The secret of getting ahead is getting started.", "Mark Twain", CardCategory.FOCUS),
    MotivationalCard("c3", "Believe you can and you're halfway there.", "Theodore Roosevelt", CardCategory.CONFIDENCE),
    MotivationalCard("c4", "Strength does not come from physical capacity. It comes from an indomitable will.", "Mahatma Gandhi", CardCategory.STRENGTH),
    MotivationalCard("c5", "The only way to do great work is to love what you do.", "Steve Jobs", CardCategory.WISDOM),
    MotivationalCard("c6", "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", CardCategory.DISCIPLINE),
    MotivationalCard("c7", "It does not matter how slowly you go as long as you do not stop.", "Confucius", CardCategory.STRENGTH),
    MotivationalCard("c8", "Your time is limited, don't waste it living someone else's life.", "Steve Jobs", CardCategory.WISDOM),
    MotivationalCard("c9", "The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt", CardCategory.CONFIDENCE),
    MotivationalCard("c10", "Focus on being productive instead of busy.", "Tim Ferriss", CardCategory.FOCUS),
    MotivationalCard("c11", "Discipline is the bridge between goals and accomplishment.", "Jim Rohn", CardCategory.DISCIPLINE),
    MotivationalCard("c12", "The pain you feel today will be the strength you feel tomorrow.", "Unknown", CardCategory.STRENGTH),
    MotivationalCard("c13", "You are never too old to set another goal or to dream a new dream.", "C.S. Lewis", CardCategory.CONFIDENCE),
    MotivationalCard("c14", "Where focus goes, energy flows.", "Tony Robbins", CardCategory.FOCUS),
    MotivationalCard("c15", "Knowing yourself is the beginning of all wisdom.", "Aristotle", CardCategory.WISDOM),
    MotivationalCard("c16", "The harder the battle, the sweeter the victory.", "Les Brown", CardCategory.STRENGTH),
    MotivationalCard("c17", "Start where you are. Use what you have. Do what you can.", "Arthur Ashe", CardCategory.DISCIPLINE),
    MotivationalCard("c18", "Self-confidence is the first requisite to great undertakings.", "Samuel Johnson", CardCategory.CONFIDENCE),
    MotivationalCard("c19", "The quality of your life is determined by the quality of your habits.", "Unknown", CardCategory.DISCIPLINE),
    MotivationalCard("c20", "An investment in knowledge pays the best interest.", "Benjamin Franklin", CardCategory.WISDOM),
)

val SYSTEM_MESSAGES = listOf(
    SystemMessage("first_launch", "[SYSTEM]: Hunter, your 90-day trial begins now. Prove your worth."),
    SystemMessage("streak_at_risk", "[SYSTEM]: Hunter, the gate closes in 3 hours. Return to your duties."),
    SystemMessage("rank_up", "[SYSTEM]: New rank achieved. The System acknowledges your growth."),
    SystemMessage("day_30", "[SYSTEM]: Hunter, you have survived 30 days. The System is watching."),
    SystemMessage("day_60", "[SYSTEM]: 60 days complete. You are approaching the final gate."),
    SystemMessage("day_90", "[SYSTEM]: S-Class certification unlocked. You have proven yourself."),
    SystemMessage("comeback", "[SYSTEM]: Hunter detected offline. Complete the Comeback Quest to restore your record."),
    SystemMessage("gate_pass_used", "[SYSTEM]: Gate Pass consumed. Streak protected. Do not fail again."),
)

fun getQuestsForGoal(goalString: String): List<Quest> {
    val selectedGoals = goalString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val compiledQuests = mutableListOf<Quest>()
    
    fun addUnique(quest: Quest) {
        if (compiledQuests.none { it.title == quest.title }) {
            compiledQuests.add(quest)
        }
    }
    
    selectedGoals.forEach { goal ->
        when (goal) {
            "Build Fitness" -> {
                addUnique(Quest("fit1", "Workout / Exercise", "30+ minutes of physical activity", 100, QuestCategory.FITNESS, difficulty = QuestDifficulty.HARD))
                addUnique(Quest("fit2", "Stretching Routine", "10 minutes of morning or evening stretching", 40, QuestCategory.FITNESS, difficulty = QuestDifficulty.NORMAL))
            }
            "Fix Sleep" -> {
                addUnique(Quest("sleep1", "Sleep Before 11PM", "Get at least 7 hours of sleep", 80, QuestCategory.SLEEP, difficulty = QuestDifficulty.HARD))
                addUnique(Quest("sleep2", "No Screens before bed", "No phone or screens 30m before sleeping", 50, QuestCategory.SLEEP, difficulty = QuestDifficulty.NORMAL))
            }
            "Mental Clarity" -> {
                addUnique(Quest("mental1", "10-Minute Meditation", "Sit in silence and focus on your breath", 70, QuestCategory.WELLNESS, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("mental2", "Journal Entry", "Write down thoughts or daily reflections", 50, QuestCategory.MINDSET, difficulty = QuestDifficulty.NORMAL))
            }
            "Career Growth" -> {
                addUnique(Quest("career1", "Deep Work Session", "Focus for 45 minutes on a skill task", 80, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.HARD))
                addUnique(Quest("career2", "Read 20 Minutes", "Read educational or professional material", 60, QuestCategory.LEARNING, difficulty = QuestDifficulty.NORMAL))
            }
            "Weight Loss" -> {
                addUnique(Quest("weight1", "Calorie Tracking", "Log all meals in calorie tracker today", 70, QuestCategory.NUTRITION, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("weight2", "Workout / Exercise", "30+ minutes of physical activity", 100, QuestCategory.FITNESS, difficulty = QuestDifficulty.HARD))
            }
            "Overall Discipline" -> {
                addUnique(Quest("disc1", "Cold Shower", "End your shower with 60 seconds cold water", 75, QuestCategory.WELLNESS, difficulty = QuestDifficulty.HARD))
                addUnique(Quest("disc2", "Make Your Bed", "Start the day with an accomplished task", 30, QuestCategory.MORNING, difficulty = QuestDifficulty.NORMAL))
            }
            "Reduce Anxiety" -> {
                addUnique(Quest("anx1", "Deep Breathing Exercises", "Spend 5 minutes doing deep box breathing", 50, QuestCategory.WELLNESS, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("anx2", "Express Gratitude", "Write down 3 things you are grateful for", 40, QuestCategory.MINDSET, difficulty = QuestDifficulty.NORMAL))
            }
            "Healthy Diet" -> {
                addUnique(Quest("diet1", "Eat 2 Portions of Fruit/Veg", "Ensure healthy food intake today", 40, QuestCategory.NUTRITION, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("diet2", "Drink 3L of Water", "Stay hydrated throughout the day", 40, QuestCategory.NUTRITION, difficulty = QuestDifficulty.NORMAL))
            }
            "Time Management" -> {
                addUnique(Quest("time1", "Plan Tomorrow", "Write out your top 3 tasks for tomorrow night", 40, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("time2", "Timebox Tasks", "Assign specific blocks of time to your tasks", 50, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.NORMAL))
            }
            "Break Bad Habits" -> {
                addUnique(Quest("bad1", "No Junk Food", "Avoid fast food, sweets, or sodas today", 80, QuestCategory.NUTRITION, difficulty = QuestDifficulty.HARD))
                addUnique(Quest("bad2", "No Social Media Before 10AM", "Protect your morning focus", 60, QuestCategory.MINDSET, difficulty = QuestDifficulty.NORMAL))
            }
            "Financial Discipline" -> {
                addUnique(Quest("fin1", "Track Expenses", "Log every cent spent today in your budget", 60, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("fin2", "Zero Spend Day", "Avoid non-essential purchases today", 75, QuestCategory.PRODUCTIVITY, difficulty = QuestDifficulty.HARD))
            }
            "Daily Meditation" -> {
                addUnique(Quest("med1", "10-Minute Meditation", "Sit in silence and focus on your breath", 70, QuestCategory.WELLNESS, difficulty = QuestDifficulty.NORMAL))
                addUnique(Quest("med2", "Mindful Moment", "Do 3 mindful check-ins during the day", 45, QuestCategory.WELLNESS, difficulty = QuestDifficulty.NORMAL))
            }
        }
    }
    
    if (compiledQuests.isEmpty()) {
        return DEFAULT_QUESTS.take(6)
    }
    
    return compiledQuests.distinctBy { it.id }.take(6)
}

fun getDailyCard(dayNumber: Int): MotivationalCard =
    MOTIVATIONAL_CARDS[dayNumber % MOTIVATIONAL_CARDS.size]

fun isBossDay(dayNumber: Int): Boolean = dayNumber > 0 && (dayNumber % 7 == 0 || dayNumber == 15 || dayNumber == 30 || dayNumber == 60 || dayNumber == 90)

fun getBossQuest(dayNumber: Int): Quest = BOSS_QUESTS[dayNumber % BOSS_QUESTS.size]
