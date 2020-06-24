package com.proyecto.proyectotransporte.activities.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.activities.client.MapClientActivity
import com.proyecto.proyectotransporte.models.ClientBooking
import com.proyecto.proyectotransporte.models.FCMBody
import com.proyecto.proyectotransporte.models.FCMResponse
import com.proyecto.proyectotransporte.providers.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class RequestDriverActivity : AppCompatActivity() {
    private lateinit var mAnimation: LottieAnimationView
    private var mTextViewLookingFor: TextView? = null
    private var mButtonCancelRequest: Button? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mExtraOrigin: String? = null
    private var mExtraDestination: String? = null
    private var mExtraOriginLat = 0.0
    private var mExtraOriginLng = 0.0
    private var mExtraDestinationLat = 0.0
    private var mExtraDestinationLng = 0.0
    private var mOriginLatLng: LatLng? = null
    private var mDestinationLatLng: LatLng? = null
    private var mRadius = 0.1
    private var mDriverFound = false
    private var mIdDriverFound = ""
    private var mDriverFoundLatLng: LatLng? = null
    private var mNotificationProvider: NotificationProvider? = null
    private var mTokenProvider: TokenProvider? = null
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mAuthProvider: AuthProvider? = null
    private var mGoogleApiProvider: GoogleApiProvider? = null
    private var mListener: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_driver)
        mAnimation = findViewById(R.id.animation)
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor)
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest)
        mAnimation.playAnimation()
        mExtraOrigin = intent.getStringExtra("origin")
        mExtraDestination = intent.getStringExtra("destination")
        mExtraOriginLat = intent.getDoubleExtra("origin_lat", 0.0)
        mExtraOriginLng = intent.getDoubleExtra("origin_lng", 0.0)
        mExtraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        mExtraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)
        mOriginLatLng = LatLng(mExtraOriginLat, mExtraOriginLng)
        mDestinationLatLng = LatLng(mExtraDestinationLat, mExtraDestinationLng)
        mGeofireProvider = GeofireProvider()
        mTokenProvider = TokenProvider()
        mNotificationProvider = NotificationProvider()
        mClientBookingProvider = ClientBookingProvider()
        mAuthProvider = AuthProvider()
        mGoogleApiProvider = GoogleApiProvider(this@RequestDriverActivity)
        closestDriver
    }// NO ENCONTRO NINGUN CONDUCTOR

    // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
    private val closestDriver: Unit
        private get() {
            mGeofireProvider!!.getActiveDrivers(mOriginLatLng!!, mRadius).addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    if (!mDriverFound) {
                        mDriverFound = true
                        mIdDriverFound = key
                        mDriverFoundLatLng = LatLng(location.latitude, location.longitude)
                        mTextViewLookingFor!!.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"
                        createClientBooking()
                        Log.d("DRIVER", "ID: $mIdDriverFound")
                    }
                }

                override fun onKeyExited(key: String) {}
                override fun onKeyMoved(key: String, location: GeoLocation) {}
                override fun onGeoQueryReady() {
                    // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                    if (!mDriverFound) {
                        mRadius = mRadius + 0.1f
                        // NO ENCONTRO NINGUN CONDUCTOR
                        if (mRadius > 5) {
                            mTextViewLookingFor!!.text = "NO SE ENCONTRO UN CONDUCTOR"
                            Toast.makeText(this@RequestDriverActivity, "NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show()
                            return
                        } else {
                            closestDriver
                        }
                    }
                }

                override fun onGeoQueryError(error: DatabaseError) {}
            })
        }

    private fun createClientBooking() {
        mGoogleApiProvider!!.getDirections(mOriginLatLng!!, mDriverFoundLatLng!!)?.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                try {
                    val jsonObject = JSONObject(response.body())
                    val jsonArray = jsonObject.getJSONArray("routes")
                    val route = jsonArray.getJSONObject(0)
                    val polylines = route.getJSONObject("overview_polyline")
                    val points = polylines.getString("points")
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    val distance = leg.getJSONObject("distance")
                    val duration = leg.getJSONObject("duration")
                    val distanceText = distance.getString("text")
                    val durationText = duration.getString("text")
                    sendNotification(durationText, distanceText)
                } catch (e: Exception) {
                    Log.d("Error", "Error encontrado " + e.message)
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {}
        })
    }

    private fun sendNotification(time: String, km: String) {
        mTokenProvider!!.getToken(mIdDriverFound).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val token = dataSnapshot.child("token").value.toString()
                    val map: MutableMap<String, String> = HashMap()
                    map["title"] = "SOLICITUD DE SERVICIO A $time DE TU POSICION"
                    map["body"] = """
                        Un cliente esta solicitando un servicio a una distancia de $km
                        Recoger en: $mExtraOrigin
                        Destino: $mExtraDestination
                        """.trimIndent()
                    map["idClient"] = mAuthProvider!!.id
                    val fcmBody = FCMBody(token, "high", map)
                    mNotificationProvider!!.sendNotification(fcmBody)?.enqueue(object : Callback<FCMResponse?> {
                        override fun onResponse(call: Call<FCMResponse?>, response: Response<FCMResponse?>) {
                            if (response.body() != null) {
                                if (response.body()!!.success == 1) {
                                    val clientBooking = ClientBooking(
                                            mAuthProvider!!.id,
                                            mIdDriverFound,
                                            mExtraDestination!!,
                                            mExtraOrigin!!,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    )
                                    mClientBookingProvider!!.create(clientBooking).addOnSuccessListener { checkStatusClientBooking() }
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this@RequestDriverActivity, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@RequestDriverActivity, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                            Log.d("Error", "Error " + t.message)
                        }
                    })
                } else {
                    Toast.makeText(this@RequestDriverActivity, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun checkStatusClientBooking() {
        mListener = mClientBookingProvider!!.getStatus(mAuthProvider!!.id).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val status = dataSnapshot.value.toString()
                    if (status == "accept") {
                        val intent = Intent(this@RequestDriverActivity, MapClientBookingActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (status == "cancel") {
                        Toast.makeText(this@RequestDriverActivity, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RequestDriverActivity, MapClientActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mListener != null) {
            mClientBookingProvider!!.getStatus(mAuthProvider!!.id).removeEventListener(mListener!!)
        }
    }
}