package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Replanifie le rappel automatique de clôture de caisse suite au redémarrage de l'appareil.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == "android.intent.action.QUICKBOOT_POWERON" || 
            action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            if (NotificationHelper.isClosingReminderEnabled(context)) {
                val reminderTime = NotificationHelper.getClosingReminderTime(context)
                NotificationHelper.scheduleDailyClosingReminder(context, reminderTime)
            }
        }
    }
}
