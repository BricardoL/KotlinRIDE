package com.ride.proyectomovilesridetransporte.activities.driver

import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.models.HistoryBooking
import com.ride.proyectomovilesridetransporte.providers.ClientProvider
import com.ride.proyectomovilesridetransporte.providers.HistoryBookingProvider
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class HistoryBookingDetailDriverActivity : AppCompatActivity() {
    private var mTextViewName: TextView? = null
    private var mTextViewOrigin: TextView? = null
    private var mTextViewDestination: TextView? = null
    private var mTextViewYourCalification: TextView? = null
    private var mRatingBarCalification: RatingBar? = null
    private var mCircleImage: CircleImageView? = null
    private lateinit var mCircleImageBack: CircleImageView
    private var mExtraId: String? = null
    private var mHistoryBookingProvider: HistoryBookingProvider? = null
    private var mClientProvider: ClientProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_booking_detail_driver)
        mTextViewName = findViewById(R.id.textViewNameBookingDetail)
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail)
        mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail)
        mTextViewYourCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail)
        mRatingBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail)
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail)
        mCircleImageBack = findViewById(R.id.circleImageBack)
        mClientProvider = ClientProvider()
        mExtraId = intent.getStringExtra("idHistoryBooking")
        mHistoryBookingProvider = HistoryBookingProvider()
        historyBooking
        mCircleImageBack.setOnClickListener(View.OnClickListener { finish() })
    }

    private val historyBooking: Unit
        private get() {
            mHistoryBookingProvider!!.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val historyBooking = dataSnapshot.getValue(HistoryBooking::class.java)
                        mTextViewOrigin!!.text = historyBooking!!.origin
                        mTextViewDestination!!.text = historyBooking.destination
                        mTextViewYourCalification!!.text = "Tu calificacion: " + historyBooking.calificationDriver
                        if (dataSnapshot.hasChild("calificationClient")) {
                            mRatingBarCalification!!.rating = historyBooking.calificationClient.toFloat()
                        }
                        mClientProvider!!.getClient(historyBooking.idClient).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val name = dataSnapshot.child("name").value.toString()
                                    mTextViewName!!.text = name.toUpperCase()
                                    if (dataSnapshot.hasChild("image")) {
                                        val image = dataSnapshot.child("image").value.toString()
                                        Picasso.with(this@HistoryBookingDetailDriverActivity).load(image).into(mCircleImage)
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
}