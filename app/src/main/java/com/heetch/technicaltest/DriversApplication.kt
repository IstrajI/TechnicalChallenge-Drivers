package com.heetch.technicaltest

import android.app.Application
import com.heetch.technicaltest.location.LocationManager
import com.heetch.technicaltest.network.NetworkManager

class DriversApplication: Application() {
    val networkManager = NetworkManager()
    val locationManager = LocationManager(this)
}