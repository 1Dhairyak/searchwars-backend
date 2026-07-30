package com.searchwars.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Points at the existing SearchWars Spring Boot backend.
 * Update BASE_URL to the current Elastic Beanstalk / Railway endpoint.
 */
object ApiClient {

    private const val BASE_URL = "https://searchwars-backend.example.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: SearchWarsService = retrofit.create(SearchWarsService::class.java)
}
