package com.client.smartpigclient.Config

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.client.smartpigclient.MainActivity
import com.client.smartpigclient.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get title and body from backend push
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["message"] ?: "Pig alert!"

        Log.d("FCM Service", "Message received: $body")

        // Show notification with dynamic title
        sendNotification(title, body)
    }

    private fun sendNotification(title: String, body: String) {
        val channelId = "pig_alerts"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pig Alerts", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.pig_ic)
            .setContentTitle(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Unique ID so temp and humid notifications don’t overwrite each other
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

}
