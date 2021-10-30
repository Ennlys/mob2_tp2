package cstjean.mobile.tp2

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView

/**
 * Permet de calculer le temps dans un thread à part
 * @param view Nécessaire pour mettre à jour le widget
 * @param widget Le id du widget (textView) qui affichera le temps
 */
class Timer(view: View, widget: Int) : Runnable {
    private var time = 0L
    private var handler: Handler? = null
    private var view: View? = null
    private var widget: Int = 0

    /**
     * Constructeur publique
     */
    init {
        this.view = view
        this.widget = widget
    }

    /**
     * Démarre le chronomètre
     */
    fun startTimer(){
        handler = Handler(Looper.getMainLooper())
        run()
    }

    /**
     * Force une donnée au chronomètre
     */
    fun setTimer(newTime: Long){
        this.time = newTime
    }

    /**
     * Arrête le chornomètre
     */
    fun stopTimer(){
        handler?.removeCallbacks(this)
    }

    /**
     * Transforme la donnée du temps en une string hh:mm:ss
     */
    //Source: https://medium.com/swlh/how-to-create-a-stopwatch-in-android-117912264491
    private fun getFormattedStopWatch(): String {
        var milliseconds = this.time * 1000
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
     * Incrémente le temps de 1 (sec) et met à jour le widget de la vue, données en paramètres au constructeur
     */
    override fun run() {
        time += 1
        view?.findViewById<TextView>(widget)?.text = getFormattedStopWatch()
        handler?.postDelayed(this, 1000L)
    }
}