package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val subjectName = intent?.getStringExtra("subject") ?: "حصة دراسية"
        val timeString = intent?.getStringExtra("time") ?: "قريباً"

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val channelId = "session_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "تذكير بالحصص", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("تذكير: حصة $subjectName")
            .setContentText("تبدأ في $timeString")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
    }
}