package com.ride.proyectomovilesridetransporte.activities.client

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.models.ClientBooking
import com.ride.proyectomovilesridetransporte.models.HistoryBooking
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.ClientBookingProvider
import com.ride.proyectomovilesridetransporte.providers.HistoryBookingProvider
import java.util.*

class CalificationDriverActivity : AppCompatActivity() {
    private var mTextViewOrigin: TextView? = null
    private var mTextViewDestination: TextView? = null
    private var mTextViewPrice: TextView? = null
    private lateinit var mRatinBar: RatingBar
    private lateinit var mButtonCalification: Button
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mAuthProvider: AuthProvider? = null
    private lateinit var mHistoryBooking: HistoryBooking;
    private var mHistoryBookingProvider: HistoryBookingProvider? = null
    private var mCalification = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calification_driver)
        mTextViewDestination = findViewById(R.id.textViewDestinationCalification)
        mTextViewOrigin = findViewById(R.id.textViewOriginCalification)
        mRatinBar = findViewById(R.id.ratingbarCalification)
        mButtonCalification = findViewById(R.id.btnCalification)
        mTextViewPrice = findViewById(R.id.textViewPrice)
        mClientBookingProvider = ClientBookingProvider()
        mHistoryBookingProvider = HistoryBookingProvider()
        mAuthProvider = AuthProvider()
        mRatinBar.onRatingBarChangeListener = OnRatingBarChangeListener { ratingBar, calification, b -> mCalification = calification }
        mButtonCalification.setOnClickListener { calificate() }
        clientBooking
    }

    private val clientBooking: Unit
        private get() {
            mClientBookingProvider!!.getClientBooking(mAuthProvider!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val clientBooking = dataSnapshot.getValue(ClientBooking::class.java)
                        mTextViewOrigin!!.text = clientBooking!!.origin
                        mTextViewPrice!!.text = String.format("%.1f", clientBooking.price) + "$"
                        mTextViewDestination!!.text = clientBooking.destination
                        mHistoryBooking = HistoryBooking(
                                clientBooking.idHistoryBooking,
                                clientBooking.idClient,
                                clientBooking.idDriver,
                                clientBooking.destination,
                                clientBooking.origin,
                                clientBooking.time,
                                clientBooking.km,
                                clientBooking.status,
                                clientBooking.originLat,
                                clientBooking.originLng,
                                clientBooking.destinationLat,
                                clientBooking.destinationLng
                        )
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun calificate() {
        if (mCalification > 0) {
            mHistoryBooking!!.calificationDriver = mCalification.toDouble()
            mHistoryBooking!!.timestamp = Date().time
            mHistoryBookingProvider!!.getHistoryBooking(mHistoryBooking!!.idHistoryBooking).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mHistoryBookingProvider!!.updateCalificactionDriver(mHistoryBooking!!.idHistoryBooking, mCalification).addOnSuccessListener {
                            Toast.makeText(this@CalificationDriverActivity, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@CalificationDriverActivity, MapClientActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        mHistoryBookingProvider!!.create(mHistoryBooking).addOnSuccessListener {
                            Toast.makeText(this@CalificationDriverActivity, "La calificacion se guardo correctamente", Toast.LENGTH_LONG).show()
                            val intent = Intent(this@CalificationDriverActivity, MapClientActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        } else {
            Toast.makeText(this, "Debes ingresar la calificacion para continuar", Toast.LENGTH_SHORT).show()
        }
    }
}