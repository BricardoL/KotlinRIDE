package com.ride.proyectomovilesridetransporte.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.client.RegisterActivity
import com.ride.proyectomovilesridetransporte.activities.driver.RegisterDriverActivity
import com.ride.proyectomovilesridetransporte.includes.MyToolbar

class SelectOptionAuthActivity : AppCompatActivity() {
    lateinit var mButtonGoToLogin: Button
    lateinit var mButtonGoToRegister: Button
    var mPref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_option_auth)
        MyToolbar.show(this, "Seleccionar opcion", true)
        mButtonGoToLogin = findViewById(R.id.btnGoToLogin)
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister)
        mButtonGoToLogin.setOnClickListener { goToLogin() }
        mButtonGoToRegister.setOnClickListener { goToRegister() }
        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)
    }

    fun goToLogin() {
        val intent = Intent(this@SelectOptionAuthActivity, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goToRegister() {
        val typeUser = mPref!!.getString("user", "")
        if (typeUser == "client") {
            val intent = Intent(this@SelectOptionAuthActivity, RegisterActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@SelectOptionAuthActivity, RegisterDriverActivity::class.java)
            startActivity(intent)
        }
    }
}