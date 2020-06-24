package com.proyecto.proyectotransporte.activities.driver

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.includes.Mytoolbar.show
import com.proyecto.proyectotransporte.models.Driver
import com.proyecto.proyectotransporte.providers.AuthProvider
import com.proyecto.proyectotransporte.providers.DriverProvider
import dmax.dialog.SpotsDialog

class RegisteDriverActivity : AppCompatActivity() {
    var mAuthProvider: AuthProvider? = null
    var mDriverProvider: DriverProvider? = null

    // VIEWS
    lateinit var mButtonRegister: Button
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputName: TextInputEditText? = null
    var mTextInputVehicleBrand: TextInputEditText? = null
    var mTextInputVehiclePlate: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    var mDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registe_driver)
        show(this, "Registro de conductor", true)
        mAuthProvider = AuthProvider()
        mDriverProvider = DriverProvider()
        mDialog = SpotsDialog.Builder().setContext(this@RegisteDriverActivity).setMessage("Espere un momento").build()
        mButtonRegister = findViewById(R.id.btnRegister)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputName = findViewById(R.id.textInputName)
        mTextInputVehicleBrand = findViewById(R.id.textInputVehicleBrand)
        mTextInputVehiclePlate = findViewById(R.id.textInputVehiclePlate)
        mTextInputPassword = findViewById(R.id.textInputPassword)
        mButtonRegister.setOnClickListener(View.OnClickListener { clickRegister() })
    }

    fun clickRegister() {
        val name = mTextInputName!!.text.toString()
        val email = mTextInputEmail!!.text.toString()
        val vehicleBrand = mTextInputVehicleBrand!!.text.toString()
        val vehiclePlate = mTextInputVehiclePlate!!.text.toString()
        val password = mTextInputPassword!!.text.toString()
        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !vehicleBrand.isEmpty() && !vehiclePlate.isEmpty()) {
            if (password.length >= 6) {
                mDialog!!.show()
                register(name, email, password, vehicleBrand, vehiclePlate)
            } else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    fun register(name: String?, email: String?, password: String?, vehicleBrand: String?, vehiclePlate: String?) {
        mAuthProvider!!.register(email, password).addOnCompleteListener { task ->
            mDialog!!.hide()
            if (task.isSuccessful) {
                val id = FirebaseAuth.getInstance().currentUser!!.uid
                val driver = Driver(id, name, email, vehicleBrand, vehiclePlate)
                create(driver)
            } else {
                Toast.makeText(this@RegisteDriverActivity, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun create(driver: Driver?) {
        mDriverProvider!!.create(driver!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //Toast.makeText(RegisterDriverActivity.this, "El registro se realizo exitosamente", Toast.LENGTH_SHORT).show();
                val intent = Intent(this@RegisteDriverActivity, MapDriverActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this@RegisteDriverActivity, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}