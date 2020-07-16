package com.blazo.transporteuniversitarioapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPassActivity : AppCompatActivity() {
    private lateinit var txtEmail:EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)

        txtEmail=findViewById(R.id.et_email_forgotpass)
        auth= FirebaseAuth.getInstance()
        progressBar=findViewById(R.id.pb_forgotpass)
    }
    fun send(view: View){
        validate()
        sendEmail()
    }
    private fun sendEmail(){
        val email=txtEmail.text.toString()

        if (!TextUtils.isEmpty(email)){
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){
                        task ->
                    if (task.isSuccessful){
                        progressBar.visibility=View.VISIBLE
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
