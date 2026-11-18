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
        @Query("air_date.gte") airDateFrom: String,
        @Query("page") page: Int,
        @Query("with_watch_monetization_types") monetization: String = "flatrate",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): DiscoverResponse

    @GET("discover/movie")
    suspend fun discoverMovie(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String,
        @Query("with_watch_providers") providerId: Int,
        @Query("language") language: String,
        @Query("primary_release_date.gte") releaseDateFrom: String,
        @Query("page") page: Int,
        @Query("with_watch_monetization_types") monetization: String = "flatrate",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): DiscoverResponse

    @GET("trending/tv/week")
    suspend fun trendingTv(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): DiscoverResponse

    @GET("trending/movie/week")
    suspend fun trendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): DiscoverResponse

    @GET("watch/providers/movie")
    suspend fun movieProviders(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String,
        @Query("language") language: String
    ): ProvidersResponse

    @GET("watch/providers/tv")
    suspend fun tvProviders(
        @Query("api_key") apiKey: String,
        @Query("watch_region") region: String,
        @Query("language") language: String
    ): ProvidersResponse
}
