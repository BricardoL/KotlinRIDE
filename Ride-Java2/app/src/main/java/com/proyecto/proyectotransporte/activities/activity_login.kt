package com.proyecto.proyectotransporte.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.activities.client.MapClientActivity
import com.proyecto.proyectotransporte.activities.driver.MapDriverActivity
import com.proyecto.proyectotransporte.includes.Mytoolbar.show
import dmax.dialog.SpotsDialog

class activity_login : AppCompatActivity() {
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    lateinit var mButtonLogin: Button
    var mAuth: FirebaseAuth? = null
    var mDatabase: DatabaseReference? = null
    var mDialog: AlertDialog? = null
    var mPref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        show(this, "Login de usuario", true)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputPassword = findViewById(R.id.textInputPassword)
        mButtonLogin = findViewById(R.id.btnLogin)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mDialog = SpotsDialog.Builder().setContext(this@activity_login).setMessage("Espere un momento").build()
        mPref = applicationContext.getSharedPreferences("typeUser", Context.MODE_PRIVATE)
        mButtonLogin.setOnClickListener(View.OnClickListener { login() })
    }

    private fun login() {
        val email = mTextInputEmail!!.text.toString()
        val password = mTextInputPassword!!.text.toString()
        if (!email.isEmpty() && !password.isEmpty()) {
            if (password.length >= 6) {
                mDialog!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mPref!!.getString("user", "")
                        if (user == "client") {
                            val intent = Intent(this@activity_login, MapClientActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        } else {
                            val intent = Intent(this@activity_login, MapDriverActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this@activity_login, "La contraseña o el password son incorrectos", Toast.LENGTH_SHORT).show()
                    }
                    mDialog!!.dismiss()
                }
            } else {
                Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "La contraseña y el email son obligatorios", Toast.LENGTH_SHORT).show()
        }
    }
}