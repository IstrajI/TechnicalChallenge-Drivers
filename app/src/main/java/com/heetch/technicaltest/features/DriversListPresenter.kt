package com.heetch.technicaltest.features

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
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

class DriversListPresenter(
    private var driversListView: DriverListView,
    private val locationManager: LocationManager,
    private val networkManager: NetworkManager
) {
    val compositeDisposable = CompositeDisposable()
    private val rxPicasso = RxPicasso()
    private var lastUserLocation: Location? = null
    var isPlaying: Boolean = false
    var playButtonState = BehaviorSubject.create<Boolean>().apply { onNext(isPlaying) }
    val driversList = BehaviorSubject.create<List<DriverUIModel>>()

    companion object {
        const val DRIVERS_REFRESH_INTERVAL = 5L

        var instance: DriversListPresenter? = null
        fun newInstance(driversListView: DriverListView,
                        locationManager: LocationManager,
                        networkManager: NetworkManager
        ): DriversListPresenter {
            if (instance != null) {
                instance!!.driversListView = driversListView
            } else {
                instance = DriversListPresenter(driversListView, locationManager, networkManager)
            }
            return instance!!
        }
    }


    fun subscribeToPlayButtonState() {
        driversListView.playClick().subscribe {
            isPlaying = !isPlaying
            playButtonState.onNext(isPlaying)
        }

        playButtonState.subscribe {
            if (it) {
                driversListView.showPauseSwitch()
                loadCurrentLocation()
            } else {
                driversListView.showPlaySwitch()
                compositeDisposable.clear()
            }
        }.addToDisposable(compositeDisposable)
    }

    private fun loadCurrentLocation() {
        Observable.just("")
            .repeatWhen { observable ->
                observable.delay(
                    DRIVERS_REFRESH_INTERVAL,
                    TimeUnit.SECONDS
                )
            }
            .flatMap { driversListView.checkPermissions() }
            .subscribe ({ isPermissionsGranted ->
                if (!driversListView.isGPSEnabled()) {
                    driversListView.showNoGPSDialog()
                    return@subscribe
                }
                if (isPermissionsGranted) {
                    loadNearestDrivers()
                } else {
                    driversListView.showPermissionsDeniedDialog()
                }
            }, {

            }, {

            }).addToDisposable(compositeDisposable)
    }

    private fun loadNearestDrivers() {
        driversListView.getUserLocation()
            .observeOn(Schedulers.io())
            .flatMap { userLocation ->
                lastUserLocation = userLocation
                networkManager.getRepository()
                    .loadDrivers(CoordinatesBody(userLocation.latitude, userLocation.longitude))
                    .toObservable()

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe({ drivers ->
                loadDriversAvatar(drivers)
            }, {

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
            .subscribe ({ drivers ->
                drivers.sortBy { it.distance }
                driversList.onNext(drivers)
            }, {
        }).addToDisposable(compositeDisposable)
        }
}