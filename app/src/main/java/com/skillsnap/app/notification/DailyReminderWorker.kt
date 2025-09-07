package com.skillsnap.app.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skillsnap.app.data.repository.ChallengeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val challengeRepository: ChallengeRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.d("DailyReminderWorker", "Executing daily reminder work")
            
            // Get the next incomplete challenge
            val nextChallenge = challengeRepository.getNextIncompleteChallenge()
            
            val notificationHelper = NotificationHelper(applicationContext)
            
            if (nextChallenge != null) {
                // Show notification with specific challenge
                notificationHelper.showDailyReminder(
                    "Day ${nextChallenge.day}: ${nextChallenge.challengeText}"
                )
                Log.d("DailyReminderWorker", "Sent notification for challenge: ${nextChallenge.challengeText}")
            } else {
                // Show generic reminder
                notificationHelper.showDailyReminder()
                Log.d("DailyReminderWorker", "Sent generic reminder notification")
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "Error in daily reminder work", e)
            Result.failure()
        }
    }
} 