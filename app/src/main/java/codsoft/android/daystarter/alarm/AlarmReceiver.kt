package codsoft.android.daystarter.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import codsoft.android.daystarter.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the alarm action here, in this case, show a notification
        if (context != null && intent != null) {
            val alarmName = intent.getStringExtra("ALARM_NAME")

            // You can customize your notification here
            val notification = createNotification(context, alarmName)

            // Display the notification
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationId = 0 // You can assign a unique ID to each notification
            notificationManager.notify(notificationId, notification)
        }
    }
}

private const val CHANNEL_ID = "alarm_id" // Use the same channel ID you defined in MainActivity

fun createNotification(context: Context, alarmName: String?): Notification {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create a notification channel if the device is running Android Oreo or higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Alarm Notifications"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_icon)
        .setContentTitle("Alarm")
        .setContentText("Alarm: $alarmName is ringing!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    return builder.build()
}


