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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var txtEmail:EditText
    private lateinit var txtName:EditText
    private lateinit var txtLastname:EditText
    private lateinit var txtBirthday:EditText
    private lateinit var txtPassword:EditText
    private lateinit var ProgressBar: ProgressBar
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txtEmail=findViewById(R.id.et_register_correo)
        txtName=findViewById(R.id.et_nombres_register)
        txtLastname=findViewById(R.id.et_apellidos_register)
        txtBirthday=findViewById(R.id.et_fecha_cumple_register)
        txtPassword=findViewById(R.id.et_password_register)
        ProgressBar=findViewById(R.id.pb_register_register)

        database= FirebaseDatabase.getInstance()
        auth= FirebaseAuth.getInstance()

        dbReference=database.reference.child("User").child("Customers")
    }

    fun registerAct(view:View){
        validate()
        createNewAccount()
        valEmail()


    }
    private fun valEmail(): Boolean{
        var regex= "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[u][c][a]+(\\.[e][d][u]+)*(\\.[s][v])$".toRegex()
        var email=txtEmail.text.toString()
        if (!regex.matches(email)){

            txtEmail.error="Introduzca un correo institucional valido"
            return false}
        return true}

    private fun createNewAccount(){
        val email:String=txtEmail.text.toString()
        val name:String=txtName.text.toString()
        val lastname:String=txtLastname.text.toString()
        val birthday:String=txtBirthday.text.toString()
        val password:String=txtPassword.text.toString()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(lastname) &&  !TextUtils.isEmpty(birthday)
            &&   !TextUtils.isEmpty(password)){
            ProgressBar.visibility=View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){
                        task ->
                    if(task.isComplete){
                        val user: FirebaseUser?=auth.currentUser
                        verifyEmail(user)

                        val userBD=dbReference.child(user?.uid!!)

                        userBD.child("Email").setValue(email)
                        userBD.child("Name").setValue(name)
                        userBD.child("Lastname").setValue(lastname)
                        userBD.child("Birthday").setValue(birthday)

                        register()
                    }
                }


        }
    }
    private fun register(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    private fun validate() :Boolean{
        if(txtEmail.text.toString().isEmpty()){
            txtEmail.error="Tienes que introducir tu correo"
            return false
        }else if (txtName.text.toString().isEmpty()){
            txtName.error="Tienes que introducir nombres"
            return false
        }else if (txtLastname.text.toString().isEmpty()){
            txtLastname.error="Tienes que introducir apellidos"
            return false
        }else if(txtBirthday.text.toString().isEmpty()){
            txtBirthday.error="Tienes que introducir tu fecha de nacimiento"
            return false
        }else if (txtPassword.text.toString().isEmpty()){
            txtPassword.error="Tienes que introducir una contraseña"
            return false
        }

        return true
    }
    private fun verifyEmail(user: FirebaseUser?){
        user!!.sendEmailVerification()
            .addOnCompleteListener(this){
                    task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Se ha enviado un correo", Toast.LENGTH_LONG).show()

                }else{
                    Toast.makeText(this, "Error al enviar correo", Toast.LENGTH_LONG).show()
                }
            }
    }

}
