package com.ride.proyectomovilesridetransporte.providers

import com.ride.proyectomovilesridetransporte.models.FCMBody
import com.ride.proyectomovilesridetransporte.models.FCMResponse
import com.ride.proyectomovilesridetransporte.retrofit.IFCMApi
import com.ride.proyectomovilesridetransporte.retrofit.RetrofitClient
import retrofit2.Call

class NotificationProvider {
    private val url = "https://fcm.googleapis.com"
    fun sendNotification(body: FCMBody?): Call<FCMResponse?>? {
        return RetrofitClient.getClientObject(url).create(IFCMApi::class.java).send(body)
    }
}