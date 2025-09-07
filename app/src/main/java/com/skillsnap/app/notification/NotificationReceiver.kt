package com.skillsnap.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received broadcast: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule notifications after device reboot
                Log.d("NotificationReceiver", "Device rebooted, rescheduling notifications")
                // WorkManager will automatically reschedule periodic work
            }
        }
    }
} 