package com.skillsnap.app.data.api

import com.skillsnap.app.data.model.ChallengeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AIService {
    
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent")
    suspend fun generateChallenges(@Body request: GeminiRequest): Response<GeminiResponse>
}

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.7,
    val topK: Int = 40,
    val topP: Double = 0.95,
    val maxOutputTokens: Int = 1024
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiContent
) 