package com.skillsnap.app.data.database

import androidx.room.*
import com.skillsnap.app.data.model.Challenge
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChallengeDao {
    
    @Query("SELECT * FROM challenges WHERE skillName = :skillName ORDER BY day ASC")
    fun getChallengesForSkill(skillName: String): Flow<List<Challenge>>
    
    @Query("SELECT * FROM challenges WHERE skillName = :skillName AND day = :day LIMIT 1")
    suspend fun getChallengeForDay(skillName: String, day: Int): Challenge?
    
    @Query("SELECT * FROM challenges WHERE isCompleted = 0 ORDER BY day ASC LIMIT 1")
    suspend fun getNextIncompleteChallenge(): Challenge?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<Challenge>)
    
    @Update
    suspend fun updateChallenge(challenge: Challenge)
    
    @Query("UPDATE challenges SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :challengeId")
    suspend fun markChallengeComplete(challengeId: Long, isCompleted: Boolean, completedAt: Date?)
    
    @Query("DELETE FROM challenges WHERE skillName = :skillName")
    suspend fun deleteChallengesForSkill(skillName: String)
    
    @Query("SELECT COUNT(*) FROM challenges WHERE skillName = :skillName AND isCompleted = 1")
    suspend fun getCompletedChallengesCount(skillName: String): Int
    
    @Query("SELECT COUNT(*) FROM challenges WHERE skillName = :skillName")
    suspend fun getTotalChallengesCount(skillName: String): Int
} 