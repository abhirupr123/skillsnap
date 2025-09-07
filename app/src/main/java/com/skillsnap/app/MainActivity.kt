package com.skillsnap.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skillsnap.app.ui.screen.ChallengeScreen
import com.skillsnap.app.ui.screen.CompletionScreen
import com.skillsnap.app.ui.screen.LoadingScreen
import com.skillsnap.app.ui.screen.OnboardingScreen
import com.skillsnap.app.ui.screen.SkillSelectionScreen
import com.skillsnap.app.ui.theme.SkillSnapTheme
import com.skillsnap.app.ui.viewmodel.ChallengeViewModel
import com.skillsnap.app.data.preferences.UserPreferences
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: ChallengeViewModel by viewModels()
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            SkillSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SkillSnapApp(
                        viewModel = viewModel,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSnapApp(
    viewModel: ChallengeViewModel,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSkill by viewModel.currentSkill.collectAsStateWithLifecycle()
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    
    // Check if onboarding is completed - make it reactive
    var isOnboardingCompleted by remember { mutableStateOf(userPreferences.onboardingCompleted) }
    
    // Show error snackbar if there's an error
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // In a real app, you'd show a Snackbar here
            Log.e("SkillSnapApp", "Error: $error")
            viewModel.clearError()
        }
    }
    
    when {
        !isOnboardingCompleted -> {
            OnboardingScreen(
                onComplete = { userName, notificationTime ->
                    // Save user preferences
                    userPreferences.userName = userName
                    userPreferences.notificationTime = notificationTime
                    userPreferences.onboardingCompleted = true
                    
                    // Update the reactive state
                    isOnboardingCompleted = true
                    
                    Log.d("SkillSnapApp", "Onboarding completed for user: $userName at ${notificationTime}:00")
                    
                    // Schedule daily reminders based on user's preferred time
                    viewModel.scheduleDailyRemindersAtTime(notificationTime)
                },
                modifier = modifier
            )
        }
        
        uiState.isLoading -> {
            LoadingScreen(
                skillName = currentSkill ?: "your skill",
                modifier = modifier
            )
        }
        
        currentSkill != null && challenges.isNotEmpty() && uiState.isSkillCompleted -> {
            CompletionScreen(
                skillName = currentSkill ?: "",
                completedChallenges = uiState.completedChallenges,
                totalChallenges = uiState.totalChallenges,
                streakDays = userPreferences.streakCount,
                onStartNewChallenge = {
                    // Reset completion state and go to skill selection
                    viewModel.resetSkillCompletion()
                    viewModel.setCurrentSkill("")
                },
                onContinueSkill = {
                    // Generate advanced challenges for the same skill
                    viewModel.resetSkillCompletion()
                    viewModel.generateChallenges("Advanced $currentSkill")
                },
                onShareProgress = {
                    // TODO: Implement sharing functionality
                    Log.d("SkillSnapApp", "Share progress for $currentSkill")
                },
                modifier = modifier
            )
        }
        
        currentSkill != null && challenges.isNotEmpty() -> {
            ChallengeScreen(
                challenges = challenges,
                uiState = uiState,
                onMarkComplete = viewModel::markChallengeComplete,
                onBackToSelection = {
                    // Reset to skill selection
                    viewModel.setCurrentSkill("")
                },
                modifier = modifier
            )
        }
        
        else -> {
            SkillSelectionScreen(
                onSkillSelected = { skill ->
                    viewModel.generateChallenges(skill)
                },
                onResetOnboarding = {
                    // Reset onboarding for testing
                    userPreferences.resetOnboarding()
                    isOnboardingCompleted = false
                    Log.d("SkillSnapApp", "Onboarding reset via long press - will show onboarding screen")
                },
                modifier = modifier
            )
        }
    }
} 