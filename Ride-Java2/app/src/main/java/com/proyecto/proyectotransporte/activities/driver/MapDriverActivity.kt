package com.proyecto.proyectotransporte.activities.driver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
import com.proyecto.proyectotransporte.R
import com.proyecto.proyectotransporte.activities.MainActivity
import com.proyecto.proyectotransporte.activities.driver.MapDriverActivity
import com.proyecto.proyectotransporte.includes.Mytoolbar.show
import com.proyecto.proyectotransporte.providers.AuthProvider
import com.proyecto.proyectotransporte.providers.GeofireProvider
import com.proyecto.proyectotransporte.providers.TokenProvider

class MapDriverActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mAuthProvider: AuthProvider? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocation: FusedLocationProviderClient? = null
    private var mTokenProvider: TokenProvider? = null
    private var mMarker: Marker? = null
    private lateinit var mButtonConnect: Button
    private var mIsConnect = false
    private var mCurrentLatLng: LatLng? = null
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (applicationContext != null) {
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    if (mMarker != null) {
                        mMarker!!.remove()
                    }
                    mMarker = mMap!!.addMarker(MarkerOptions().position(
                            LatLng(location.latitude, location.longitude)
                    )
                            .title("Tu posicion")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.driver_icon))
                    )
                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(16f)
                                    .build()
                    ))
                    updateLocation()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_driver)
        show(this, "Conductor", false)
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider()
        mTokenProvider = TokenProvider()
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        mButtonConnect = findViewById(R.id.btnConnect)
        mButtonConnect.setOnClickListener(View.OnClickListener {
            if (mIsConnect) {
                disconnect()
            } else {
                startLocation()
            }
        })
        generateToken()
    }

    private fun updateLocation() {
        if (mAuthProvider!!.existSession() && mCurrentLatLng != null) {
            mGeofireProvider!!.saveLocation(mAuthProvider!!.id, mCurrentLatLng!!)
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
            mButtonConnect!!.text = "Conectarse"
            mIsConnect = false
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
                    mButtonConnect!!.text = "Desconectarse"
                    mIsConnect = true
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
                        .setPositiveButton("OK") { dialogInterface, i -> ActivityCompat.requestPermissions(this@MapDriverActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE) }
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this@MapDriverActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.driver_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            logout()
        }
        return super.onOptionsItemSelected(item)
    }

    fun logout() {
        disconnect()
        mAuthProvider!!.logout()
        val intent = Intent(this@MapDriverActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun generateToken() {
        mTokenProvider!!.create(mAuthProvider!!.id)
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val SETTINGS_REQUEST_CODE = 2
    }
}