package com.blazo.transporteuniversitarioapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_slideshow.*
import java.util.jar.Manifest

class LoginActivity : AppCompatActivity() {

    private lateinit var txtUser: EditText
    private lateinit var password: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var btnregistrar: Button




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        txtUser = findViewById(R.id.et_carne_login)
        password = findViewById(R.id.et_pass_login)
        progressBar = findViewById(R.id.pb_login)
        auth = FirebaseAuth.getInstance()
        btnregistrar= findViewById(R.id.btn_ingresar_login)


    }
    private fun valEmail(): Boolean{
        var regex= "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[u][c][a]+(\\.[e][d][u]+)*(\\.[s][v])$".toRegex()
        var email=txtUser.text.toString()
        if (!regex.matches(email)){

            txtUser.error="Introduzca un correo institucional  válido"
            return false


        }
        return true   }

    private fun validate() :Boolean{
        if(txtUser.text.toString().isEmpty()){
            txtUser.error="Tienes que introducir tu correo"
            return false
        }else if (password.text.toString().isEmpty()){
            password.error="Tienes que introducir la contraseña"
            return false
        }

        return true
    }




    fun forgotPassword(view: View) {
        startActivity(Intent(this, ForgotPassActivity::class.java))
    }

    fun register(view: View) {


                startActivity(Intent(this, RegisterActivity::class.java))

        }


    fun signin(view: View){

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
                    }else{
                        progressBar.visibility= View.GONE
                        Toast.makeText(this,"Error en la autenticación", Toast.LENGTH_LONG).show()
                    }
                }

        }
    }
    private fun action(){

        startActivity(Intent(this, ClientActivity::class.java))
        finish()
    }
    private fun checkEmail(){
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if(user!!.isEmailVerified){
            action()
        } else{
            progressBar.visibility= View.GONE
            Toast.makeText(this, "No se ha verificado en su correo", Toast.LENGTH_LONG).show()
        }
    }



    /*override fun onStart() {
        super.onStart()
        if(auth.currentUser != null){
            startActivity(Intent(this, PreBeginActivity::class.java))
            finish()
        }
    }*/
}
