package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClosingReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CHECK_CLOSING = "com.example.action.CHECK_CLOSING_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (!NotificationHelper.isClosingReminderEnabled(context)) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val latestSession = db.cashDao().getLatestSession()

                // If there is an open session that has not been closed yet
                if (latestSession != null && !latestSession.isClosed) {
                    NotificationHelper.sendClosingReminderNotification(context)
                }

                // Reschedule for tomorrow at the configured time
                NotificationHelper.scheduleDailyClosingReminder(
                    context,
                    NotificationHelper.getClosingReminderTime(context)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
