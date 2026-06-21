package com.example.sololeveling90days

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object OnboardingKey : NavKey

@Serializable
object AuthKey : NavKey

@Serializable
object HomeKey : NavKey

@Serializable
object QuestsKey : NavKey

@Serializable
object ToolsKey : NavKey

@Serializable
object CardsKey : NavKey

@Serializable
object ProfileKey : NavKey

@Serializable
object FocusTimerKey : NavKey

@Serializable
object MeditationKey : NavKey

@Serializable
object CalorieTrackerKey : NavKey

@Serializable
object MoodJournalKey : NavKey

@Serializable
object StreakCalendarKey : NavKey

@Serializable
object ComebackKey : NavKey

@Serializable
object CertificateKey : NavKey

@Serializable
object AdditionalTasksKey : NavKey

@Serializable
data class QuestDetailKey(val questId: String) : NavKey


