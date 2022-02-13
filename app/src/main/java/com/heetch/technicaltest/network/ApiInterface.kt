package com.heetch.technicaltest.network

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.PUT

interface ApiInterface {

    @PUT("coordinates")
    fun loadDrivers(@Body coordinatesBody: CoordinatesBody): Single<List<DriverRemoteModel>>
}