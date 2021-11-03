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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.location.*
import java.util.concurrent.TimeUnit
import androidx.fragment.app.setFragmentResult

/**
 *
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
class AccueilFragment : Fragment() {

    private lateinit var btnDemarrer: Button

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("track", "isGranted - FINE")
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("track", "isGranted - COARSE")
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

        btnDemarrer = view.findViewById(R.id.btn_demmarer)

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
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, RallyFragment())
                        .commit()
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

    companion object {
        const val FRAGMENT_RALLYFRAGMENT_OPEN = "fragment_rallyfragment_open"
        const val FRAGMENT_RALLYFRAGMENT_CLOSED = "fragment_rallyfragment_closed"
    }
}