package com.example.bloodpressuremonitorconnector.utils.bluetooth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.bloodpressuremonitorconnector.R

class BleService : Service() {
    private lateinit var bleManager: BleManager
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "BleServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        bleManager = BleContainer.getBleManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Blood Pressure Monitor")
            .setContentText("Bluetooth service is running")
            .setSmallIcon(R.drawable.notification_icon)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bluetooth Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}