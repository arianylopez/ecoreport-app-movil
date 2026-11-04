package com.example.ecoreport.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://6ae3c189933120ae20f924dd.mockapi.io/api/v1/"

    val api:
            ReporteApiService by lazy {

        Retrofit.Builder()
            .baseUrl(
                BASE_URL
            )
            .addConverterFactory(
                GsonConverterFactory
                    .create()
            )
            .build()
            .create(
                ReporteApiService::class.java
            )
    }
}
