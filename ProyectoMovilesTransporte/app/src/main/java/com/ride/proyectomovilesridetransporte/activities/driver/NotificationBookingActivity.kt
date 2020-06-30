package com.ride.proyectomovilesridetransporte.activities.driver

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.driver.MapDriverActivity
import com.ride.proyectomovilesridetransporte.activities.driver.MapDriverBookingActivity
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.ClientBookingProvider
import com.ride.proyectomovilesridetransporte.providers.GeofireProvider

class NotificationBookingActivity : AppCompatActivity() {
    private lateinit var mTextViewDestination: TextView
    private lateinit var mTextViewOrigin: TextView
    private lateinit var mTextViewMin: TextView
    private lateinit var mTextViewDistance: TextView
    private var mTextViewCounter: TextView? = null
    private lateinit var mbuttonAccept: Button
    private lateinit var mbuttonCancel: Button
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mAuthProvider: AuthProvider? = null
    private var mExtraIdClient: String? = null
    private var mExtraOrigin: String? = null
    private var mExtraDestination: String? = null
    private var mExtraMin: String? = null
    private var mExtraDistance: String? = null
    private lateinit var mMediaPlayer: MediaPlayer
    private var mCounter = 10
    private var mHandler: Handler? = null
    var runnable = Runnable {
        mCounter = mCounter - 1
        mTextViewCounter!!.text = mCounter.toString()
        if (mCounter > 0) {
            initTimer()
        } else {
            cancelBooking()
        }
    }
    private var mListener: ValueEventListener? = null
    private fun initTimer() {
        mHandler = Handler()
        mHandler!!.postDelayed(runnable, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_booking)
        mTextViewDestination = findViewById(R.id.textViewDestination)
        mTextViewOrigin = findViewById(R.id.textViewOrigin)
        mTextViewMin = findViewById(R.id.textViewMin)
        mTextViewDistance = findViewById(R.id.textViewDistance)
        mTextViewCounter = findViewById(R.id.textViewCounter)
        mbuttonAccept = findViewById(R.id.btnAcceptBooking)
        mbuttonCancel = findViewById(R.id.btnCancelBooking)
        mExtraIdClient = intent.getStringExtra("idClient")
        mExtraOrigin = intent.getStringExtra("origin")
        mExtraDestination = intent.getStringExtra("destination")
        mExtraMin = intent.getStringExtra("min")
        mExtraDistance = intent.getStringExtra("distance")
        mTextViewDestination.setText(mExtraDestination)
        mTextViewOrigin.setText(mExtraOrigin)
        mTextViewMin.setText(mExtraMin)
        mTextViewDistance.setText(mExtraDistance)
        mMediaPlayer = MediaPlayer.create(this, R.raw.ringtone)
        mMediaPlayer.setLooping(true)
        mClientBookingProvider = ClientBookingProvider()
        window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        initTimer()
        checkIfClientCancelBooking()
        mbuttonAccept.setOnClickListener { acceptBooking() }
        mbuttonCancel.setOnClickListener { cancelBooking() }
    }

    private fun checkIfClientCancelBooking() {
        mListener = mClientBookingProvider!!.getClientBooking(mExtraIdClient).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(this@NotificationBookingActivity, "El cliente cancelo el viaje", Toast.LENGTH_LONG).show()
                    if (mHandler != null) mHandler!!.removeCallbacks(runnable)
                    val intent = Intent(this@NotificationBookingActivity, MapDriverActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun cancelBooking() {
        if (mHandler != null) mHandler!!.removeCallbacks(runnable)
        mClientBookingProvider!!.updateStatus(mExtraIdClient, "cancel")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent = Intent(this@NotificationBookingActivity, MapDriverActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun acceptBooking() {
        if (mHandler != null) mHandler!!.removeCallbacks(runnable)
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider("active_drivers")
        mGeofireProvider!!.removeLocation(mAuthProvider!!.id)
        mClientBookingProvider = ClientBookingProvider()
        mClientBookingProvider!!.updateStatus(mExtraIdClient, "accept")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(2)
        val intent1 = Intent(this@NotificationBookingActivity, MapDriverBookingActivity::class.java)
        intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent1.action = Intent.ACTION_RUN
        intent1.putExtra("idClient", mExtraIdClient)
        startActivity(intent1)
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.release()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mMediaPlayer != null) {
            if (!mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mHandler != null) mHandler!!.removeCallbacks(runnable)
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
            }
        }
        if (mListener != null) {
            mClientBookingProvider!!.getClientBooking(mExtraIdClient).removeEventListener(mListener!!)
        }
    }
}