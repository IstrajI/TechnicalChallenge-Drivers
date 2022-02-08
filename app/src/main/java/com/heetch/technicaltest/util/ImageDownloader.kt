package com.heetch.technicaltest.util

import android.graphics.Bitmap
import io.reactivex.Observable

interface ImageDownloader {

    fun loadImage(url:String) : Observable<Bitmap>
}