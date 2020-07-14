package com.ride.proyectomovilesridetransporte.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.ride.proyectomovilesridetransporte.R

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var mButtonPassword: Button
    private lateinit var txtEmail: TextInputEditText
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        txtEmail= findViewById(R.id.textInputEmailE)
        auth = FirebaseAuth.getInstance()
        mButtonPassword= findViewById(R.id.btnPassword)

        mButtonPassword.setOnClickListener {
            validate()
            sendEmail()
        }
    }
    private fun sendEmail(){
        val email=txtEmail.text.toString()

        if (!TextUtils.isEmpty(email)){
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this){
                        task ->
                        if (task.isSuccessful){

                            Toast.makeText(this, "Se ha enviado un correo para restablecer la contraseña", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()

                        }else{
                            Toast.makeText(this, "Error al enviar el correo para reestablecer la contraseña", Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }
    private fun validate() :Boolean{
        if(txtEmail.text.toString().isEmpty()){
            txtEmail.error="Tienes que introducir tu correo"
            return false

        }

        return true
    }
}
