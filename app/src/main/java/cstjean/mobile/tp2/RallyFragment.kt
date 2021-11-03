package cstjean.mobile.tp2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import java.security.Permission

private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

class RallyFragment : Fragment(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private var requestingLocationUpdates = false
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView

    private val mutableList = arrayOf(
        Coordonees(45.3031,-73.2658),
        Coordonees(45.3013,-73.2577),
        Coordonees(45.2944,-73.2577),
        Coordonees(45.2956,-73.2670))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rally, container, false)
        mapView = view.findViewById(R.id.GM)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()


        val timer = Timer(view, R.id.timer)
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapView.getMapAsync(OnMapReadyCallback {
            this.googleMap = it
            startLocationUpdates()
        })

        return view
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
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
        if(location != null) currentLocation = location
        else Log.d("track", "No location provided")
        googleMap.clear()
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Toi")
        )

        for (position in mutableList) {
            if (position.visite) {
                // TODO
            }
            else {
                val latLng = LatLng(position.latitude, position.longitude)
                googleMap.addMarker(
                    MarkerOptions().position(latLng)
                        .title("non visite")
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
        if(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ), ContextCompat.checkSelfPermission(
                this.requireContext(),
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