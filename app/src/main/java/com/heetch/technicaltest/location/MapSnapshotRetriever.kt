package com.heetch.technicaltest.location

import android.graphics.Bitmap
import io.reactivex.Observable

interface MapSnapshotRetriever {

    fun retrieveSnapshot(latitude: Double, longitude: Double) : Observable<Bitmap>

    fun generateSnapshotUrl(latitude: Double, longitude: Double): String

}