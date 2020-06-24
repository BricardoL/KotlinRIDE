package com.proyecto.proyectotransporte.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.proyecto.proyectotransporte.providers.ClientBookingProvider

class CancelReceiver : BroadcastReceiver() {
    private var mClientBookingProvider: ClientBookingProvider? = null
    override fun onReceive(context: Context, intent: Intent) {
        val idClient = intent.extras!!.getString("idClient")
        mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider!!.updateStatus(idClient, "cancel")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
    }
}