package com.skillsnap.app.data.model

data class ChallengeResponse(
    val challenges: List<DailyChallenge>
)

data class DailyChallenge(
    val day: Int,
    val challenge: String
) 