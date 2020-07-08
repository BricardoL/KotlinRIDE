package com.ride.proyectomovilesridetransporte.activities.client
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.LoginActivity
import com.ride.proyectomovilesridetransporte.includes.MyToolbar
import com.ride.proyectomovilesridetransporte.models.Client
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.ClientProvider
import dmax.dialog.SpotsDialog

class RegisterActivity : AppCompatActivity() {
    var mAuthProvider: AuthProvider? = null
    var mClientProvider: ClientProvider? = null

    // VIEWS
    lateinit var mButtonRegister: Button
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputName: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    var mDialog: AlertDialog? = null
    lateinit var mcheckPolitics: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        MyToolbar.show(this, "Registro de usuario", true)
        mAuthProvider = AuthProvider()
        mClientProvider = ClientProvider()
        mDialog = SpotsDialog.Builder().setContext(this@RegisterActivity).setMessage("Espere un momento").build()
        mButtonRegister = findViewById(R.id.btnRegister)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputName = findViewById(R.id.textInputName)
        mTextInputPassword = findViewById(R.id.textInputPassword)
        mButtonRegister.setOnClickListener{ clickRegister() }
        mcheckPolitics = findViewById(R.id.checkPolitics1)
    }

    fun clickRegister() {
        val name = mTextInputName!!.text.toString()
        val email = mTextInputEmail!!.text.toString()
        val password = mTextInputPassword!!.text.toString()
        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if (password.length >= 6) {
                mDialog!!.show()
                register(name, email, password)

            } else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    fun register(name: String?, email: String?, password: String?) {
        if (mcheckPolitics.isChecked) {
            mAuthProvider!!.register(email, password).addOnCompleteListener { task ->
                mDialog!!.hide()
                if (task.isSuccessful) {

                    val id = FirebaseAuth.getInstance().currentUser!!.uid
                    val user = FirebaseAuth.getInstance().currentUser!!
                    val client = Client(id, name, email)
                    create(client)

                    user.sendEmailVerification()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Se ha enviado un correo de verificación a " + client.email, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(this, "No se ha podido enviar un correo de verificación a " + client.email, Toast.LENGTH_LONG).show()

                                }
                            }

                } else {
                    Toast.makeText(this@RegisterActivity, "No se pudo registrar el usuario debido a que el correo no existe o ya tenga un registro previo", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            mDialog!!.hide()
            mcheckPolitics.error = "Debe aceptar esta condición"

        }
    }



    fun create(client: Client?) {
        mClientProvider!!.create(client!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this@RegisterActivity, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }


}