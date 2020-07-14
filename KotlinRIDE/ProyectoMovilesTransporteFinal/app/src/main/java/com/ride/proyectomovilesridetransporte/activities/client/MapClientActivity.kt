package com.ride.proyectomovilesridetransporte.activities.client

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.database.DatabaseError
import com.google.maps.android.SphericalUtil
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.activities.MainActivity
import com.ride.proyectomovilesridetransporte.activities.client.MapClientActivity
import com.ride.proyectomovilesridetransporte.includes.MyToolbar
import com.ride.proyectomovilesridetransporte.providers.AuthProvider
import com.ride.proyectomovilesridetransporte.providers.GeofireProvider
import com.ride.proyectomovilesridetransporte.providers.TokenProvider
import java.util.*

class MapClientActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mAuthProvider: AuthProvider? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocation: FusedLocationProviderClient? = null
    private var mGeofireProvider: GeofireProvider? = null
    private var mTokenProvider: TokenProvider? = null
    private val mMarker: Marker? = null
    private lateinit var mCurrentLatLng: LatLng;
    private val mDriversMarkers: MutableList<Marker> = ArrayList()
    private var mIsFirstTime = true
    private var mPlaces: PlacesClient? = null
    private var mAutocomplete: AutocompleteSupportFragment? = null
    private var mAutocompleteDestination: AutocompleteSupportFragment? = null
    private var mOrigin: String? = null
    private var mOriginLatLng: LatLng? = null
    private var mDestination: String? = null
    private var mDestinationLatLng: LatLng? = null
    private var mCameraListener: OnCameraIdleListener? = null
    private lateinit var mButtonRequestDriver: Button
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (applicationContext != null) {
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)

                    // OBTENER LA LOCALIZACION DEL USUARIO EN TIEMPO REAL
                    mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(15f)
                                    .build()
                    ))
                    if (mIsFirstTime) {
                        mIsFirstTime = false
                        activeDrivers
                        limitSearch()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_client)
        MyToolbar.show(this, "Cliente", false)
        mAuthProvider = AuthProvider()
        mGeofireProvider = GeofireProvider("active_drivers")
        mTokenProvider = TokenProvider()
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        mButtonRequestDriver = findViewById(R.id.btnRequestDriver)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        }
        mPlaces = Places.createClient(this)
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()
        onCameraMove()
        mButtonRequestDriver.setOnClickListener(View.OnClickListener { requestDriver() })
        generateToken()
    }

    private fun requestDriver() {
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            val intent = Intent(this@MapClientActivity, DetailRequestActivity::class.java)
            intent.putExtra("origin_lat", mOriginLatLng!!.latitude)
            intent.putExtra("origin_lng", mOriginLatLng!!.longitude)
            intent.putExtra("destination_lat", mDestinationLatLng!!.latitude)
            intent.putExtra("destination_lng", mDestinationLatLng!!.longitude)
            intent.putExtra("origin", mOrigin)
            intent.putExtra("destination", mDestination)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Debe seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limitSearch() {
        val northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000.0, 180.0)
        mAutocomplete!!.setCountry("SV")
        mAutocomplete!!.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        mAutocompleteDestination!!.setCountry("SV")
        mAutocompleteDestination!!.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
    }

    private fun onCameraMove() {
        mCameraListener = OnCameraIdleListener {
            try {
                val geocoder = Geocoder(this@MapClientActivity)
                mOriginLatLng = mMap!!.cameraPosition.target
                val addressList = geocoder.getFromLocation(mOriginLatLng!!.latitude, mOriginLatLng!!.longitude, 1)
                val city = addressList[0].locality
                val country = addressList[0].countryName
                val address = addressList[0].getAddressLine(0)
                mOrigin = "$address $city"
                mAutocomplete!!.setText("$address $city")
            } catch (e: Exception) {
                Log.d("Error: ", "Mensaje error: " + e.message)
            }
        }
    }

    private fun instanceAutocompleteOrigin() {
        mAutocomplete = supportFragmentManager.findFragmentById(R.id.placeAutocompleteOrigin) as AutocompleteSupportFragment?
        mAutocomplete!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME))
        mAutocomplete!!.setHint("Lugar de recogida")
        mAutocomplete!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                mOrigin = place.name
                mOriginLatLng = place.latLng
                Log.d("PLACE", "Name: $mOrigin")
                Log.d("PLACE", "Lat: " + mOriginLatLng!!.latitude)
                Log.d("PLACE", "Lng: " + mOriginLatLng!!.longitude)
            }

            override fun onError(status: Status) {}
        })
    }

    private fun instanceAutocompleteDestination() {
        mAutocompleteDestination = supportFragmentManager.findFragmentById(R.id.placeAutocompleteDestination) as AutocompleteSupportFragment?
        mAutocompleteDestination!!.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME))
        mAutocompleteDestination!!.setHint("Destino")
        mAutocompleteDestination!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                mDestination = place.name
                mDestinationLatLng = place.latLng
                Log.d("PLACE", "Name: $mDestination")
                Log.d("PLACE", "Lat: " + mDestinationLatLng!!.latitude)
                Log.d("PLACE", "Lng: " + mDestinationLatLng!!.longitude)
            }

            override fun onError(status: Status) {}
        })
    }// ACTUALIZAR LA POSICION DE CADA CONDUCTOR

    // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION
    private val activeDrivers: Unit
        private get() {
            mGeofireProvider!!.getActiveDrivers(mCurrentLatLng, 10.0).addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    // AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE SE CONECTEN EN LA APLICACION
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == key) {
                                return
                            }
                        }
                    }
                    val driverLatLng = LatLng(location.latitude, location.longitude)
                    val marker = mMap!!.addMarker(MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_drive)))
                    marker.tag = key
                    mDriversMarkers.add(marker)
                }

                override fun onKeyExited(key: String) {
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == key) {
                                marker.remove()
                                mDriversMarkers.remove(marker)
                                return
                            }
                        }
                    }
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    // ACTUALIZAR LA POSICION DE CADA CONDUCTOR
                    for (marker in mDriversMarkers) {
                        if (marker.tag != null) {
                            if (marker.tag == key) {
                                marker.position = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }
                }

                override fun onGeoQueryReady() {}
                override fun onGeoQueryError(error: DatabaseError) {}
            })
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.isMyLocationEnabled = false
        mMap!!.setOnCameraIdleListener(mCameraListener)
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
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
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
                        .setPositiveButton("OK") { dialogInterface, i -> ActivityCompat.requestPermissions(this@MapClientActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE) }
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this@MapClientActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.client_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            logout()
        }
        if (item.itemId == R.id.action_update) {
            val intent = Intent(this@MapClientActivity, UpdateProfileActivity::class.java)
            startActivity(intent)
        }
        if (item.itemId == R.id.action_history) {
            val intent = Intent(this@MapClientActivity, HistoryBookingClientActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun logout() {
        mAuthProvider!!.logout()
        val intent = Intent(this@MapClientActivity, MainActivity::class.java)
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