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
     * permet de crée une nouvelle instance d'un dialog Fragment
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
     * Permet d'afficher a l'utilisateur un dialog de félicitation.
     *
     * @param savedInstanceState l'intance sauvegarder par le dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = requireArguments().getString(time)
        val steps = requireArguments().getInt(steps)

        val dialog = AlertDialog.Builder(requireActivity())
            .setTitle("Félicitation!")
            .setMessage(getString(R.string.congrats, time, steps))
            .setPositiveButton(R.string.another_rally) { _, _ ->
                //startActivity(Intent(requireContext(), RallyActivity::class.java))
            }
            .show()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    companion object {
        const val TAG = "CongratRunnerDialog"
        const val steps = "StepCounter"
        const val time = "TimeCounter"
    }
}