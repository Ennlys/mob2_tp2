package cstjean.mobile.tp2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * Activité qui permet l'autorisation d'utiliser la localisation
 *
 * @property btnDemarrer le bouton à cliquer
 * @property requestPermissionLauncher permet d'afficher la requête pour la localisation
 * et si oui, le renvoie vers une nouvelle activité
 *
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
class AccueilActivity : AppCompatActivity() {

    private lateinit var btnDemarrer: Button

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    startActivity(Intent(this, RallyActivity::class.java))
                    ActivityResultContracts.StartActivityForResult()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    startActivity(Intent(this, RallyActivity::class.java))
                }
                else -> {
                }
            }
        }


    /**
     *  Creation de la vue. Permet d'Afficher à l'utilisateur l'autorisation de la localisation
     *  @param savedInstanceState l'instance de sauvegarde
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_accueil)

        btnDemarrer = findViewById(R.id.btn_demmarer)

        btnDemarrer.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                    startActivity(Intent(this, RallyActivity::class.java))
                }
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    //ConvincingFragment().show(supportFragmentManager, ConvincingFragment.TAG)
                    afficheDialogLocalisation()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }
    }


    /**
     * Affiche a l'utilisateur un dialog qui explique a l'utilisateur pourquoi on utilisons
     * la localisation
     */
    private fun afficheDialogLocalisation() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Explication de la permission")
            .setMessage("Votre localisation est utilisée pour poursuivre votre progression dans le" +
                    " rally, sans votre accord, le rally ne peut vous suivre et l'accès au rally est impossible.")
            .setPositiveButton("Ok") { _,_ ->
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) startActivity(Intent(this, RallyActivity::class.java))
            }
            .setNegativeButton("fermer l'application") {_,_ ->
                finish()
            }
            .show()

        dialog.setCanceledOnTouchOutside(false)
    }
}