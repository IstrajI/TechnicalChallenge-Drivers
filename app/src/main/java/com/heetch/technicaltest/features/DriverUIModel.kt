package com.heetch.technicaltest.features

import android.graphics.Bitmap

data class DriverUIModel (
    val id: Int,
    val avatar: Bitmap,
    val name: String,
    val surname: String,
    val distance: String
)