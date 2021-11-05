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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class RallyActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private var requestingLocationUpdates = false
    private lateinit var googleMap: GoogleMap
    private var time = 0L
    private lateinit var tvTimer: TextView
    private var stepCounter = 0
    private var measureTime = true

    private val mutableList = arrayOf(
        Coordonees(45.3031,-73.2658),
        Coordonees(45.3013,-73.2577),
        Coordonees(45.2944,-73.2577),
        Coordonees(45.2956,-73.2670))

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_rally)

        tvTimer = findViewById(R.id.timer)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.GM) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

        val timer = Thread {
            while (measureTime) {
                runOnUiThread { tvTimer.text = getFormattedStopWatch(time) }
                runOnUiThread { time++ }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun resumeTimer(timer: Thread){
        if(timer.isAlive){
            measureTime = true
        }
    }

    private fun pauseTimer(timer: Thread){
        if(timer.isAlive){
            measureTime = false
        }
    }

    /**
     * Transforme la donnée du temps en une string hh:mm:ss
     */
    //Source: https://medium.com/swlh/how-to-create-a-stopwatch-in-android-117912264491
    private fun getFormattedStopWatch(time: Long): String {
        var milliseconds = time * 1000
        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= java.util.concurrent.TimeUnit.HOURS.toMillis(hours)
        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= java.util.concurrent.TimeUnit.MINUTES.toMillis(minutes)
        val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds"
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
            if(getDistanceFromLatLonInM(currentLocation, position) <= 100){
                position.visite = true
            }
        }

        if(marker != null) googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
        googleMap.setMinZoomPreference(16F)
    }

    //Source: https://stackoverflow.com/questions/18883601/function-to-calculate-distance-between-two-coordinates
    //Avec quelques modifications pour nos besoins
    private fun getDistanceFromLatLonInM(userLocation: Location, objectiveLocation: Coordonees): Double {
        val earthRadius = 6371; // Radius of the earth in km
        val dLat = degToRad(objectiveLocation.latitude - userLocation.latitude)  // deg2rad below
        val dLon = degToRad(objectiveLocation.longitude - userLocation.longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(degToRad(userLocation.latitude)) * cos(degToRad(objectiveLocation.latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c * 1000 // Distance in m
    }

    private fun degToRad(deg: Double): Double {
        return deg * (Math.PI/180)
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
                    startLocationUpdates()
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if(requestingLocationUpdates) startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

}