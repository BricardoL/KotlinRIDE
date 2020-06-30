package com.ride.proyectomovilesridetransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ride.proyectomovilesridetransporte.models.Client
import java.util.*

class ClientProvider {
    var mDatabase: DatabaseReference
    fun create(client: Client): Task<Void> {
        val map: MutableMap<String, Any?> = HashMap()
        map["name"] = client.name
        map["email"] = client.email
        return mDatabase.child(client.id!!).setValue(map)
    }

    fun update(client: Client): Task<Void> {
        val map: MutableMap<String, Any?> = HashMap()
        map["name"] = client.name
        map["image"] = client.image
        return mDatabase.child(client.id!!).updateChildren(map)
    }

    fun getClient(idClient: String?): DatabaseReference {
        return mDatabase.child(idClient!!)
    }

    fun currentUser(): FirebaseUser?{
        val auth= FirebaseAuth.getInstance()
        return auth.currentUser
    }

    init {
        mDatabase = FirebaseDatabase.getInstance().reference.child("Users").child("Clients")
    }
}