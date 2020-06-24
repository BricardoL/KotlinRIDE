package com.proyecto.proyectotransporte.providers

import com.proyecto.proyectotransporte.models.FCMBody
import com.proyecto.proyectotransporte.models.FCMResponse
import com.proyecto.proyectotransporte.retrofit.IFCMApi
import com.proyecto.proyectotransporte.retrofit.RetrofitClient
import retrofit2.Call

class NotificationProvider {
    private val url = "https://fcm.googleapis.com"
    fun sendNotification(body: FCMBody?): Call<FCMResponse?>? {
        return RetrofitClient.getClientObject(url).create(IFCMApi::class.java).send(body)
    }
}