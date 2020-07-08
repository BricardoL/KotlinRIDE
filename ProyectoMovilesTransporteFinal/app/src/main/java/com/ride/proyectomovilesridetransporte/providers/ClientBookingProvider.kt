package com.ride.proyectomovilesridetransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ride.proyectomovilesridetransporte.models.ClientBooking
import java.util.*

class ClientBookingProvider {
    private val mDatabase: DatabaseReference
    fun create(clientBooking: ClientBooking): Task<Void> {
        return mDatabase.child(clientBooking.idClient!!).setValue(clientBooking)
    }

    fun updateStatus(idClientBooking: String?, status: String): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["status"] = status
        return mDatabase.child(idClientBooking!!).updateChildren(map)
    }

    fun updateIdHistoryBooking(idClientBooking: String?): Task<Void> {
        val idPush = mDatabase.push().key
        val map: MutableMap<String, Any?> = HashMap()
        map["idHistoryBooking"] = idPush
        return mDatabase.child(idClientBooking!!).updateChildren(map)
    }

    fun updatePrice(idClientBooking: String?, price: Double): Task<Void> {
        val map: MutableMap<String, Any> = HashMap()
        map["price"] = price
        return mDatabase.child(idClientBooking!!).updateChildren(map)
    }

    fun getStatus(idClientBooking: String?): DatabaseReference {
        return mDatabase.child(idClientBooking!!).child("status")
    }

    fun getClientBooking(idClientBooking: String?): DatabaseReference {
        return mDatabase.child(idClientBooking!!)
    }

    fun delete(idClientBooking: String?): Task<Void> {
        return mDatabase.child(idClientBooking!!).removeValue()
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("ClientBooking")
    }
}