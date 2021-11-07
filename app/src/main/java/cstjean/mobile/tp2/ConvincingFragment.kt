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
            .setTitle("Explication de la permission")
            .setMessage("Votre localisation est utilisée pour poursuivre votre progression dans le" +
                    " rally, sans votre accord, le rally ne peut vous suivre et l'accès au rally est impossible.")
            .setPositiveButton("Ok") { _,_ ->
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))

                if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED) startActivity(Intent(requireActivity(), RallyActivity::class.java))
            }
            .setNegativeButton("Nope") {_,_ ->}
            .show()

        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    startActivity(Intent(requireContext(), RallyActivity::class.java))
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    startActivity(Intent(requireContext(), RallyActivity::class.java))
                }
                else -> {
                }
            }
        }

    companion object {
        const val TAG = "ConvinceRunnerDialog"
    }
}