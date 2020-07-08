package com.ride.proyectomovilesridetransporte.providers

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InfoProvider {
    var info: DatabaseReference

    init {
        info = FirebaseDatabase.getInstance().reference.child("Info")
    }
}