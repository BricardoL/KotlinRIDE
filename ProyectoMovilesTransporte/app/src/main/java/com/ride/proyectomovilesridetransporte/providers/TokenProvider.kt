package com.ride.proyectomovilesridetransporte.providers

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.ride.proyectomovilesridetransporte.models.Token

class TokenProvider {
    var mDatabase: DatabaseReference
    fun create(idUser: String?) {
        if (idUser == null) return
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = Token(instanceIdResult.token)
            mDatabase.child(idUser).setValue(token)
        }
    }

    fun getToken(idUser: String?): DatabaseReference {
        return mDatabase.child(idUser!!)
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("Tokens")
    }
}