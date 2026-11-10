package com.utez.newsflash.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Para pruebas reales se puede usar un MockAPI, Beeceptor o un JSON estático público en GitHub Pages.
    // Usamos una URL base simulada para el caso de estudio:
    private const val BASE_URL = "https://run.mocky.io/v3/"

    val instance: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}
