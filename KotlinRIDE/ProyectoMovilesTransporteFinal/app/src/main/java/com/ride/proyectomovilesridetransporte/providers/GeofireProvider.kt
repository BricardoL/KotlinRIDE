package com.ride.proyectomovilesridetransporte.providers

import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class GeofireProvider(reference: String?) {
    private val mDatabase: DatabaseReference
    private val mGeofire: GeoFire
    fun saveLocation(idDriver: String?, latLng: LatLng) {
        mGeofire.setLocation(idDriver, GeoLocation(latLng.latitude, latLng.longitude))
    }

    fun removeLocation(idDriver: String?) {
        mGeofire.removeLocation(idDriver)
    }

    fun getActiveDrivers(latLng: LatLng, radius: Double): GeoQuery {
        val geoQuery = mGeofire.queryAtLocation(GeoLocation(latLng.latitude, latLng.longitude), radius)
        geoQuery.removeAllListeners()
        return geoQuery
    }

    fun getDriverLocation(idDriver: String?): DatabaseReference {
        return mDatabase.child(idDriver!!).child("l")
    }

    fun isDriverWorking(idDriver: String?): DatabaseReference {
        return FirebaseDatabase.getInstance().reference.child("drivers_working").child(idDriver!!)
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child(reference!!)
        mGeofire = GeoFire(mDatabase)
    }
}