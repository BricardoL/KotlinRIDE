package com.proyecto.proyectotransporte.includes

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.proyecto.proyectotransporte.R

object Mytoolbar {
    @JvmStatic
    fun show(activity: AppCompatActivity, title: String?, upButton: Boolean) {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar!!.title = title
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(upButton)
    }
}