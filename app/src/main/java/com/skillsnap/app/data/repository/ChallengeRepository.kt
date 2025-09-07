package com.skillsnap.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.skillsnap.app.data.api.*
import com.skillsnap.app.data.database.ChallengeDao
import com.skillsnap.app.data.model.Challenge
import com.skillsnap.app.data.model.ChallengeResponse
import com.skillsnap.app.data.model.DailyChallenge
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeRepository @Inject constructor(
    private val challengeDao: ChallengeDao,
    private val aiService: AIService,
    private val userPreferences: com.skillsnap.app.data.preferences.UserPreferences
) {
    
    fun getChallengesForSkill(skillName: String): Flow<List<Challenge>> {
        return challengeDao.getChallengesForSkill(skillName)
    }
    
    suspend fun getChallengeForDay(skillName: String, day: Int): Challenge? {
        return challengeDao.getChallengeForDay(skillName, day)
    }
    
    suspend fun getNextIncompleteChallenge(): Challenge? {
        return challengeDao.getNextIncompleteChallenge()
    }
    
    suspend fun markChallengeComplete(challengeId: Long) {
        Log.d("ChallengeRepository", "Marking challenge $challengeId as complete")
        challengeDao.markChallengeComplete(challengeId, true, Date())
        
        // Update user preferences and streak
        userPreferences.incrementChallengesCompleted()
        userPreferences.updateStreak()
        
        // Check if skill is completed (all 7 challenges done)
        val challenge = challengeDao.getChallengeForDay("", 0) // We'll need to get the actual challenge
        // TODO: Implement skill completion check and celebration
    }
    
    suspend fun generateAndSaveChallenges(skillName: String): Result<List<Challenge>> {
        return try {
            Log.d("ChallengeRepository", "Generating challenges for skill: $skillName")
            
            // First, delete existing challenges for this skill
            challengeDao.deleteChallengesForSkill(skillName)
            
            // Generate challenges using AI
            val challenges = generateChallengesWithAI(skillName)
            
            // Convert to Challenge entities and save
            val challengeEntities = challenges.mapIndexed { index, dailyChallenge ->
                Challenge(
                    skillName = skillName,
                    day = dailyChallenge.day,
                    challengeText = dailyChallenge.challenge,
                    isCompleted = false,
                    createdAt = Date()
                )
            }
            
            challengeDao.insertChallenges(challengeEntities)
            Log.d("ChallengeRepository", "Successfully saved ${challengeEntities.size} challenges")
            
            Result.success(challengeEntities)
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error generating challenges", e)
            Result.failure(e)
        }
    }
    
    private suspend fun generateChallengesWithAI(skillName: String): List<DailyChallenge> {
        val prompt = """
            Generate exactly 7 daily micro-challenges for learning "$skillName". 
            Each challenge should take 5-10 minutes to complete.
            Return the response in this exact JSON format:
            {
              "challenges": [
                {"day": 1, "challenge": "Learn 5 basic greetings"},
                {"day": 2, "challenge": "Practice numbers 1-20"},
                {"day": 3, "challenge": "Record yourself introducing in the language"},
                {"day": 4, "challenge": "Learn 10 common verbs"},
                {"day": 5, "challenge": "Write 5 simple sentences"},
                {"day": 6, "challenge": "Have a 2-minute conversation with yourself"},
                {"day": 7, "challenge": "Review and practice all learned words"}
              ]
            }
            
            Make the challenges progressive, engaging, and specific to "$skillName".
        """.trimIndent()
        
        try {
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(prompt))
                    )
                )
            )
            
            val response = aiService.generateChallenges(request)
            
            if (response.isSuccessful) {
                val geminiResponse = response.body()
                val content = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                if (content != null) {
                    Log.d("ChallengeRepository", "Gemini AI Response: $content")
                    
                    // Clean the response - remove markdown code blocks if present
                    val cleanedContent = content
                        .replace("```json", "")
                        .replace("```", "")
                        .trim()
                    
                    Log.d("ChallengeRepository", "Cleaned JSON: $cleanedContent")
                    
                    // Parse JSON response
                    val gson = Gson()
                    try {
                        val challengeResponse = gson.fromJson(cleanedContent, ChallengeResponse::class.java)
                        return challengeResponse.challenges
                    } catch (jsonException: Exception) {
                        Log.w("ChallengeRepository", "Failed to parse JSON response, using fallback", jsonException)
                        Log.w("ChallengeRepository", "Raw content: $content")
                        Log.w("ChallengeRepository", "Cleaned content: $cleanedContent")
                        return getFallbackChallenges(skillName)
                    }
                }
            }
            
            Log.w("ChallengeRepository", "Gemini AI service failed, using fallback challenges")
            return getFallbackChallenges(skillName)
            
        } catch (e: Exception) {
            Log.e("ChallengeRepository", "Error calling Gemini AI service", e)
            return getFallbackChallenges(skillName)
        }
    }
    
    private fun getFallbackChallenges(skillName: String): List<DailyChallenge> {
        // Fallback challenges when AI service is not available
        return when (skillName.lowercase()) {
            "spanish" -> listOf(
                DailyChallenge(1, "Learn 5 basic Spanish greetings: Hola, Buenos días, Buenas tardes, Buenas noches, Adiós"),
                DailyChallenge(2, "Practice Spanish numbers 1-20 and use them in sentences"),
                DailyChallenge(3, "Record yourself introducing in Spanish: 'Me llamo... Tengo... años'"),
                DailyChallenge(4, "Learn 10 common Spanish verbs: ser, estar, tener, hacer, ir, venir, decir, poder, querer, saber"),
                DailyChallenge(5, "Write 5 simple sentences in Spanish about your daily routine"),
                DailyChallenge(6, "Have a 2-minute conversation with yourself in Spanish about your hobbies"),
                DailyChallenge(7, "Review all learned Spanish words and create flashcards for practice")
            )
            "pushups" -> listOf(
                DailyChallenge(1, "Do 5 wall push-ups to learn proper form and build initial strength"),
                DailyChallenge(2, "Perform 3 knee push-ups focusing on controlled movement"),
                DailyChallenge(3, "Attempt 1 full push-up, or hold plank position for 30 seconds"),
                DailyChallenge(4, "Do 5 incline push-ups using a chair or bench"),
                DailyChallenge(5, "Perform 2-3 full push-ups or 5 knee push-ups"),
                DailyChallenge(6, "Complete 5 full push-ups or 8 knee push-ups"),
                DailyChallenge(7, "Challenge: Do 10 push-ups (any variation) and celebrate your progress!")
            )
            "journaling" -> listOf(
                DailyChallenge(1, "Write 3 things you're grateful for today"),
                DailyChallenge(2, "Describe your perfect day in 5 sentences"),
                DailyChallenge(3, "Write about a challenge you overcame recently"),
                DailyChallenge(4, "List 5 goals you want to achieve this month"),
                DailyChallenge(5, "Reflect on a lesson you learned this week"),
                DailyChallenge(6, "Write a letter to your future self"),
                DailyChallenge(7, "Review your week's entries and note patterns or insights")
            )
            else -> listOf(
                DailyChallenge(1, "Research the basics of $skillName for 10 minutes"),
                DailyChallenge(2, "Practice the fundamental technique of $skillName"),
                DailyChallenge(3, "Find and try a beginner tutorial for $skillName"),
                DailyChallenge(4, "Practice $skillName for 15 minutes with focus"),
                DailyChallenge(5, "Teach someone else what you've learned about $skillName"),
                DailyChallenge(6, "Challenge yourself with a slightly harder $skillName exercise"),
                DailyChallenge(7, "Reflect on your $skillName progress and plan next steps")
            )
        }
    }
    
    suspend fun getProgress(skillName: String): Pair<Int, Int> {
        val completed = challengeDao.getCompletedChallengesCount(skillName)
        val total = challengeDao.getTotalChallengesCount(skillName)
        return Pair(completed, total)
    }
    
    suspend fun markSkillCompleted(skillName: String) {
        Log.d("ChallengeRepository", "Marking skill '$skillName' as completed")
        userPreferences.addCompletedSkill(skillName)
    }
} 