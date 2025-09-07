package com.skillsnap.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.skillsnap.app.data.model.Challenge
import com.skillsnap.app.data.repository.ChallengeRepository
import com.skillsnap.app.notification.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()
    
    private val _currentSkill = MutableStateFlow<String?>(null)
    val currentSkill: StateFlow<String?> = _currentSkill.asStateFlow()
    
    val challenges: StateFlow<List<Challenge>> = _currentSkill
        .filterNotNull()
        .flatMapLatest { skill ->
            challengeRepository.getChallengesForSkill(skill)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun generateChallenges(skillName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d("ChallengeViewModel", "Generating challenges for: $skillName")
                
                val result = challengeRepository.generateAndSaveChallenges(skillName)
                
                if (result.isSuccess) {
                    _currentSkill.value = skillName
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasGeneratedChallenges = true
                    )
                    
                    // Schedule daily reminders
                    scheduleDailyReminders()
                    
                    Log.d("ChallengeViewModel", "Successfully generated challenges")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to generate challenges. Please try again."
                    )
                    Log.e("ChallengeViewModel", "Failed to generate challenges", result.exceptionOrNull())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An error occurred. Please try again."
                )
                Log.e("ChallengeViewModel", "Error generating challenges", e)
            }
        }
    }
    
    fun markChallengeComplete(challengeId: Long) {
        viewModelScope.launch {
            try {
                Log.d("ChallengeViewModel", "Marking challenge $challengeId as complete")
                challengeRepository.markChallengeComplete(challengeId)
                
                // Update progress
                _currentSkill.value?.let { skill ->
                    updateProgress(skill)
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error marking challenge complete", e)
            }
        }
    }
    
    private fun updateProgress(skillName: String) {
        viewModelScope.launch {
            try {
                val (completed, total) = challengeRepository.getProgress(skillName)
                val isCompleted = completed == total && total > 0
                
                _uiState.value = _uiState.value.copy(
                    completedChallenges = completed,
                    totalChallenges = total,
                    isSkillCompleted = isCompleted
                )
                
                // If skill is completed, mark it in user preferences
                if (isCompleted) {
                    challengeRepository.markSkillCompleted(skillName)
                    Log.d("ChallengeViewModel", "Skill '$skillName' completed! 🎉")
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error updating progress", e)
            }
        }
    }
    
    private fun scheduleDailyReminders() {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel existing work
        workManager.cancelUniqueWork("daily_reminder")
        
        // Create new periodic work request
        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(1, TimeUnit.HOURS) // Start in 1 hour
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        
        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyReminderRequest
        )
        
        Log.d("ChallengeViewModel", "Daily reminders scheduled")
    }
    
    fun scheduleDailyRemindersAtTime(hour: Int) {
        val workManager = WorkManager.getInstance(context)
        
        // Cancel existing work
        workManager.cancelUniqueWork("daily_reminder")
        
        // Calculate initial delay to the specified hour
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        // Set target time
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        // If the time has already passed today, schedule for tomorrow
        if (hour <= currentHour) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
        
        // Create new periodic work request
        val dailyReminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        
        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyReminderRequest
        )
        
        Log.d("ChallengeViewModel", "Daily reminders scheduled for ${hour}:00 with initial delay: ${initialDelay / 1000 / 60} minutes")
    }
    
    fun getTodayChallenge(): Challenge? {
        val challengesList = challenges.value
        return challengesList.find { !it.isCompleted } ?: challengesList.firstOrNull()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun setCurrentSkill(skillName: String) {
        _currentSkill.value = skillName
        viewModelScope.launch {
            updateProgress(skillName)
        }
    }
    
    fun resetSkillCompletion() {
        _uiState.value = _uiState.value.copy(isSkillCompleted = false)
    }
}

data class ChallengeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasGeneratedChallenges: Boolean = false,
    val completedChallenges: Int = 0,
    val totalChallenges: Int = 0,
    val isSkillCompleted: Boolean = false
)