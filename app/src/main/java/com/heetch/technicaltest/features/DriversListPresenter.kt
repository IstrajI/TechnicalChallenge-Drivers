package com.heetch.technicaltest.features

import com.heetch.technicaltest.network.CoordinatesBody
import com.heetch.technicaltest.network.DriverRemoteModel
import com.heetch.technicaltest.network.NetworkManager
import com.heetch.technicaltest.util.RxPicasso
import com.heetch.technicaltest.util.addToDisposable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class DriversListPresenter(private val driversListView: DriverListView) {
    private var networkManager: NetworkManager = NetworkManager.newInstance()
    val compositeDisposable = CompositeDisposable()
    private val rxPicasso = RxPicasso()

    companion object {
        const val DRIVERS_REFRESH_INTERVAL = 5L
    }

    fun checkLocationPermissions() {
        driversListView.playClick()
            .flatMap {
                driversListView.checkPermissions()
            }.subscribe { isPermissionsGranted ->
                if (!driversListView.isGPSEnabled()) {
                    driversListView.showNoGPSDialog()
                    return@subscribe
                }
                if (isPermissionsGranted) {
                    loadNearestDrivers()
                } else {
                    driversListView.showPermissionsDeniedDialog()
                }
            }.addToDisposable(compositeDisposable)
    }

    private fun loadNearestDrivers() {
        driversListView.getUserLocation()
            .repeatWhen { observable ->
                observable.delay(
                    DRIVERS_REFRESH_INTERVAL,
                    TimeUnit.SECONDS
                )
            }
            .observeOn(Schedulers.io())
            .flatMap { userLocation ->
                networkManager.getRepository()
                    .loadDrivers(CoordinatesBody(userLocation.latitude, userLocation.longitude))
                    .toObservable()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ drivers ->
                loadDriversAvatar(drivers)
            }, {

            }).addToDisposable(compositeDisposable)
    }

    private fun loadDriversAvatar(drivers: List<DriverRemoteModel>) {
        Observable.fromIterable(drivers)
            .flatMap({ driver ->
                rxPicasso.loadImage(NetworkManager.BASE_SHORTEN_URL + driver.image)
            }, { driver, imageBitmap ->
                DriverUIModel(driver.id, imageBitmap, driver.firstname, driver.lastname, 4.0)
            })
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drivers ->
                driversListView.setDrivers(drivers)
            }.addToDisposable(compositeDisposable)
    }
}