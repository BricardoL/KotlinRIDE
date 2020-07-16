package com.blazo.transporteuniversitarioapp

import android.R.attr.name
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ConductorLoginActivity : AppCompatActivity() {

    private lateinit var txtUser: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var btnregistrar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conductor_login)

        txtUser = findViewById(R.id.et_carne_login_c)
        password = findViewById(R.id.et_pass_login_c)
        progressBar = findViewById(R.id.pb_login_c)
        auth = FirebaseAuth.getInstance()
        btnregistrar = findViewById(R.id.btn_ingresar_login_c)


    }

    private fun valEmail(): Boolean {
        var regex =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[u][c][a]+(\\.[e][d][u]+)*(\\.[s][v])$".toRegex()
        var email = txtUser.text.toString()
        if (!regex.matches(email)) {

            txtUser.error = "Introduzca un correo institucional  válido"
            return false


        }
        return true
    }

    private fun validate(): Boolean {
        if (txtUser.text.toString().isEmpty()) {
            txtUser.error = "Tienes que introducir tu correo"
            return false
        } else if (password.text.toString().isEmpty()) {
            password.error = "Tienes que introducir la contraseña"
            return false
        }

        return true
    }


    fun forgotPassword(view: View) {
        startActivity(Intent(this, ForgotPassActivity::class.java))
    }

    fun register(view: View) {


        startActivity(Intent(this, ConductorRegisterActivity::class.java))

    }


    fun signin(view: View) {

        validate()
        //valEmail()
        loginUser()


    }

    private fun loginUser() {
        val user: String = txtUser.text.toString()
        val pass: String = password.text.toString()

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {

            progressBar.visibility = View.VISIBLE

            auth.signInWithEmailAndPassword(user, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkEmail()
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }





    private fun action() {

        startActivity(Intent(this, ConductorActivity::class.java))
        finish()
    }

    private fun checkEmail() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (user!!.isEmailVerified) {
            action()
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "No se ha verificado en su correo", Toast.LENGTH_LONG).show()
        }
    }
}
