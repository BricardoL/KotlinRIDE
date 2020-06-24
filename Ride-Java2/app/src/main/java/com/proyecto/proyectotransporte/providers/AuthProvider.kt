package com.proyecto.proyectotransporte.providers

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthProvider {
    var mAuth: FirebaseAuth
    fun register(email: String?, password: String?): Task<AuthResult> {
        return mAuth.createUserWithEmailAndPassword(email!!, password!!)
    }

    fun login(email: String?, password: String?): Task<AuthResult> {
        return mAuth.signInWithEmailAndPassword(email!!, password!!)
    }

    fun logout() {
        mAuth.signOut()
    }

    val id: String
        get() = mAuth.currentUser!!.uid

    fun existSession(): Boolean {
        var exist = false
        if (mAuth.currentUser != null) {
            exist = true
        }
        return exist
    }

    init {
        mAuth = FirebaseAuth.getInstance()
    }
}