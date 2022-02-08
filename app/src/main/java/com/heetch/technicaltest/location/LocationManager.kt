package com.heetch.technicaltest.location

import android.content.Context
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.heetch.technicaltest.util.RxPicasso
import io.reactivex.Observable

class LocationManager(private val context: Context) : MapSnapshotRetriever, AddressRetriever {

    companion object {
        const val API_KEY = "AIzaSyDBIKDhvR3uHyAioXcH1IR_8t9MsIa-9Og"
        const val DEFAULT_ZOOM = 18
        const val ICON_URL =
            "https%3A%2F%2Fs3-eu-west-1.amazonaws.com%2Fheetch-production%2Fassets%2Fproducts%2Fcar-image-lepro.png"
    }

    override fun retrieveSnapshot(latitude: Double, longitude: Double): Observable<Bitmap> {
        val url = generateSnapshotUrl(latitude, longitude)
        return RxPicasso().loadImage(url)
    }

    override fun generateSnapshotUrl(latitude: Double, longitude: Double): String {
        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=$latitude,$longitude" +
                "&zoom=$DEFAULT_ZOOM" +
                "&size=600x400" +
                "&markers=anchor:center%7Cicon:$ICON_URL%7C$latitude,$longitude" +
                "&key=$API_KEY"
    }

    override fun geocode(latitude: Double, longitude: Double): Observable<Address> {
        return Observable.fromCallable {
            Geocoder(context).getFromLocation(latitude, longitude, 1)
        }.flatMap {
            if (it.isNotEmpty()) {
                Observable.just(it[0])
            } else {
                Observable.empty()
            }
        }
    }

    fun getDistance(from: Location, to: Location) : Float {
        return from.distanceTo(to)
    }

}