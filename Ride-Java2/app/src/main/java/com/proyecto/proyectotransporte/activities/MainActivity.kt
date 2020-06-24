package com.proyecto.proyectotransporte.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.activities.client.MapClientActivity
import com.proyecto.proyectotransporte.activities.driver.MapDriverActivity

class MainActivity : AppCompatActivity() {
    lateinit var mButtonIAmClient: Button
    lateinit var mButtonIAmDriver: Button
    lateinit var mPref: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)
        val editor = mPref.edit()
        mButtonIAmClient = findViewById(R.id.btnIAmClient)
        mButtonIAmDriver = findViewById(R.id.btnIAmDriver)
        mButtonIAmClient.setOnClickListener(View.OnClickListener {
            editor.putString("user", "client")
            editor.apply()
            goToSelectAuth()
        })
        mButtonIAmDriver.setOnClickListener(View.OnClickListener {
            editor.putString("user", "driver")
            editor.apply()
            goToSelectAuth()
        })
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            val user = mPref!!.getString("user", "")
            if (user == "client") {
                val intent = Intent(this@MainActivity, MapClientActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                val intent = Intent(this@MainActivity, MapDriverActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
        }
    }

    private fun goToSelectAuth() {
        val intent = Intent(this@MainActivity, SelectOptionOutActivity::class.java)
        startActivity(intent)
    }
}