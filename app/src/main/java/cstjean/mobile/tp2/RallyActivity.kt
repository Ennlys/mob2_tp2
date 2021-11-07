package cstjean.mobile.tp2

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import java.lang.Integer.parseInt
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt




/**
 *
 * @property fusedLocationClient permet la comunication avec l'api du location provider
 * @property locationRequest un objet qui contient des qualités de service pour le fusedLocationClient
 * @property locationCallback recoit les notification si l'appareil ne peut être trouver
 * @property currentLocation la position actuelle
 * @property requestingLocationUpdates booléen si l'utilisateur (avait) accepte la localisation
 * @property googleMap garde en mémoire la carte de google
 * @property time le temps accumulé depuis le départ du rally
 * @property tvTimer textview pour le timer
 * @property tvStepcounter le textview du Step counter
 * @property activeThread booléene qui active ou désactive le thread dépendant de l'application
 * @property showUserLocation permet d'afficher à l'utilisateur son marqueur
 * @property listCoordonees la liste des coordonées du rally
 *
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
class RallyActivity : AppCompatActivity(), SensorEventListener, OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private var requestingLocationUpdates = false
    private lateinit var googleMap: GoogleMap
    private var time = 0L
    private lateinit var tvTimer: TextView
    private lateinit var tvStepcounter: TextView


    private var activeThread: AtomicBoolean = AtomicBoolean(true)
    private var showUserLocation = true

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    private var stepCmptMax = 0

    private val listCoordonees = arrayOf(
        Coordonees(45.3031,-73.2658),
        Coordonees(45.3013,-73.2577),
        Coordonees(45.2944,-73.2577),
        Coordonees(45.2956,-73.2670))

    /**
     * Permet d'afficher les données correspodant à la vue.
     * Elle permet aussi d'activer les threads pour le timer
     * et de configurer la mise à jour de la localisation.
     *
     * @param savedInstanceState l'instance sauvegarder par l'activité.
     */
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_rally)

        tvStepcounter = findViewById(R.id.steps)
        tvTimer = findViewById(R.id.timer)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.GM) as SupportMapFragment
        mapFragment.getMapAsync(this)

         sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
         sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        /*
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                if (event != null) {
                    tvStepcounter.text = getString(R.string.tv_stepCounter, event.values[0])
                }
            }
        }

        sensor?.also {
            sensorManager.requestTriggerSensor(triggerEventListener, it)
        }
        */
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (showUserLocation){
                    googleMap.moveCamera(CameraUpdateFactory.
                    newLatLng(fromLocationToLatLng(locationResult.lastLocation)))
                    showUserLocation = false
                }
                showLocation(locationResult.lastLocation)
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = java.util.concurrent.TimeUnit.SECONDS.toMillis(5)
            fastestInterval = java.util.concurrent.TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        Thread {
            while (activeThread.get()) {
                runOnUiThread { tvTimer.text = getFormattedStopWatch(time) }
                runOnUiThread { time++ }
                Thread.sleep(1000)
            }
        }.start()
    }
    /**
     * permet d'obtenir la localisation est le convertir

     * @param location la localisation d'un emplacement
     * @return [LatLng] latitude et longitude d'une localisation
     */
    private fun fromLocationToLatLng(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }

    /**
     * Transforme la donnée du temps en une string hh:mm:ss
     *
     * @param time le temps à convertir
     * @return [String] retourne le temps convertir string
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

    /**
     * Vérifie si la localisation a été accepté auparavant, et démarre la localisation.
     */
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

    /**
     * arrêt la localisation s'il est activé
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * affiche les marqueurs sur la carte google, celle de l'utilisateur
     * et les objectifs.
     * Il vérifie si l'utilisateur a tous bien obtenu les objectif et si oui affiche un dialog
     *
     * @param location la position de l'utilisateur
     */
    private fun showLocation(location: Location?){
        Log.d("test", stepCmptMax.toString())
        if(location != null) currentLocation = location
        else Log.d("track", "No location provided")
        googleMap.clear()
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                .title("Toi")
        )

        for (position in listCoordonees) {
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

        if(listCoordonees.all { a -> a.visite }){
            activeThread.set(false)
            stopLocationUpdates()
            afficheDialogTermine()

            /*
            val intent = Intent()
            intent.putExtra(CongratFragment.steps, tvStepcounter.text)
            intent.putExtra(CongratFragment.time, tvTimer.text)

            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag(CongratFragment.TAG)

            if(prev != null) ft.remove(prev)
            ft.addToBackStack(null)

            val congratFragment = CongratFragment().newInstance(tvTimer.text.toString(), parseInt("${if (tvStepcounter.text.toString() == "") 0 else tvStepcounter.text.toString()}"))
            congratFragment.show(ft, CongratFragment.TAG)
            return*/

        }
        googleMap.setMinZoomPreference(14F)
    }

    /**
     * Permet d'afficher a l'utilisateur un dialog de félicitation.
     */
    private fun afficheDialogTermine() {
        val alertDialogSupprimer = AlertDialog.Builder(this)
        alertDialogSupprimer.setTitle("Fecilitations!")
        alertDialogSupprimer.setMessage(getString(
            R.string.congrats, tvTimer.text, parseInt("" +
                    "${if (tvStepcounter.text.toString() == "") 0 
                    else tvStepcounter.text.toString()}")))

        alertDialogSupprimer.setPositiveButton(R.string.btn_retourMenu) { _, _ ->
            finish()
        }
        alertDialogSupprimer.setCancelable(false)
        alertDialogSupprimer.show()
    }

    /**
     *  Source: https://stackoverflow.com/questions/18883601/function-to-calculate-distance-between-two-coordinates
     *  Avec quelques modifications pour nos besoins
     *
     *  @param userLocation l'emplacement actuel de l'utilisateur
     *  @param objectiveLocation l'emplacement de l'objectif
     *  @return la distance entre l'utilisateur et l'emplacement.
     */
    private fun getDistanceFromLatLonInM(userLocation: Location, objectiveLocation: Coordonees): Double {
        val earthRadius = 6371 // Radius of the earth in km
        val dLat = degToRad(objectiveLocation.latitude - userLocation.latitude)  // degToRad below
        val dLon = degToRad(objectiveLocation.longitude - userLocation.longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(degToRad(userLocation.latitude)) * cos(degToRad(objectiveLocation.latitude)) *
                    sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c * 1000 // Distance in m
    }

    /**
     * methode qui permet de changer le degrée en radians
     *
     * @param deg le degrée
     * @return un radian
     */
    private fun degToRad(deg: Double): Double {
        return deg * (Math.PI/180)
    }

    /**
     * Vérifie si l'option d'utiliser le GPS a été accepté,
     * si oui, démarre la methode de localisation.
     */
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

    /**
     * permet d'arrêter la localisation quand l'application se met en pause
     * et arrête le timer
     */
    override fun onPause() {
        super.onPause()
        activeThread.set(false)
        stopLocationUpdates()
        sensorManager.unregisterListener(this)
    }

    /**
     * recommence la localisation si l'utilisateur avait déja accepté la localisation auparavant
     * et recommence le timer
     */
    override fun onResume() {
        super.onResume()
        if(requestingLocationUpdates) startLocationUpdates()
        activeThread.set(true)
        sensor?.also { proximity ->
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * À l'arrêt de l'activité, arrête la localisation.
     */
    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
    }

    /**
     * S'il a un changement avec la carte, enregistre les nouveux config dans la variable.
     * Force le type de carte de google au normal.
     * @param googleMap la carte de google
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    /**
     *
     */
    override fun onSensorChanged(event: SensorEvent) {
        if(stepCmptMax == 0) {
            stepCmptMax = event.values[0].toInt()
        }
       tvStepcounter.text = getString(R.string.tv_stepCounter,((event.values[0].toInt() - stepCmptMax)))
    }

    /**
     *
     */
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // TODO rien en ce moment
    }
}