package com.ride.proyectomovilesridetransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ride.proyectomovilesridetransporte.models.HistoryBooking
import java.util.*

class HistoryBookingProvider {
    private val mDatabase: DatabaseReference
    fun create(historyBooking: HistoryBooking): Task<Void> {
        return mDatabase.child(historyBooking.idHistoryBooking!!).setValue(historyBooking)
    }

    fun updateCalificactionClient(idHistoryBooking: String?, calificacionClient: Float): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["calificationClient"] = calificacionClient
        return mDatabase.child(idHistoryBooking!!).updateChildren(map)
    }

    fun updateCalificactionDriver(idHistoryBooking: String?, calificacionDriver: Float): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["calificationDriver"] = calificacionDriver
        return mDatabase.child(idHistoryBooking!!).updateChildren(map)
    }

    fun getHistoryBooking(idHistoryBooking: String?): DatabaseReference {
        return mDatabase.child(idHistoryBooking!!)
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("HistoryBooking")
    }
}