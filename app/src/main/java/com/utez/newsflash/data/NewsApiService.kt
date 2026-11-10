package com.utez.newsflash.data

import retrofit2.Response
import retrofit2.http.GET

/**
 * Interfaz de Retrofit para consultar el servidor de noticias.
 */
interface NewsApiService {
    // Endpoint para obtener la última noticia disponible
    @GET("latest_news")
    suspend fun getLatestNews(): Response<NewsItem>
}
