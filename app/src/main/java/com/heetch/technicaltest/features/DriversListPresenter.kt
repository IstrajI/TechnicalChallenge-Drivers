package com.heetch.technicaltest.features

import android.annotation.SuppressLint
import android.location.Location
import com.heetch.technicaltest.location.LocationManager
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

class DriversListPresenter(
    private val driversListView: DriverListView,
    private val locationManager: LocationManager,
    private val networkManager: NetworkManager
) {
    val compositeDisposable = CompositeDisposable()
    private val rxPicasso = RxPicasso()
    private var lastUserLocation: Location? = null
    private var isPlaying: Boolean = false

    companion object {
        const val DRIVERS_REFRESH_INTERVAL = 5L
    }

    @SuppressLint("CheckResult")
    fun subscribeToDriversUpdates() {
        driversListView.playClick()
            .doOnNext {
                isPlaying = !isPlaying
                if (isPlaying) {
                    driversListView.showPauseSwitch()
                } else {
                    driversListView.showPlaySwitch()
                }
            }
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
            }
    }

    private fun loadNearestDrivers() {
        Observable.just("")
            .takeWhile { isPlaying }
            .repeatWhen { observable ->
                observable.delay(
                    DRIVERS_REFRESH_INTERVAL,
                    TimeUnit.SECONDS
                )
            }
            .flatMap {
                driversListView.getUserLocation()
            }
            .observeOn(Schedulers.io())
            .flatMap { userLocation ->
                lastUserLocation = userLocation
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
                val driverLocation = Location("").apply {
                    latitude = driver.coordinates.latitude
                    longitude = driver.coordinates.longitude
                }

                val distanceBetweenUser = lastUserLocation?.let {
                    locationManager.getDistance(
                        lastUserLocation!!,
                        driverLocation
                    )
                } ?: -1F

                val distanceBetweenUserInKm = String.format("%.1f", distanceBetweenUser / 1000)

                DriverUIModel(
                    driver.id,
                    imageBitmap,
                    driver.firstname,
                    driver.lastname,
                    distanceBetweenUserInKm
                )
            })
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { drivers ->
                drivers.sortBy { it.distance }
                driversListView.setDrivers(drivers)
            }.addToDisposable(compositeDisposable)
    }
}