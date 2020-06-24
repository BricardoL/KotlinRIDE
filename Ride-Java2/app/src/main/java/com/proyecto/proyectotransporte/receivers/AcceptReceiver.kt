package com.proyecto.proyectotransporte.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.proyecto.proyectotransporte.activities.driver.MapDriverBookingActivity
import com.proyecto.proyectotransporte.providers.ClientBookingProvider

class AcceptReceiver : BroadcastReceiver() {
    private var mClientBookingProvider: ClientBookingProvider? = null
    override fun onReceive(context: Context, intent: Intent) {
        val idClient = intent.extras!!.getString("idClient")
        mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider!!.updateStatus(idClient, "accept")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent1 = Intent(context, MapDriverBookingActivity::class.java)
        intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent1.action = Intent.ACTION_RUN
        context.startActivity(intent1)
    }
}