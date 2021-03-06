package com.heetch.technicaltest.features

import android.location.Location
import com.heetch.technicaltest.network.DriverRemoteModel
import io.reactivex.Observable

interface DriverListView {
    fun playClick(): Observable<Unit>

    fun isGPSEnabled(): Boolean
    fun checkPermissions(): Observable<Boolean>
    fun getUserLocation(): Observable<Location>
    fun setDrivers(drivers: List<DriverUIModel>)

    fun showPermissionsDeniedDialog()
    fun showNetworkErrorDialog(message: String)
    fun showNoGPSDialog()

    fun showPlaySwitch()
    fun showPauseSwitch()
}