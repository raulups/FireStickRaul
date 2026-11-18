package com.raularribas.firelauncher.data.tmdb

import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String,
        @Query("with_watch_providers") providerId: Int,
        @Query("language") language: String,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): DiscoverResponse

    @GET("discover/movie")
    suspend fun discoverMovie(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String,
        @Query("with_watch_providers") providerId: Int,
        @Query("language") language: String,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): DiscoverResponse
}
