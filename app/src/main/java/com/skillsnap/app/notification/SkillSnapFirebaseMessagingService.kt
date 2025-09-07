package com.skillsnap.app.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skillsnap.app.data.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class SkillSnapFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            
            val challengeType = remoteMessage.data["type"]
            if (challengeType == "global_challenge") {
                val challengeText = remoteMessage.data["challenge"]
                if (challengeText != null) {
                    showGlobalChallengeNotification(challengeText)
                }
            }
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            val notificationHelper = NotificationHelper(this)
            notificationHelper.showDailyReminder(it.body)
        }
    }
    
    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        
        // Store token locally
        userPreferences.fcmToken = token
        
        // Generate device ID if not exists
        if (userPreferences.deviceId.isEmpty()) {
            userPreferences.deviceId = UUID.randomUUID().toString()
        }
        
        // Send token to your server if needed
        sendRegistrationToServer(token)
    }
    
    private fun sendRegistrationToServer(token: String?) {
        // Store token and device info for potential server integration
        Log.d("FCM", "Token stored locally: $token")
        Log.d("FCM", "Device ID: ${userPreferences.deviceId}")
        Log.d("FCM", "User: ${userPreferences.userName}")
        
        // TODO: In a real app, you would send this to your backend server
        // Example: API call to register device for push notifications
        /*
        val deviceInfo = mapOf(
            "fcm_token" to token,
            "device_id" to userPreferences.deviceId,
            "user_name" to userPreferences.userName,
            "notification_time" to userPreferences.notificationTime
        )
        // POST to your server: /api/register-device
        */
    }
    
    private fun showGlobalChallengeNotification(challengeText: String) {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.showDailyReminder("Global Challenge: $challengeText")
    }
} 