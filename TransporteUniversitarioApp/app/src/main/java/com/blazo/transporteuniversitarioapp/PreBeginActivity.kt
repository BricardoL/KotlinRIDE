package com.blazo.transporteuniversitarioapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil

import com.blazo.transporteuniversitarioapp.databinding.ActivityPreBeginBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PreBeginActivity : AppCompatActivity() {
    lateinit var binding: ActivityPreBeginBinding
    lateinit var mAuth: FirebaseAuth
    lateinit var mDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_pre_begin
        )
       // mAuth = FirebaseAuth.getInstance()
       // mDatabase = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().currentUser!!.uid)

        binding.btnClienteprebg.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnConductorpreb.setOnClickListener {
            startActivity(Intent(this, ConductorLoginActivity::class.java))
        }

        /*binding.tvCerrarsesionPrebegin.setOnClickListener {
            mAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        mDatabase.child("Name").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val username = p0.getValue()
                binding.name.text = username.toString()
            }

        }) */


    }




}
