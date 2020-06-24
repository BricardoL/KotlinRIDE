package com.proyecto.proyectotransporte.retrofit

import com.proyecto.proyectotransporte.models.FCMBody
import com.proyecto.proyectotransporte.models.FCMResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMApi {
    @Headers("Content-Type:application/json", "Authorization:key=AAAAGbb7VBc:APA91bHuhOmvLR5CcERn9jMrimIVlhIQmduh_v5xkG8dLefoPkhA-VN7HVgvIBssnxCBVbyMfu1z7r456bilGrL0uAaWheJPqLc1r9QZ_URKgoqBl6N0h9P_RrtLNiZIdYHvgzmhWcmR")
    @POST("fcm/send")
    fun send(@Body body: FCMBody?): Call<FCMResponse?>?
}