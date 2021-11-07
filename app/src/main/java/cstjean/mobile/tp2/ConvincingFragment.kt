package cstjean.mobile.tp2

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class ConvincingFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(requireActivity())
            .setMessage("Votre localisation est utilisée pour poursuivre votre progression dans le" +
                    " rally, sans votre accord, le rally ne peut vous suivre et l'accès au rally est impossible.")
            .setPositiveButton("2e chance") { _, _ ->
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton("Nope") {_,_ ->
            }
            .show()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    /**La boîte de dialogue qui demande la permission et enregistre la réponse de l'utilisateur*/
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    startActivity(Intent(requireContext(), AccueilActivity::class.java))
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    startActivity(Intent(requireContext(), AccueilActivity::class.java))
                }
            }
        }

    companion object {
        const val TAG = "ConvinceRunnerDialog"
    }
}