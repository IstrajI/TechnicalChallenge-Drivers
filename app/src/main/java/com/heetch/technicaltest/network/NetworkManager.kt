package com.heetch.technicaltest.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.heetch.technicaltest.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkManager {

    companion object {
        var instance: NetworkManager? = null
        const val BASE_URL = "http://hiring.heetch.com/mobile/"
        const val BASE_SHORTEN_URL = "http://hiring.heetch.com"

        fun newInstance(): NetworkManager {
            return instance?:NetworkManager().also {
                instance = it
            }
        }
    }

    fun getRepository() : ApiInterface {
        val loggingInterceptor = provideLoggingInterceptor()
        val httpClient = provideLoggingCapableHttpClient(loggingInterceptor)
        val gson = provideGsonConverter()
        val rxJavaCallAdapterFactory = provideRxJavaCallAdapterFactory()
        val retrofit = provideRetrofitBuilder(httpClient, gson, rxJavaCallAdapterFactory)
        return provideRestService(retrofit, BASE_URL)
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Log.d("Retrofit logging", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }


    private fun provideLoggingCapableHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        gson: Gson,
        rxJavaCallAdapterFactory: RxJava2CallAdapterFactory
    ): Retrofit.Builder {

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(rxJavaCallAdapterFactory)
            .client(okHttpClient)
    }

    private fun provideRestService(retrofitBuilder: Retrofit.Builder, baseUrl: String): ApiInterface {
        return retrofitBuilder.baseUrl(baseUrl)
            .build()
            .create(ApiInterface::class.java)
    }

    private fun provideGsonConverter(): Gson {
        return GsonBuilder().create()
    }

    private fun provideRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

}