package com.utez.newsflash.data

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos representativo de una noticia emitida por la API.
 */
data class NewsItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("isBreakingNews")
    val isBreakingNews: Boolean,
    @SerializedName("timestamp")
    val timestamp: String
)
