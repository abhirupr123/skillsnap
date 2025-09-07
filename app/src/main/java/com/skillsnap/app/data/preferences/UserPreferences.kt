package com.skillsnap.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "skillsnap_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val KEY_COMPLETED_SKILLS = "completed_skills"
        private const val KEY_TOTAL_CHALLENGES_COMPLETED = "total_challenges_completed"
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_LAST_ACTIVITY_DATE = "last_activity_date"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PREFERRED_DIFFICULTY = "preferred_difficulty"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    // User Profile
    var userName: String
        get() = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_USER_NAME, value).apply()
    
    // Notification Settings
    var notificationTime: Int // Hour of day (0-23)
        get() = sharedPreferences.getInt(KEY_NOTIFICATION_TIME, 9) // Default 9 AM
        set(value) = sharedPreferences.edit().putInt(KEY_NOTIFICATION_TIME, value).apply()
    
    // Progress Tracking
    var completedSkills: Set<String>
        get() = sharedPreferences.getStringSet(KEY_COMPLETED_SKILLS, emptySet()) ?: emptySet()
        set(value) = sharedPreferences.edit().putStringSet(KEY_COMPLETED_SKILLS, value).apply()
    
    var totalChallengesCompleted: Int
        get() = sharedPreferences.getInt(KEY_TOTAL_CHALLENGES_COMPLETED, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_TOTAL_CHALLENGES_COMPLETED, value).apply()
    
    var streakCount: Int
        get() = sharedPreferences.getInt(KEY_STREAK_COUNT, 0)
        set(value) = sharedPreferences.edit().putInt(KEY_STREAK_COUNT, value).apply()
    
    var lastActivityDate: Long
        get() = sharedPreferences.getLong(KEY_LAST_ACTIVITY_DATE, 0L)
        set(value) = sharedPreferences.edit().putLong(KEY_LAST_ACTIVITY_DATE, value).apply()
    
    // App Settings
    var onboardingCompleted: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    
    var preferredDifficulty: String
        get() = sharedPreferences.getString(KEY_PREFERRED_DIFFICULTY, "medium") ?: "medium"
        set(value) = sharedPreferences.edit().putString(KEY_PREFERRED_DIFFICULTY, value).apply()
    
    // FCM and Device Management
    var fcmToken: String
        get() = sharedPreferences.getString(KEY_FCM_TOKEN, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_FCM_TOKEN, value).apply()
    
    var deviceId: String
        get() = sharedPreferences.getString(KEY_DEVICE_ID, "") ?: ""
        set(value) = sharedPreferences.edit().putString(KEY_DEVICE_ID, value).apply()
    
    // Helper Methods
    fun addCompletedSkill(skillName: String) {
        val currentSkills = completedSkills.toMutableSet()
        currentSkills.add(skillName)
        completedSkills = currentSkills
    }
    
    fun incrementChallengesCompleted() {
        totalChallengesCompleted += 1
    }
    
    fun updateStreak() {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24) // Days since epoch
        val lastActivity = lastActivityDate / (1000 * 60 * 60 * 24)
        
        when {
            lastActivity == today -> {
                // Same day, no change to streak
            }
            lastActivity == today - 1 -> {
                // Consecutive day, increment streak
                streakCount += 1
            }
            else -> {
                // Streak broken, reset to 1
                streakCount = 1
            }
        }
        
        lastActivityDate = System.currentTimeMillis()
    }
    
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun resetOnboarding() {
        onboardingCompleted = false
        Log.d("UserPreferences", "Onboarding reset - user will see onboarding screen again")
    }
} 