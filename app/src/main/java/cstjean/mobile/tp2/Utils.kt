package cstjean.mobile.tp2

import android.location.Location

    fun Location?.toText():String {
        return if (this != null) {
            "($latitude, $longitude)"
        } else {
            "Unknown location"
        }
    }
