package com.proyecto.proyectotransporte.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.activities.client.activity_register
import com.proyecto.proyectotransporte.activities.driver.RegisteDriverActivity
import com.proyecto.proyectotransporte.includes.Mytoolbar.show

class SelectOptionOutActivity : AppCompatActivity() {
    lateinit var mButtonGoToLogin: Button
    lateinit var mButtonGoToRegister: Button
    var mPref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_option_out)
        show(this, "Registrar usuario", true)
        mButtonGoToLogin = findViewById(R.id.btnGoToLogin)
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister)
        mButtonGoToLogin.setOnClickListener(View.OnClickListener { goToLogin() })
        mButtonGoToRegister.setOnClickListener(View.OnClickListener { goToRegister() })
        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)
    }

    fun goToLogin() {
        val intent = Intent(this@SelectOptionOutActivity, activity_login::class.java)
        startActivity(intent)
    }

    fun goToRegister() {
        val typeUser = mPref!!.getString("user", "")
        if (typeUser == "client") {
            val intent = Intent(this@SelectOptionOutActivity, activity_register::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@SelectOptionOutActivity, RegisteDriverActivity::class.java)
            startActivity(intent)
        }
    }
}