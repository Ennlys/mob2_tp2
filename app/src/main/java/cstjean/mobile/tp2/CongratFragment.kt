package cstjean.mobile.tp2

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class CongratFragment: DialogFragment() {

    fun newInstance(time: String, steps: Int): CongratFragment{
        val f = CongratFragment()
        val args = Bundle()
        args.putString(CongratFragment.time, time)
        args.putInt(CongratFragment.steps, steps)
        f.arguments = args
        return f
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val time = requireArguments().getString(time)
        val steps = requireArguments().getInt(steps)

        return AlertDialog.Builder(requireActivity())
            .setTitle("Félicitation!")
            .setMessage(getString(R.string.congrats, time, steps))
            .setPositiveButton(R.string.another_rally) { _, _ ->
                startActivity(Intent(requireContext(), RallyActivity::class.java))
            }
            .show()
    }

    companion object {
        const val TAG = "CongratRunnerDialog"
        const val steps = "StepCounter"
        const val time = "TimeCounter"
    }
}