package com.proyecto.proyectotransporte.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.channel.NotificationHelper
import com.proyecto.proyectotransporte.receivers.AcceptReceiver
import com.proyecto.proyectotransporte.receivers.CancelReceiver

class MyFirebaseMessagingClient : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val notification = remoteMessage.notification
        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        if (title != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    val idClient = data["idClient"]
                    showNotificationApiOreoActions(title, body, idClient)
                } else {
                    showNotificationApiOreo(title, body)
                }
            } else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    val idClient = data["idClient"]
                    showNotificationActions(title, body, idClient)
                } else {
                    showNotification(title, body)
                }
            }
        }
    }

    private fun showNotification(title: String, body: String?) {
        val intent = PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldAPI(title, body, intent, sound)
        notificationHelper.manager!!.notify(1, builder.build())
    }

    private fun showNotificationActions(title: String, body: String?, idClient: String?) {

        // ACEPTAR
        val acceptIntent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val acceptAction = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build()

        // CANCELAR
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cancelAction = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationOldAPIActions(title, body, sound, acceptAction, cancelAction)
        notificationHelper.manager!!.notify(2, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreo(title: String, body: String?) {
        val intent = PendingIntent.getActivity(baseContext, 0, Intent(), PendingIntent.FLAG_ONE_SHOT)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotification(title, body, intent, sound)
        notificationHelper.manager!!.notify(1, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun showNotificationApiOreoActions(title: String, body: String?, idClient: String?) {
        val acceptIntent = Intent(this, AcceptReceiver::class.java)
        acceptIntent.putExtra("idClient", idClient)
        val acceptPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val acceptAction = Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                acceptPendingIntent
        ).build()
        val cancelIntent = Intent(this, CancelReceiver::class.java)
        cancelIntent.putExtra("idClient", idClient)
        val cancelPendingIntent = PendingIntent.getBroadcast(this, NOTIFICATION_CODE, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val cancelAction = Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                cancelPendingIntent
        ).build()
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationHelper = NotificationHelper(baseContext)
        val builder = notificationHelper.getNotificationActions(title, body, sound, acceptAction, cancelAction)
        notificationHelper.manager!!.notify(2, builder.build())
    }

    companion object {
        private const val NOTIFICATION_CODE = 100
    }
}