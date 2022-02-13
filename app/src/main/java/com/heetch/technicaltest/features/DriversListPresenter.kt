package com.heetch.technicaltest.features

import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.network.NetworkManager
import com.heetch.technicaltest.util.addToDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DriversListPresenter(private val driversListView: DriverListView) {
    var networkManager: NetworkManager = NetworkManager.newInstance()
    val compositeDisposable = CompositeDisposable()

    fun subscribeToFabClick() {
        driversListView.playClick()
            .flatMap {
                driversListView.checkPermissions()
            }.subscribe { isPermissionsGranted ->
                if (!driversListView.isGPSEnabled()) {
                    driversListView.showNoGPSDialog()
                    return@subscribe
                }
                if (isPermissionsGranted) {
                    loadUserLocation()
                } else {
                    driversListView.showPermissionsDeniedDialog()
                }
            }.addToDisposable(compositeDisposable)
    }

    private fun loadUserLocation() {
        driversListView.getUserLocation()
            .repeatWhen { observable -> observable.delay(5, TimeUnit.SECONDS) }
            .observeOn(Schedulers.io())
            .flatMap { location ->
                networkManager.getRepository()
                    .getCoordinates(CoordinatesBody(location.latitude, location.longitude))
                    .toObservable()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {

            }.addToDisposable(compositeDisposable)
    }
}