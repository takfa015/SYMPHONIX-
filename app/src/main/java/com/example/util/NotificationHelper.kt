package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object NotificationHelper {

    private const val PREFS_NAME = "symphonix_notification_prefs"
    private const val KEY_CLOSING_REMINDER_ENABLED = "closing_reminder_enabled"
    private const val KEY_CLOSING_REMINDER_TIME = "closing_reminder_time" // "HH:mm"
    private const val KEY_LOW_BALANCE_ENABLED = "low_balance_enabled"
    private const val KEY_LOW_BALANCE_THRESHOLD = "low_balance_threshold"

    const val CHANNEL_CLOSING_ID = "symphonix_closing_reminders"
    const val CHANNEL_LOW_BALANCE_ID = "symphonix_low_balance"

    const val NOTIF_ID_CLOSING = 1001
    const val NOTIF_ID_LOW_BALANCE = 1002
    const val NOTIF_ID_TEST = 1003

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes notification channels (required on Android 8.0+)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val closingChannel = NotificationChannel(
                CHANNEL_CLOSING_ID,
                "Rappel de Clôture de Caisse",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avertissement en cas d'oubli de clôture de caisse en fin de journée"
                enableVibration(true)
            }

            val lowBalanceChannel = NotificationChannel(
                CHANNEL_LOW_BALANCE_ID,
                "Alerte Solde Faible",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avertissement lorsque le solde disponible passe sous le seuil configuré"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(closingChannel)
            notificationManager.createNotificationChannel(lowBalanceChannel)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Preferences getters & setters
    // ---------------------------------------------------------------------------------------------

    fun isClosingReminderEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_CLOSING_REMINDER_ENABLED, true)
    }

    fun setClosingReminderEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_CLOSING_REMINDER_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDailyClosingReminder(context, getClosingReminderTime(context))
        } else {
            cancelDailyClosingReminder(context)
        }
    }

    fun getClosingReminderTime(context: Context): String {
        return getPrefs(context).getString(KEY_CLOSING_REMINDER_TIME, "20:00") ?: "20:00"
    }

    fun setClosingReminderTime(context: Context, time: String) {
        getPrefs(context).edit().putString(KEY_CLOSING_REMINDER_TIME, time).apply()
        if (isClosingReminderEnabled(context)) {
            scheduleDailyClosingReminder(context, time)
        }
    }

    fun isLowBalanceAlertEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOW_BALANCE_ENABLED, true)
    }

    fun setLowBalanceAlertEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOW_BALANCE_ENABLED, enabled).apply()
    }

    fun getLowBalanceThreshold(context: Context): Double {
        return getPrefs(context).getString(KEY_LOW_BALANCE_THRESHOLD, "5000.0")?.toDoubleOrNull() ?: 5000.0
    }

    fun setLowBalanceThreshold(context: Context, threshold: Double) {
        getPrefs(context).edit().putString(KEY_LOW_BALANCE_THRESHOLD, threshold.toString()).apply()
    }

    // ---------------------------------------------------------------------------------------------
    // Alarm Scheduling
    // ---------------------------------------------------------------------------------------------

    fun scheduleDailyClosingReminder(context: Context, timeString: String) {
        val parts = timeString.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 20
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClosingReminderReceiver::class.java).apply {
            action = ClosingReminderReceiver.ACTION_CHECK_CLOSING
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // In Android 12+, exact alarm might require permission, setRepeating with RTC_WAKEUP does not.
        }
    }

    fun cancelDailyClosingReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClosingReminderReceiver::class.java).apply {
            action = ClosingReminderReceiver.ACTION_CHECK_CLOSING
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // ---------------------------------------------------------------------------------------------
    // Notification Dispatching
    // ---------------------------------------------------------------------------------------------

    fun sendClosingReminderNotification(context: Context) {
        createNotificationChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CLOSING_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ SYMPHONIX Caisse - Clôture en attente")
            .setContentText("Votre caisse du jour n'a pas encore été clôturée. Effectuez le pointage physique.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Votre caisse journalière est toujours ouverte. Veuillez procéder au comptage des espèces et à la clôture officielle de la journée pour assurer la conformité de vos comptes."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_CLOSING, notification)
        } catch (_: SecurityException) {
            // Permission denied
        }
    }

    fun sendLowBalanceNotification(context: Context, balance: Double, threshold: Double, currency: String) {
        createNotificationChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val balanceFormatted = "%,.2f".format(balance)
        val thresholdFormatted = "%,.2f".format(threshold)

        val notification = NotificationCompat.Builder(context, CHANNEL_LOW_BALANCE_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ SYMPHONIX - Alerte Solde Faible")
            .setContentText("Solde actuel : $balanceFormatted $currency (seuil : $thresholdFormatted $currency)")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Attention : Le solde disponible en caisse est descendu à $balanceFormatted $currency, ce qui est inférieur au seuil d'alerte configuré de $thresholdFormatted $currency. Prévoyez un réassort de caisse si nécessaire."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_LOW_BALANCE, notification)
        } catch (_: SecurityException) {
            // Permission denied
        }
    }

    fun sendTestNotification(context: Context) {
        createNotificationChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CLOSING_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 Notifications SYMPHONIX Actives")
            .setContentText("Les rappels de clôture et alertes de solde fonctionnent parfaitement.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_TEST, notification)
        } catch (_: SecurityException) {
            // Permission denied
        }
    }
}
