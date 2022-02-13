package com.heetch.technicaltest.features

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.heetch.technicaltest.R
import com.jakewharton.rxbinding3.view.clicks
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_drivers.*
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

class DriversListActivity : AppCompatActivity(), DriverListView {

    companion object {
        const val LOG_TAG = "DriversListActivity"
    }

    private val permissions =
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    private lateinit var driversListPresenter: DriversListPresenter
    private lateinit var driversAdapter: DriverListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)
        setSupportActionBar(drivers_toolbar)

        drivers_list.layoutManager = LinearLayoutManager(this)
        driversAdapter = DriverListAdapter()
        drivers_list.adapter = driversAdapter

        driversListPresenter = DriversListPresenter(this)
        driversListPresenter.checkLocationPermissions()
    }

    override fun onDestroy() {
        driversListPresenter.compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun checkPermissions(): Observable<Boolean> {
        return RxPermissions(this).request(*permissions)
    }

    @SuppressLint("MissingPermission")
    override fun getUserLocation(): Observable<Location> {
        return ReactiveLocationProvider(this).lastKnownLocation
    }

    override fun playClick(): Observable<Unit> {
        return drivers_fab.clicks()
    }

    override fun setDrivers(drivers: List<DriverUIModel>) {
        driversAdapter.submitList(drivers)
    }

    override fun showPermissionsDeniedDialog() {

    }

    override fun isGPSEnabled(): Boolean {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }

    override fun showNoGPSDialog() {
        Toast.makeText(this, getString(R.string.drivers_list_no_gps_message), Toast.LENGTH_SHORT)
            .show()
    }
}