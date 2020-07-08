package com.ride.proyectomovilesridetransporte.activities.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.FirebaseDatabase
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.adapters.HistoryBookingClientAdapter
import com.ride.proyectomovilesridetransporte.includes.MyToolbar
import com.ride.proyectomovilesridetransporte.models.HistoryBooking
import com.ride.proyectomovilesridetransporte.providers.AuthProvider

class HistoryBookingClientActivity : AppCompatActivity() {
    private lateinit var mReciclerView: RecyclerView
    private var mAdapter: HistoryBookingClientAdapter? = null
    private var mAuthProvider: AuthProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_booking_client)
        MyToolbar.show(this, "Historial de viajes", true)
        mReciclerView = findViewById(R.id.recyclerViewHistoryBooking)
        val linearLayoutManager = LinearLayoutManager(this)
        mReciclerView.setLayoutManager(linearLayoutManager)
    }

    override fun onStart() {
        super.onStart()
        mAuthProvider = AuthProvider()
        val query = FirebaseDatabase.getInstance().reference
                .child("HistoryBooking")
                .orderByChild("idClient")
                .equalTo(mAuthProvider!!.id)
        val options = FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query, HistoryBooking::class.java)
                .build()
        mAdapter = HistoryBookingClientAdapter(options, this@HistoryBookingClientActivity)
        mReciclerView!!.adapter = mAdapter
        mAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        mAdapter!!.stopListening()
    }
}