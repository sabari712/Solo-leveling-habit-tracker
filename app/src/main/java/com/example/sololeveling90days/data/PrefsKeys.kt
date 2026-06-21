package com.example.sololeveling90days.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sololeveling90days_prefs")

object PrefsKeys {
    val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_GOAL = stringPreferencesKey("user_goal")
    val USER_WAKE_TIME = stringPreferencesKey("user_wake_time")
    val STREAK = intPreferencesKey("streak")
    val TOTAL_XP = intPreferencesKey("total_xp")
    val HARD_MODE = booleanPreferencesKey("hard_mode")
    val LAST_COMPLETED_DATE = stringPreferencesKey("last_completed_date")
    val QUESTS_JSON = stringPreferencesKey("quests_json")
    val COMPLETED_DATES_JSON = stringPreferencesKey("completed_dates_json")
    val COLLECTED_CARDS_JSON = stringPreferencesKey("collected_cards_json")
    val DAY_NUMBER = intPreferencesKey("day_number")
    val START_DATE = stringPreferencesKey("start_date")
    val FOCUS_TIMER_WORK = intPreferencesKey("focus_timer_work")
    val FOCUS_TIMER_BREAK = intPreferencesKey("focus_timer_break")
    val CALORIE_LOG_JSON = stringPreferencesKey("calorie_log_json")
    val MOOD_ENTRY_JSON = stringPreferencesKey("mood_entry_json")
    val GATE_PASSES = intPreferencesKey("gate_passes")
    val COMEBACK_AVAILABLE = booleanPreferencesKey("comeback_available")
    val COMEBACK_DEADLINE = stringPreferencesKey("comeback_deadline")
    val LAST_LOGIN_DATE = stringPreferencesKey("last_login_date")
    val LAST_LOGIN_BONUS_XP = intPreferencesKey("last_login_bonus_xp")
    val IS_SUBSCRIBED = booleanPreferencesKey("is_subscribed")
    val TIMER_TARGET_EPOCH = longPreferencesKey("timer_target_epoch")
    val TIMER_IS_RUNNING = booleanPreferencesKey("timer_is_running")
    val TIMER_MODE = stringPreferencesKey("timer_mode")
    val TIMER_SAVED_REMAINING = longPreferencesKey("timer_saved_remaining")
    val AMOLED_MODE = booleanPreferencesKey("amoled_mode")
    val ADDITIONAL_TASKS_JSON = stringPreferencesKey("additional_tasks_json")
    val JOGGING_SESSIONS_JSON = stringPreferencesKey("jogging_sessions_json")
    val DAILY_STEPS_JSON = stringPreferencesKey("daily_steps_json")
    val STEPS_START_OF_DAY = intPreferencesKey("steps_start_of_day")
    val QUEST_COMPLETIONS_JSON = stringPreferencesKey("quest_completions_json")
    val LAST_CHECKED_DATE = stringPreferencesKey("last_checked_date")
    val GUEST_MODE = booleanPreferencesKey("guest_mode")
}


