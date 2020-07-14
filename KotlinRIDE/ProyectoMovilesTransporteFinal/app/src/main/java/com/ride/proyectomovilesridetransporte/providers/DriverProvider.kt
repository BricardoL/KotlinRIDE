package com.ride.proyectomovilesridetransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ride.proyectomovilesridetransporte.models.Driver
import java.util.*

class DriverProvider {
    var mDatabase: DatabaseReference
    fun create(driver: Driver): Task<Void> {
        return mDatabase.child(driver.id!!).setValue(driver)
    }

    fun getDriver(idDriver: String?): DatabaseReference {
        return mDatabase.child(idDriver!!)
    }

    fun update(driver: Driver): Task<Void> {
        val map: MutableMap<String, Any?> = HashMap()
        map["name"] = driver.name
        map["image"] = driver.image
        map["vehicleBrand"] = driver.vehicleBrand
        map["vehiclePlate"] = driver.vehiclePlate
        return mDatabase.child(driver.id!!).updateChildren(map)
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child("Drivers")
    }
}