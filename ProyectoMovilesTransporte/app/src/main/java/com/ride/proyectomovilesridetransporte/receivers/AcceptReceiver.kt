package com.ride.proyectomovilesridetransporte.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ride.proyectomovilesridetransporte.activities.driver.MapDriverBookingActivity
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.ClientBookingProvider
import com.ride.proyectomovilesridetransporte.providers.GeofireProvider

class AcceptReceiver : BroadcastReceiver() {
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mAuthProvider: AuthProvider? = null
    override fun onReceive(context: Context, intent: Intent) {
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider("active_drivers")
        mGeofireProvider!!.removeLocation(mAuthProvider!!.id)
        val idClient = intent.extras!!.getString("idClient")
        mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider!!.updateStatus(idClient, "accept")
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent1 = Intent(context, MapDriverBookingActivity::class.java)
        intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent1.action = Intent.ACTION_RUN
        intent1.putExtra("idClient", idClient)
        context.startActivity(intent1)
    }
}