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
import java.util.concurrent.TimeUnit

class DriversListPresenter(
    private var driversListView: DriverListView,
    private val locationManager: LocationManager,
    private val networkManager: NetworkManager
) {
    val compositeDisposable = CompositeDisposable()
    private val rxPicasso = RxPicasso()
    var isPlaying: Boolean = false
    var playButtonState = BehaviorSubject.create<Boolean>().apply { onNext(isPlaying) }
    val driversList = BehaviorSubject.create<List<DriverUIModel>>()

    companion object {
        const val DRIVERS_REFRESH_INTERVAL = 5L

        var instance: DriversListPresenter? = null
        fun newInstance(
            driversListView: DriverListView,
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
                startLocationLoading()
            } else {
                driversListView.showPlaySwitch()
                compositeDisposable.clear()
            }
        }
    }

    private fun startLocationLoading() {
        var currentLocation: Location? = null
        Observable.interval(0, DRIVERS_REFRESH_INTERVAL, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { driversListView.checkPermissions() }
            .doOnNext { checkGPSErrors(it) }
            .filter { it }
            .flatMap { driversListView.getUserLocation() }
            .doOnNext { currentLocation = it }
            .observeOn(Schedulers.io())
            .flatMapSingle {
                networkManager.getRepository()
                    .loadDrivers(CoordinatesBody(it.latitude, it.longitude))
            }
            .flatMap { drivers -> createDriversUIModels(drivers, currentLocation!!) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ uiDrivers ->
                uiDrivers.sortBy { it.distance }
                driversList.onNext(uiDrivers)
            }, {
                driversListView.showNetworkErrorDialog(it.localizedMessage)
            }).addToDisposable(compositeDisposable)
    }

    private fun createDriversUIModels(drivers: List<DriverRemoteModel>, currentLocation: Location): Observable<MutableList<DriverUIModel>> {
        return Observable.fromIterable(drivers)
            .flatMap { driver ->
                rxPicasso.loadImage(NetworkManager.BASE_SHORTEN_URL + driver.image)
                    .map {
                        DriverUIModel(driver.id, it, driver.firstname, driver.lastname,
                            locationManager.getDistance(
                                currentLocation!!,
                                Location("").apply {
                                    latitude = driver.coordinates.latitude
                                    longitude = driver.coordinates.longitude
                                }
                            ).let { distance ->
                                String.format("%.1f", distance / 1000)
                            })
                    }
            }
            .toList()
            .toObservable()
    }

    private fun checkGPSErrors(isPermissionGranted: Boolean) {
        if (!driversListView.isGPSEnabled()) {
            driversListView.showNoGPSDialog()
        }

        if (!isPermissionGranted) {
            playButtonState.onNext(false)
            driversListView.showPermissionsDeniedDialog()
        }
    }
}