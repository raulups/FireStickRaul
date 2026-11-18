package com.raularribas.firelauncher.data.tmdb

import com.raularribas.firelauncher.BuildConfig

object TmdbConfig {
    val apiKey: String get() = BuildConfig.TMDB_API_KEY
    const val REGION = "ES"
    const val LANGUAGE = "es-ES"
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w1280"
}
