package com.ride.proyectomovilesridetransporte.retrofit

import com.ride.proyectomovilesridetransporte.models.FCMBody
import com.ride.proyectomovilesridetransporte.models.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {
    @Headers("Content-Type:application/json", "Authorization:key=AAAAWX45EEk:APA91bEgC4fFFfFoJib_dRRSStaQ_bwSHuO63FRnKgdvRjXYU68Yit7FCFjN3DXs2PVjQyySQVLC3OdU2Yqu-IXumIsrNbm3B-olaqSwKAbYElTisNkbIFwig9pXDfboedMJMWNxhgPd")
    @POST("fcm/send")
    fun send(@Body body: FCMBody?): Call<FCMResponse?>?
}