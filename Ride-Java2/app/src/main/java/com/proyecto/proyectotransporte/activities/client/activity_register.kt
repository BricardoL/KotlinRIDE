package com.proyecto.proyectotransporte.activities.client

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
import com.proyecto.proyectotransporte.models.Client
import com.proyecto.proyectotransporte.providers.AuthProvider
import com.proyecto.proyectotransporte.providers.ClientProvider
import dmax.dialog.SpotsDialog

class activity_register : AppCompatActivity() {
    var mAuthProvider: AuthProvider? = null
    var mClientProvider: ClientProvider? = null

    // VIEWS
    lateinit var mButtonRegister: Button
    var mTextInputEmail: TextInputEditText? = null
    var mTextInputName: TextInputEditText? = null
    var mTextInputPassword: TextInputEditText? = null
    var mDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        show(this, "Registro de usuario", true)
        mAuthProvider = AuthProvider()
        mClientProvider = ClientProvider()
        mDialog = SpotsDialog.Builder().setContext(this@activity_register).setMessage("Espere un momento").build()
        mButtonRegister = findViewById(R.id.btnRegister)
        mTextInputEmail = findViewById(R.id.textInputEmail)
        mTextInputName = findViewById(R.id.textInputName)
        mTextInputPassword = findViewById(R.id.textInputPassword)

        mButtonRegister.setOnClickListener(View.OnClickListener { clickRegister() })
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
        mAuthProvider!!.register(email, password).addOnCompleteListener { task ->
            mDialog!!.hide()
            if (task.isSuccessful) {
                val id = FirebaseAuth.getInstance().currentUser!!.uid
                val client = Client(id, name, email)
                create(client)
            } else {
                Toast.makeText(this@activity_register, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun create(client: Client?) {
        mClientProvider!!.create(client!!).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this@activity_register, MapClientActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this@activity_register, "No se pudo crear el cliente", Toast.LENGTH_SHORT).show()
            }
        }
    } /*
    void saveUser(String id, String name, String email) {
        String selectedUser = mPref.getString("user", "");
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        if (selectedUser.equals("driver")) {
            mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (selectedUser.equals("client")){
            mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    */
}