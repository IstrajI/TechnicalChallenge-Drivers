package com.heetch.technicaltest.util

import android.graphics.Bitmap
import com.squareup.picasso.Picasso
import io.reactivex.Observable

class RxPicasso : ImageDownloader {

    override fun loadImage(url: String): Observable<Bitmap> {
        return Observable.fromCallable { Picasso.get().load(url).get() }
    }
}