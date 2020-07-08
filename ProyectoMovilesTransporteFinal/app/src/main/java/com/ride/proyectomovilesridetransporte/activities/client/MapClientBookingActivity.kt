package com.ride.proyectomovilesridetransporte.activities.client

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.providers.*
import com.ride.proyectomovilesridetransporte.utils.DecodePoints
import com.squareup.picasso.Picasso
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapClientBookingActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mAuthProvider: AuthProvider? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mTokenProvider: TokenProvider? = null
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mDriverProvider: DriverProvider? = null
    private var mImageViewBooking: ImageView? = null
    private var mMarkerDriver: Marker? = null
    private var mIsFirstTime = true
    private val mOrigin: String? = null
    private var mOriginLatLng: LatLng? = null
    private val mDestination: String? = null
    private var mDestinationLatLng: LatLng? = null
    private var mDriverLatLng: LatLng? = null
    private var mTextViewClientBooking: TextView? = null
    private var mTextViewEmailClientBooking: TextView? = null
    private var mTextViewOriginClientBooking: TextView? = null
    private var mTextViewDestinationClientBooking: TextView? = null
    private var mTextViewStatusBooking: TextView? = null
    private var mGoogleApiProvider: GoogleApiProvider? = null
    private var mPolylineList: List<LatLng?>? = null
    private var mPolylineOptions: PolylineOptions? = null
    private var mListener: ValueEventListener? = null
    private var mIdDriver: String? = null
    private var mListenerStatus: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_client_booking)
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider("drivers_working")
        mTokenProvider = TokenProvider()
        mClientBookingProvider = ClientBookingProvider()
        mGoogleApiProvider = GoogleApiProvider(this@MapClientBookingActivity)
        mDriverProvider = DriverProvider()
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        }
        mTextViewClientBooking = findViewById(R.id.textViewDriverBooking)
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailDriverBooking)
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking)
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginDriverBooking)
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationDriverBooking)
        mImageViewBooking = findViewById(R.id.imageViewClientBooking)
        status
        clientBooking
    }

    private val status: Unit
        private get() {
            mListenerStatus = mClientBookingProvider!!.getStatus(mAuthProvider!!.id).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val status = dataSnapshot.value.toString()
                        if (status == "accept") {
                            mTextViewStatusBooking!!.text = "Estado: Aceptado"
                        }
                        if (status == "start") {
                            mTextViewStatusBooking!!.text = "Estado: Viaje Iniciado"
                            startBooking()
                        } else if (status == "finish") {
                            mTextViewStatusBooking!!.text = "Estado: Viaje Finalizado"
                            finishBooking()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun finishBooking() {
        val intent = Intent(this@MapClientBookingActivity, CalificationDriverActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startBooking() {
        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions().position(mDestinationLatLng!!).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_blue)))
        drawRoute(mDestinationLatLng)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mListener != null) {
            mGeofireProvider!!.getDriverLocation(mIdDriver).removeEventListener(mListener!!)
        }
        if (mListenerStatus != null) {
            mClientBookingProvider!!.getStatus(mAuthProvider!!.id).removeEventListener(mListenerStatus!!)
        }
    }

    private val clientBooking: Unit
        private get() {
            mClientBookingProvider!!.getClientBooking(mAuthProvider!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val destination = dataSnapshot.child("destination").value.toString()
                        val origin = dataSnapshot.child("origin").value.toString()
                        val idDriver = dataSnapshot.child("idDriver").value.toString()
                        mIdDriver = idDriver
                        val destinatioLat = dataSnapshot.child("destinationLat").value.toString().toDouble()
                        val destinatioLng = dataSnapshot.child("destinationLng").value.toString().toDouble()
                        val originLat = dataSnapshot.child("originLat").value.toString().toDouble()
                        val originLng = dataSnapshot.child("originLng").value.toString().toDouble()
                        mOriginLatLng = LatLng(originLat, originLng)
                        mDestinationLatLng = LatLng(destinatioLat, destinatioLng)
                        mTextViewOriginClientBooking!!.text = "recoger en: $origin"
                        mTextViewDestinationClientBooking!!.text = "destino: $destination"
                        mMap!!.addMarker(MarkerOptions().position(mOriginLatLng!!).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_red)))
                        getDriver(idDriver)
                        getDriverLocation(idDriver)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun getDriver(idDriver: String) {
        mDriverProvider!!.getDriver(idDriver).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").value.toString()
                    val email = dataSnapshot.child("email").value.toString()
                    var image = ""
                    if (dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").value.toString()
                        Picasso.with(this@MapClientBookingActivity).load(image).into(mImageViewBooking)
                    }
                    mTextViewClientBooking!!.text = name
                    mTextViewEmailClientBooking!!.text = email
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun getDriverLocation(idDriver: String) {
        mListener = mGeofireProvider!!.getDriverLocation(idDriver).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val lat = dataSnapshot.child("0").value.toString().toDouble()
                    val lng = dataSnapshot.child("1").value.toString().toDouble()
                    mDriverLatLng = LatLng(lat, lng)
                    if (mMarkerDriver != null) {
                        mMarkerDriver!!.remove()
                    }
                    mMarkerDriver = mMap!!.addMarker(MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_drive)))
                    if (mIsFirstTime) {
                        mIsFirstTime = false
                        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(15f)
                                        .build()
                        ))
                        drawRoute(mOriginLatLng)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun drawRoute(latLng: LatLng?) {
        mGoogleApiProvider!!.getDirections(mDriverLatLng, latLng).enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                try {
                    val jsonObject = JSONObject(response.body())
                    val jsonArray = jsonObject.getJSONArray("routes")
                    val route = jsonArray.getJSONObject(0)
                    val polylines = route.getJSONObject("overview_polyline")
                    val points = polylines.getString("points")
                    mPolylineList = DecodePoints.decodePoly(points) as List<LatLng?>?
                    mPolylineOptions = PolylineOptions()
                    mPolylineOptions!!.color(Color.DKGRAY)
                    mPolylineOptions!!.width(13f)
                    mPolylineOptions!!.startCap(SquareCap())
                    mPolylineOptions!!.jointType(JointType.ROUND)
                    mPolylineOptions!!.addAll(mPolylineList)
                    mMap!!.addPolyline(mPolylineOptions)
                    val legs = route.getJSONArray("legs")
                    val leg = legs.getJSONObject(0)
                    val distance = leg.getJSONObject("distance")
                    val duration = leg.getJSONObject("duration")
                    val distanceText = distance.getString("text")
                    val durationText = duration.getString("text")
                } catch (e: Exception) {
                    Log.d("Error", "Error encontrado " + e.message)
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.isMyLocationEnabled = true
    }
}