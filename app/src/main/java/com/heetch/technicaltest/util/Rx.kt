package com.heetch.technicaltest.util

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addToDisposable(compositeDisposable: CompositeDisposable): Disposable {
    compositeDisposable.add(this)
    return this
}