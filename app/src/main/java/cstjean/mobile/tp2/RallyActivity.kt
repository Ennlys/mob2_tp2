package cstjean.mobile.tp2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class RallyActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private var requestingLocationUpdates = false
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private var stepCounter = 0

    private val mutableList = arrayOf(
        Coordonees(45.3031,-73.2658),
        Coordonees(45.3013,-73.2577),
        Coordonees(45.2944,-73.2577),
        Coordonees(45.2956,-73.2670))

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        mapView = findViewById(R.id.GM)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                stepCounter++
            }
        }
        sensor?.also {
            sensorManager.requestTriggerSensor(triggerEventListener, it)
        }

        val timer = Timer(this.currentFocus!!, R.id.timer)
        timer.startTimer()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                showLocation(locationResult.lastLocation)
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = java.util.concurrent.TimeUnit.SECONDS.toMillis(5)
            fastestInterval = java.util.concurrent.TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView.getMapAsync(OnMapReadyCallback {
            this.googleMap = it
            startLocationUpdates()
        })
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun showLocation(location: Location?){
        Log.d("Steps", stepCounter.toString())
        if(location != null) currentLocation = location
        else Log.d("track", "No location provided")
        googleMap.clear()
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Toi")
        )

        for (position in mutableList) {
            val latLng = LatLng(position.latitude, position.longitude)
            if (position.visite) {
                googleMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title("visité")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                )
            }
            else {

                googleMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title("non visite")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                googleMap.addCircle(
                    CircleOptions().center(latLng).radius(100.0)
                )
            }
        }

        if(marker != null) googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
        googleMap.setMinZoomPreference(14F)
    }


    private fun stopLocationUpdates() {
        Log.d("track", "STOP")
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ){
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
        locationCallback,
        Looper.getMainLooper())
    }

    override fun onStart() {
        super.onStart()
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> {
                if (!requestingLocationUpdates){
                    requestingLocationUpdates = true
                }
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
    }

}