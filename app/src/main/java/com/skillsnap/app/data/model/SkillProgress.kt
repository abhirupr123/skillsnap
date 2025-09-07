package com.skillsnap.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "skill_progress")
data class SkillProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val skillName: String,
    val startDate: Date,
    val completionDate: Date? = null,
    val isCompleted: Boolean = false,
    val completedChallenges: Int = 0,
    val totalChallenges: Int = 7,
    val streakDays: Int = 0,
    val lastActivityDate: Date? = null,
    val difficulty: String = "medium", // easy, medium, hard
    val notes: String = ""
)

@Entity(tableName = "daily_activity")
data class DailyActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val skillName: String,
    val challengeId: Long,
    val timeSpentMinutes: Int = 0,
    val completedAt: Date,
    val rating: Int = 0, // 1-5 stars
    val notes: String = ""
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "first_skill", "streak_7", "streak_30", "skill_master", etc.
    val title: String,
    val description: String,
    val unlockedAt: Date,
    val iconName: String = ""
) 