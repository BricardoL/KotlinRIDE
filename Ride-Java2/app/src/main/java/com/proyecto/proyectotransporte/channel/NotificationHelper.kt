package com.proyecto.proyectotransporte.channel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.proyecto.proyectotransporte.R

class NotificationHelper(base: Context?) : ContextWrapper(base) {
    var manager: NotificationManager? = null
        get() {
            if (field == null) {
                field = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }
        private set

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lightColor = Color.GRAY
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager!!.createNotificationChannel(notificationChannel)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotification(title: String?, body: String?, intent: PendingIntent?, soundUri: Uri?): Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title))
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getNotificationActions(title: String?,
                               body: String?,
                               soundUri: Uri?,
                               acceptAction: Notification.Action?,
                               cancelAction: Notification.Action?): Notification.Builder {
        return Notification.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldAPI(title: String?, body: String?, intent: PendingIntent?, soundUri: Uri?): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_car)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    fun getNotificationOldAPIActions(
            title: String?,
            body: String?,
            soundUri: Uri?,
            acceptAction: NotificationCompat.Action?,
            cancelAction: NotificationCompat.Action?): NotificationCompat.Builder {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.ic_car)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title))
    }

    companion object {
        private const val CHANNEL_ID = "com.proyecto.proyectotransporte"
        private const val CHANNEL_NAME = "Ride"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }
}