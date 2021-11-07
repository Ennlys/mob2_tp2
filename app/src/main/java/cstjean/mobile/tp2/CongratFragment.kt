package cstjean.mobile.tp2

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment

/**
 * @author Joseph Duquet
 * @author Ennlys Granger-Corbeil
 */
class CongratFragment: DialogFragment() {

    /**
     * permet de crée une nouvelle instance d'un dialogFragment
     *
     * @param time le temps mesuré du rally
     * @param steps les pas mesuré du rally
     */
    fun newInstance(time: String, steps: Int): CongratFragment{
        val f = CongratFragment()
        val args = Bundle()
        args.putString(CongratFragment.time, time)
        args.putInt(CongratFragment.steps, steps)
        f.arguments = args
        return f
    }

    /**
     * Permet d'afficher à l'utilisateur un dialog de félicitation.
     *
     * @param savedInstanceState l'intance sauvegardé par le dialog
     * @return [Dialog] La boîte de dialogue à afficher
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = requireArguments().getString(time)
        val steps = requireArguments().getInt(steps)

        val dialog = AlertDialog.Builder(requireActivity())
            .setTitle("Félicitation!")
            .setMessage(getString(R.string.congrats, time, steps))
            .setPositiveButton(R.string.another_rally) { _, _ ->
                startActivity(Intent(requireContext(), AccueilActivity::class.java))
            }
            .show()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    /**
     * Les tags servant à stocker les données du rally afin de les passer en arguments au dialogFragment
     * @property TAG Identifiant de la classe
     * @property steps Tag pour retrouver le nombre de pas
     * @property time Tag pour retrouver le temps
     */
    companion object {
        const val TAG = "CongratRunnerDialog"
        const val steps = "StepCounter"
        const val time = "TimeCounter"
    }
}