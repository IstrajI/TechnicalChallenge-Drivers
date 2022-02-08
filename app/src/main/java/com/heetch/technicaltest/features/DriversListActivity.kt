package com.heetch.technicaltest.features

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heetch.technicaltest.R
import com.heetch.technicaltest.location.LocationManager
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_drivers.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

class DriversListActivity : AppCompatActivity() {

    companion object {
        const val LOG_TAG = "DriversListActivity"
    }

    private val permissions =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val compositeDisposable = CompositeDisposable()
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)
        setSupportActionBar(drivers_toolbar)

        locationManager = LocationManager(this)
        compositeDisposable.add(subscribeToFabClick())
    }

    private fun subscribeToFabClick(): Disposable {
        return drivers_fab.clicks()
            .doOnNext { Toast.makeText(this, "Play!", Toast.LENGTH_SHORT).show() }
            .flatMap {
                checkPermissions()
                    .flatMap { getUserLocation() }
                    .doOnNext {
                        Log.e(LOG_TAG, "Location : $it")
                    }
            }
            .subscribe()
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    private fun checkPermissions(): Observable<Boolean> {
        return RxPermissions(this).request(*permissions)
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation(): Observable<Location> {
        return ReactiveLocationProvider(this).lastKnownLocation
    }

}