package com.raularribas.firelauncher.data.tmdb

import android.content.Context
import com.raularribas.firelauncher.domain.model.FeaturedContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class FeaturedSnapshot(
    val providers: List<ProviderDto> = emptyList(),
    val providersAt: Long = 0,
    val featured: Map<String, CachedFeatured> = emptyMap()
)

@Serializable
data class CachedFeatured(
    val savedAt: Long,
    val items: List<CachedItem>
)

@Serializable
data class CachedItem(
    val providerName: String,
    val title: String,
    val kind: String,
    val year: String,
    val backdropUrl: String
) {
    fun toDomain() = FeaturedContent(providerName, title, kind, year, backdropUrl)
}

@Singleton
class FeaturedDiskCache @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) {
    private val file = File(context.filesDir, "featured_cache.json")

    suspend fun read(): FeaturedSnapshot = withContext(Dispatchers.IO) {
        runCatching { json.decodeFromString(FeaturedSnapshot.serializer(), file.readText()) }
            .getOrDefault(FeaturedSnapshot())
    }

    suspend fun write(snapshot: FeaturedSnapshot) {
        withContext(Dispatchers.IO) {
            runCatching {
                val tmp = File(file.parentFile, "${file.name}.tmp")
                tmp.writeText(json.encodeToString(FeaturedSnapshot.serializer(), snapshot))
                tmp.renameTo(file)
            }
        }
    }
}
