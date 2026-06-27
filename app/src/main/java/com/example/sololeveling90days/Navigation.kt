package com.example.sololeveling90days

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.sololeveling90days.data.AppRepository
import com.example.sololeveling90days.data.UserProfile
import com.example.sololeveling90days.ui.cards.CardLibraryScreen
import com.example.sololeveling90days.ui.home.HomeScreen
import com.example.sololeveling90days.ui.onboarding.ComebackScreen
import com.example.sololeveling90days.ui.onboarding.OnboardingScreen
import com.example.sololeveling90days.ui.onboarding.PenaltyScreen
import com.example.sololeveling90days.ui.onboarding.SplashLoadingScreen
import com.example.sololeveling90days.ui.profile.CertificateScreen
import com.example.sololeveling90days.ui.profile.ProfileScreen
import com.example.sololeveling90days.ui.quests.QuestsScreen
import com.example.sololeveling90days.ui.quests.QuestDetailScreen
import com.example.sololeveling90days.ui.tools.CalorieTrackerScreen
import com.example.sololeveling90days.ui.tools.FocusTimerScreen
import com.example.sololeveling90days.ui.tools.MeditationScreen
import com.example.sololeveling90days.ui.tools.MoodJournalScreen
import com.example.sololeveling90days.ui.tools.ToolsScreen
import com.example.sololeveling90days.ui.tools.TdeeScreen
import com.example.sololeveling90days.ui.streak.StreakCalendarScreen
import com.example.sololeveling90days.ui.additional.AdditionalTasksScreen
import com.example.sololeveling90days.ui.exercise.WorkoutLibraryScreen
import com.example.sololeveling90days.ui.exercise.ExerciseCameraScreen
import com.example.sololeveling90days.ui.exercise.ExerciseResultScreen
import kotlinx.coroutines.launch

import com.example.sololeveling90days.ui.auth.AuthScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }
    val isOnboardingDone by repository.isOnboardingDone.collectAsStateWithLifecycle(initialValue = null)
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())
    val isGuestMode by repository.isGuestMode.collectAsStateWithLifecycle(initialValue = null)
    val sessionStatus by repository.authRepository.sessionStatus.collectAsStateWithLifecycle(initialValue = io.github.jan.supabase.auth.status.SessionStatus.Initializing)
    val isAuthenticated = sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated || repository.authRepository.isAuthenticated()
    var showSplash by remember { mutableStateOf(true) }
    var systemMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val msg = repository.verifyStreakAndPenalties()
        systemMsg = msg
        val loginBonus = repository.checkAndAwardLoginBonus()
        // loginBonus XP awarded, handled in HomeScreen via state
        if (isAuthenticated) {
            repository.syncManager.pullFromCloud()
        }
    }

    if (showSplash) {
        SplashLoadingScreen(onTimeout = { showSplash = false })
    } else {
        if (isGuestMode == null) {
            // Loading
        } else if (!isAuthenticated && !isGuestMode!!) {
            AuthScreen(
                authRepository = repository.authRepository,
                onAuthSuccess = {
                    scope.launch {
                        repository.syncManager.pullFromCloud()
                    }
                },
                onContinueAsGuest = {
                    scope.launch {
                        repository.setGuestMode(true)
                    }
                }
            )
        } else {
            when (isOnboardingDone) {
                null -> {} // Loading
                false -> {
                    OnboardingScreen(repository = repository, onComplete = {})
                }
                true -> {
                    if (profile.isPenaltyActive) {
                        PenaltyScreen(
                            repository = repository,
                            onComplete = { /* unlocked */ }
                        )
                    } else if (profile.comebackAvailable) {
                        val backStack = rememberNavBackStack(HomeKey)
                        ComebackScreen(
                            repository = repository,
                            onComplete = { /* navigate home */ },
                            onDismiss = { /* navigate home */ }
                        )
                    } else {
                        MainAppNav(repository = repository)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppNav(repository: AppRepository) {
    val backStack = rememberNavBackStack(HomeKey)
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<HomeKey> {
                HomeScreen(
                    repository = repository,
                    onNavigate = { key -> backStack.add(key as NavKey) }
                )
            }
            entry<QuestsKey> {
                QuestsScreen(
                    repository = repository,
                    onNavigate = { key -> backStack.add(key as NavKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WorkoutLibraryKey> {
                val scope = rememberCoroutineScope()
                WorkoutLibraryScreen(
                    onAddToQuests = { definition, reps ->
                        scope.launch {
                            repository.addExerciseQuest(definition.type, reps)
                        }
                    },
                    onStartExercise = { type, reps ->
                        backStack.add(
                            ExerciseCameraKey(
                                exerciseTypeName = type.name,
                                targetReps = reps,
                                questId = ""
                            )
                        )
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ExerciseCameraKey> { key ->
                val cameraKey = key as ExerciseCameraKey
                ExerciseCameraScreen(
                    exerciseTypeName = cameraKey.exerciseTypeName,
                    targetReps = cameraKey.targetReps,
                    questId = cameraKey.questId,
                    onComplete = { reps, duration, verified ->
                        backStack.removeLastOrNull() // remove camera
                        backStack.add(
                            ExerciseResultKey(
                                exerciseTypeName = cameraKey.exerciseTypeName,
                                repsCompleted = reps,
                                targetReps = cameraKey.targetReps,
                                durationSeconds = duration,
                                wasVerified = verified,
                                questId = cameraKey.questId
                            )
                        )
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ExerciseResultKey> { key ->
                val resultKey = key as ExerciseResultKey
                val scope = rememberCoroutineScope()
                ExerciseResultScreen(
                    exerciseTypeName = resultKey.exerciseTypeName,
                    repsCompleted = resultKey.repsCompleted,
                    targetReps = resultKey.targetReps,
                    durationSeconds = resultKey.durationSeconds,
                    wasVerified = resultKey.wasVerified,
                    questId = resultKey.questId,
                    onDone = {
                        scope.launch {
                            repository.verifyExerciseQuest(
                                questId = resultKey.questId,
                                repsCompleted = resultKey.repsCompleted,
                                wasVerified = resultKey.wasVerified
                            )
                            backStack.removeLastOrNull() // remove result screen
                        }
                    }
                )
            }
            entry<ToolsKey> {
                ToolsScreen(
                    repository = repository,
                    onNavigate = { key -> backStack.add(key as NavKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CardsKey> {
                CardLibraryScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ProfileKey> {
                ProfileScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigate = { key -> backStack.add(key as NavKey) }
                )
            }
            entry<FocusTimerKey> {
                FocusTimerScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<MeditationKey> {
                MeditationScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CalorieTrackerKey> {
                CalorieTrackerScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<MoodJournalKey> {
                MoodJournalScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<StreakCalendarKey> {
                StreakCalendarScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ComebackKey> {
                ComebackScreen(
                    repository = repository,
                    onComplete = { backStack.removeLastOrNull() },
                    onDismiss = { backStack.removeLastOrNull() }
                )
            }
            entry<CertificateKey> {
                CertificateScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<AdditionalTasksKey> {
                AdditionalTasksScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<TdeeKey> {
                TdeeScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<QuestDetailKey> { key ->
                val questDetailKey = key as QuestDetailKey
                QuestDetailScreen(
                    repository = repository,
                    questId = questDetailKey.questId,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

