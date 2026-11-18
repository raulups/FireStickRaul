package com.raularribas.firelauncher.data.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverResponse(
    val results: List<DiscoverItem> = emptyList()
)

@Serializable
data class DiscoverItem(
    val title: String? = null,
    val name: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val popularity: Double = 0.0
)
