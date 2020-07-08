package com.ride.proyectomovilesridetransporte.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.client.HistoryBookingDetailClientActivity
import com.ride.proyectomovilesridetransporte.models.HistoryBooking
import com.ride.proyectomovilesridetransporte.providers.DriverProvider
import com.squareup.picasso.Picasso

class HistoryBookingClientAdapter(options: FirebaseRecyclerOptions<HistoryBooking?>?, context: Context) : FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder>(options!!) {
    private val mDriverProvider: DriverProvider
    private val mContext: Context
    override fun onBindViewHolder(holder: ViewHolder, position: Int, historyBooking: HistoryBooking) {
        val id = getRef(position).key
        holder.textViewOrigin.text = historyBooking.origin
        holder.textViewDestination.text = historyBooking.destination
        holder.textViewCalification.text = historyBooking.calificationClient.toString()
        mDriverProvider.getDriver(historyBooking.idDriver).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").value.toString()
                    holder.textViewName.text = name
                    if (dataSnapshot.hasChild("image")) {
                        val image = dataSnapshot.child("image").value.toString()
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        holder.mView.setOnClickListener {
            val intent = Intent(mContext, HistoryBookingDetailClientActivity::class.java)
            intent.putExtra("idHistoryBooking", id)
            mContext.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_history_booking, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val textViewName: TextView
        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewCalification: TextView
        val imageViewHistoryBooking: ImageView

        init {
            textViewName = mView.findViewById(R.id.textViewName)
            textViewOrigin = mView.findViewById(R.id.textViewOrigin)
            textViewDestination = mView.findViewById(R.id.textViewDestination)
            textViewCalification = mView.findViewById(R.id.textViewCalification)
            imageViewHistoryBooking = mView.findViewById(R.id.imageViewHistoryBooking)
        }
    }

    init {
        mDriverProvider = DriverProvider()
        mContext = context
    }
}