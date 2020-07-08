package com.ride.proyectomovilesridetransporte.activities.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.driver.MapDriverBookingActivity
import com.ride.proyectomovilesridetransporte.models.FCMBody
import com.ride.proyectomovilesridetransporte.models.FCMResponse
import com.ride.proyectomovilesridetransporte.models.Info
import com.ride.proyectomovilesridetransporte.providers.*
import com.ride.proyectomovilesridetransporte.utils.DecodePoints
import com.squareup.picasso.Picasso
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MapDriverBookingActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mAuthProvider: AuthProvider? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mClientProvider: ClientProvider? = null
    private var mClientBookingProvider: ClientBookingProvider? = null
    private var mTokenProvider: TokenProvider? = null
    private var mNotificationProvider: NotificationProvider? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocation: FusedLocationProviderClient? = null
    private var mMarker: Marker? = null
    private var mCurrentLatLng: LatLng? = null
    private var mTextViewClientBooking: TextView? = null
    private var mTextViewEmailClientBooking: TextView? = null
    private var mTextViewOriginClientBooking: TextView? = null
    private var mTextViewDestinationClientBooking: TextView? = null
    private var mTextViewTime: TextView? = null
    private var mImageViewBooking: ImageView? = null
    private var mExtraClientId: String? = null
    private var mOriginLatLng: LatLng? = null
    private var mDestinationLatLng: LatLng? = null
    private var mGoogleApiProvider: GoogleApiProvider? = null
    private var mPolylineList: List<LatLng?>? = null
    private var mPolylineOptions: PolylineOptions? = null
    private var mInfoProvider: InfoProvider? = null
    private var mInfo: Info? = null
    private var mIsFirstTime = true
    private var mIsCloseToClient = false
    private lateinit var mButtonStartBooking: Button
    private lateinit var mButtonFinishBooking: Button
    var mDistanceInmeters = 1.0
    var mMinutes = 0
    var mSeconds = 0
    var mSecondsIsOver = false
    var mRideStart = false
    var mHandler: Handler? = Handler()
    var mPriviusLocation = Location("")
    var runnable: Runnable = object : Runnable {
        override fun run() {
            mSeconds++
            if (!mSecondsIsOver) {
                mTextViewTime!!.text = "$mSeconds Seg"
            } else {
                mTextViewTime!!.text = "$mMinutes Min$mSeconds"
            }
            if (mSeconds == 59) {
                mSeconds = 0
                mSecondsIsOver = true
                mMinutes++
            }
            mHandler!!.postDelayed(this, 1000)
        }
    }
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (applicationContext != null) {
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    if (mMarker != null) {
                        mMarker!!.remove()
                    }
                    if (mRideStart) {
                        mDistanceInmeters = mDistanceInmeters + mPriviusLocation.distanceTo(location)
                        Log.d("ENTRO", "Distancia recorrida: $mDistanceInmeters")
                    }
                    mPriviusLocation = location
                    mMarker = mMap!!.addMarker(MarkerOptions().position(
                            LatLng(location.latitude, location.longitude)
                    )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_drive))
                    )
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(16f)
                                    .build()
                    ))
                    updateLocation()
                    if (mIsFirstTime) {
                        mIsFirstTime = false
                        clientBooking
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_driver_booking)
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider("drivers_working")
        mTokenProvider = TokenProvider()
        mClientProvider = ClientProvider()
        mClientBookingProvider = ClientBookingProvider()
        mNotificationProvider = NotificationProvider()
        mInfoProvider = InfoProvider()
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        mTextViewClientBooking = findViewById(R.id.textViewClientBooking)
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking)
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking)
        mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking)
        mButtonStartBooking = findViewById(R.id.btnStartBooking)
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking)
        mImageViewBooking = findViewById(R.id.imageViewClientBooking)
        mTextViewTime = findViewById(R.id.textViewCard)
        info
        mExtraClientId = intent.getStringExtra("idClient")
        mGoogleApiProvider = GoogleApiProvider(this@MapDriverBookingActivity)
        client
        mButtonStartBooking.setOnClickListener(View.OnClickListener {
            if (mIsCloseToClient) {
                startBooking()
            } else {
                Toast.makeText(this@MapDriverBookingActivity, "Debes estar mas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show()
            }
        })
        mButtonFinishBooking.setOnClickListener(View.OnClickListener { finishBooking() })
    }

    private fun calculeRide() {
        if (mMinutes == 0) {
            mMinutes = 1
        }
        val priceMinute = mMinutes * mInfo!!.min
        val priceKm = mDistanceInmeters / 1000 * mInfo!!.km
        Log.d("VALORES: ", "Minu Total:$priceMinute")
        Log.d("VALORES: ", "Km Total:" + mDistanceInmeters / 1000)
        val total = priceMinute + priceKm
        mClientBookingProvider!!.updatePrice(mExtraClientId, total).addOnSuccessListener {
            mClientBookingProvider!!.updateStatus(mExtraClientId, "finish")
            val intent = Intent(this@MapDriverBookingActivity, CalificationClientActivity::class.java)
            intent.putExtra("idClient", mExtraClientId)
            intent.putExtra("price", total)
            startActivity(intent)
            finish()
        }
    }

    private val info: Unit
        private get() {
            mInfoProvider!!.info.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mInfo = dataSnapshot.getValue(Info::class.java)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun finishBooking() {
        mClientBookingProvider!!.updateIdHistoryBooking(mExtraClientId).addOnSuccessListener {
            sendNotification("Viaje finalizado")
            if (mFusedLocation != null) {
                mFusedLocation!!.removeLocationUpdates(mLocationCallback)
            }
            mGeofireProvider!!.removeLocation(mAuthProvider!!.id)
            if (mHandler != null) {
                mHandler!!.removeCallbacks(runnable)
            }
            calculeRide()
        }
    }

    private fun startBooking() {
        mClientBookingProvider!!.updateStatus(mExtraClientId, "start")
        mButtonStartBooking!!.visibility = View.GONE
        mButtonFinishBooking!!.visibility = View.VISIBLE
        mMap!!.clear()
        mMap!!.addMarker(MarkerOptions().position(mDestinationLatLng!!).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_blue)))
        drawRoute(mDestinationLatLng)
        sendNotification("Viaje iniciado")
        mRideStart = true
        mHandler!!.postDelayed(runnable, 1000)
    }

    private fun getDistanceBetween(clientLatLng: LatLng, driverLatLng: LatLng): Double {
        var distance = 0.0
        val clientLocation = Location("")
        val driverLocation = Location("")
        clientLocation.latitude = clientLatLng.latitude
        clientLocation.longitude = clientLatLng.longitude
        driverLocation.latitude = driverLatLng.latitude
        driverLocation.longitude = driverLatLng.longitude
        distance = clientLocation.distanceTo(driverLocation).toDouble()
        return distance
    }

    private val clientBooking: Unit
        private get() {
            mClientBookingProvider!!.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val destination = dataSnapshot.child("destination").value.toString()
                        val origin = dataSnapshot.child("origin").value.toString()
                        val destinatioLat = dataSnapshot.child("destinationLat").value.toString().toDouble()
                        val destinatioLng = dataSnapshot.child("destinationLng").value.toString().toDouble()
                        val originLat = dataSnapshot.child("originLat").value.toString().toDouble()
                        val originLng = dataSnapshot.child("originLng").value.toString().toDouble()
                        mOriginLatLng = LatLng(originLat, originLng)
                        mDestinationLatLng = LatLng(destinatioLat, destinatioLng)
                        mTextViewOriginClientBooking!!.text = "recoger en: $origin"
                        mTextViewDestinationClientBooking!!.text = "destino: $destination"
                        mMap!!.addMarker(MarkerOptions().position(mOriginLatLng!!).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_red)))
                        drawRoute(mOriginLatLng)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun drawRoute(latLng: LatLng?) {
        mGoogleApiProvider!!.getDirections(mCurrentLatLng, latLng).enqueue(object : Callback<String?> {
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

    private val client: Unit
        private get() {
            mClientProvider!!.getClient(mExtraClientId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val email = dataSnapshot.child("email").value.toString()
                        val name = dataSnapshot.child("name").value.toString()
                        var image = ""
                        if (dataSnapshot.hasChild("image")) {
                            image = dataSnapshot.child("image").value.toString()
                            Picasso.with(this@MapDriverBookingActivity).load(image).into(mImageViewBooking)
                        }
                        mTextViewClientBooking!!.text = name
                        mTextViewEmailClientBooking!!.text = email
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun updateLocation() {
        if (mAuthProvider!!.existSession() && mCurrentLatLng != null) {
            mGeofireProvider!!.saveLocation(mAuthProvider!!.id, mCurrentLatLng)
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    val distance = getDistanceBetween(mOriginLatLng!!, mCurrentLatLng!!) // METROS
                    if (distance <= 200) {
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true
                        Toast.makeText(this, "Estas cerca a la posicion de recogida", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.isMyLocationEnabled = false
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.smallestDisplacement = 5f
        startLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                    } else {
                        showAlertDialogNOGPS()
                    }
                } else {
                    checkLocationPermissions()
                }
            } else {
                checkLocationPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            mFusedLocation!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        } else {
            showAlertDialogNOGPS()
        }
    }

    private fun showAlertDialogNOGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Por favor activa tu ubicacion para continuar")
                .setPositiveButton("Configuraciones") { dialogInterface, i -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE) }.create().show()
    }

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

    private fun disconnect() {
        if (mFusedLocation != null) {
            mFusedLocation!!.removeLocationUpdates(mLocationCallback)
            if (mAuthProvider!!.existSession()) {
                mGeofireProvider!!.removeLocation(mAuthProvider!!.id)
            }
        } else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                } else {
                    showAlertDialogNOGPS()
                }
            } else {
                checkLocationPermissions()
            }
        } else {
            if (gpsActived()) {
                mFusedLocation!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            } else {
                showAlertDialogNOGPS()
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos de ubicacion para poder utilizarse")
                        .setPositiveButton("OK") { dialogInterface, i -> ActivityCompat.requestPermissions(this@MapDriverBookingActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE) }
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this@MapDriverBookingActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    private fun sendNotification(status: String) {
        mTokenProvider!!.getToken(mExtraClientId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val token = dataSnapshot.child("token").value.toString()
                    val map: MutableMap<String, String> = HashMap()
                    map["title"] = "ESTADO DE TU VIAJE"
                    map["body"] = "Tu estado del viaje es: $status"
                    val fcmBody = FCMBody(token, "high", "4500s", map)
                    mNotificationProvider!!.sendNotification(fcmBody).enqueue(object : Callback<FCMResponse?> {
                        override fun onResponse(call: Call<FCMResponse?>, response: Response<FCMResponse?>) {
                            if (response.body() != null) {
                                if (response.body()!!.success != 1) {
                                    Toast.makeText(this@MapDriverBookingActivity, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@MapDriverBookingActivity, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                            Log.d("Error", "Error " + t.message)
                        }
                    })
                } else {
                    Toast.makeText(this@MapDriverBookingActivity, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val SETTINGS_REQUEST_CODE = 2
    }
}