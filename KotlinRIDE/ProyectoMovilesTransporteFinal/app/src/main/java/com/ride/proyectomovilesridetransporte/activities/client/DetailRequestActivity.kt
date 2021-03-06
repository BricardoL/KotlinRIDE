package com.ride.proyectomovilesridetransporte.activities.client

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ride.proyectomovilesridetransporte.R
import com.ride.proyectomovilesridetransporte.models.Info
import com.ride.proyectomovilesridetransporte.providers.GoogleApiProvider
import com.ride.proyectomovilesridetransporte.providers.InfoProvider
import com.ride.proyectomovilesridetransporte.utils.DecodePoints
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailRequestActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var mMapFragment: SupportMapFragment? = null
    private var mExtraOriginLat = 0.0
    private var mExtraOriginLng = 0.0
    private var mExtraDestinationLat = 0.0
    private var mExtraDestinationLng = 0.0
    private var mExtraOrigin: String? = null
    private var mExtraDestination: String? = null
    private lateinit var mCircleImageBack: CircleImageView
    private lateinit var mOriginLatLng: LatLng;
    private  lateinit var mDestinationLatLng: LatLng
    private var mGoogleApiProvider: GoogleApiProvider? = null
    private var mInfoProvider: InfoProvider? = null
    private var mPolylineList: List<LatLng?>? = null
    private var mPolylineOptions: PolylineOptions? = null
    private lateinit var mTextViewOrigin: TextView
    private lateinit var mTextViewDestination: TextView
    private var mTextViewTime: TextView? = null
    private var mtextViewPrice: TextView? = null
    private lateinit var mButtonRequest: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_request)
        //MyToolbar.show(this, "TUS DATOS", true);
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mMapFragment!!.getMapAsync(this)
        mExtraOriginLat = intent.getDoubleExtra("origin_lat", 0.0)
        mExtraOriginLng = intent.getDoubleExtra("origin_lng", 0.0)
        mExtraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        mExtraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)
        mExtraOrigin = intent.getStringExtra("origin")
        mExtraDestination = intent.getStringExtra("destination")
        mCircleImageBack = findViewById(R.id.circleImageBack)
        mOriginLatLng = LatLng(mExtraOriginLat, mExtraOriginLng)
        mDestinationLatLng = LatLng(mExtraDestinationLat, mExtraDestinationLng)
        mGoogleApiProvider = GoogleApiProvider(this@DetailRequestActivity)
        mInfoProvider = InfoProvider()
        mTextViewOrigin = findViewById(R.id.textViewOrigin)
        mTextViewDestination = findViewById(R.id.textViewDestination)
        mTextViewTime = findViewById(R.id.textViewTime)
        mtextViewPrice = findViewById(R.id.textViewPrice)
        mButtonRequest = findViewById(R.id.btnRequestNow)
        mTextViewOrigin.setText(mExtraOrigin)
        mTextViewDestination.setText(mExtraDestination)
        mButtonRequest.setOnClickListener(View.OnClickListener { goToRequestDriver() })
        mCircleImageBack.setOnClickListener(View.OnClickListener { finish() })
    }

    private fun goToRequestDriver() {
        val intent = Intent(this@DetailRequestActivity, RequestDriverActivity::class.java)
        intent.putExtra("origin_lat", mOriginLatLng!!.latitude)
        intent.putExtra("origin_lng", mOriginLatLng!!.longitude)
        intent.putExtra("origin", mExtraOrigin)
        intent.putExtra("destination", mExtraDestination)
        intent.putExtra("destination_lat", mDestinationLatLng!!.latitude)
        intent.putExtra("destination_lng", mDestinationLatLng!!.longitude)
        startActivity(intent)
        finish()
    }

    private fun drawRoute() {
        mGoogleApiProvider!!.getDirections(mOriginLatLng, mDestinationLatLng)?.enqueue(object : Callback<String?> {
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
                    mTextViewTime!!.text = "$distanceText en $durationText"
                    val distanceAndKm = distanceText.split(" ").toTypedArray()
                    val distanceValue = distanceAndKm[0].toDouble()
                    val durationAndMins = distanceText.split(" ").toTypedArray()
                    val durationValue = durationAndMins[0].toDouble()
                    calculePrice(distanceValue, durationValue)
                } catch (e: Exception) {
                    Log.d("Error", "Error encontrado " + e.message)
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {}
        })
    }

    private fun calculePrice(distanceValue: Double, durationValue: Double) {
        mInfoProvider!!.info.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val info = dataSnapshot.getValue(Info::class.java)
                    val Totaldistance = distanceValue * info!!.km
                    val TotalDuration = durationValue * info.min
                    val Total = Totaldistance + TotalDuration
                    val minTotal = Total - 0.5
                    val mmaxTotal = Total + 0.5
                    mtextViewPrice!!.text = "$minTotal - $mmaxTotal$"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.addMarker(MarkerOptions().position(mOriginLatLng!!).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_red)))
        mMap!!.addMarker(MarkerOptions().position(mDestinationLatLng!!).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_blue)))
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(15f)
                        .build()
        ))
        drawRoute()
    }
}