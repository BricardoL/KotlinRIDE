package com.proyecto.proyectotransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.proyecto.proyectotransporte.models.ClientBooking
import java.util.*

class ClientBookingProvider {
    private val mDatabase: DatabaseReference
    fun create(clientBooking: ClientBooking): Task<Void> {
        return mDatabase.child(clientBooking.idClient).setValue(clientBooking)
    }

    fun updateStatus(idClientBooking: String?, status: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        return mDatabase.child(idClientBooking!!).updateChildren(map)
    }

    fun getStatus(idClientBooking: String?): DatabaseReference {
        return mDatabase.child(idClientBooking!!).child("status")
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("ClientBooking")
    }
}