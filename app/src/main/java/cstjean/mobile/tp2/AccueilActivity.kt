package cstjean.mobile.tp2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class AccueilActivity : AppCompatActivity() {

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_accueil)

        btnDemarrer = findViewById(R.id.btn_demmarer)

        btnDemarrer.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED ->
                {
                    val intent = Intent(this, RallyActivity::class.java)
                    startActivity(intent)
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
    }

    companion object {
        const val FRAGMENT_RALLYFRAGMENT_OPEN = "fragment_rallyfragment_open"
        const val FRAGMENT_RALLYFRAGMENT_CLOSED = "fragment_rallyfragment_closed"
    }
}