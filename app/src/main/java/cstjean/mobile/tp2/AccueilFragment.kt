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
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit

private const val REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES_KEY"

/**
 *
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
class AccueilFragment : Fragment() {

    private lateinit var btnDemarrer: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var requestingLocationUpdates = false

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("track", "isGranted - FINE")
                    requestingLocationUpdates = true
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("track", "isGranted - COARSE")
                    requestingLocationUpdates = true
                }
                else -> {
                    // TODO
                    // Expliquer à l'usager que la fonctionnalité n'est pas disponible car elle
                    // nécessite une permission qui a été refusée.
                    Log.d("track", "notGranted")
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_accueil, container, false)

        if (savedInstanceState != null) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY, false)
        }

        btnDemarrer = view.findViewById(R.id.btn_demmarer)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())

        btnDemarrer.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this.requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this.requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ->
                {
                    if (!requestingLocationUpdates) {
                        requestingLocationUpdates = true
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.accueil, RallyFragment(), "fragmentRally")
                            .addToBackStack(null)
                            .commit()
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    // TODO expliquer pourquoi necessaire
                }
                else -> {
                    requestPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            }
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }
}