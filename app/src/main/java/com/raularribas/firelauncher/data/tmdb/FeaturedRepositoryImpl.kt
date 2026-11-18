package com.raularribas.firelauncher.data.tmdb

import com.raularribas.firelauncher.BuildConfig
import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.domain.repository.FeaturedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturedRepositoryImpl @Inject constructor(
    private val api: TmdbApi
) : FeaturedRepository {

    private data class CacheEntry(val timestamp: Long, val items: List<FeaturedContent>)

    private val cache = ConcurrentHashMap<Int, CacheEntry>()

    override suspend fun featuredFor(app: LauncherApp): List<FeaturedContent> {
        if (BuildConfig.TMDB_API_KEY.isBlank()) return emptyList()
        val provider = StreamingProviders.forApp(app) ?: return emptyList()
        cache[provider.id]?.takeIf { System.currentTimeMillis() - it.timestamp < CACHE_TTL_MS }?.let { return it.items }
        val items = runCatching { fetch(provider) }.getOrDefault(emptyList())
        if (items.isNotEmpty()) cache[provider.id] = CacheEntry(System.currentTimeMillis(), items)
        return items
    }

    private suspend fun fetch(provider: StreamingProvider): List<FeaturedContent> = coroutineScope {
        val key = BuildConfig.TMDB_API_KEY
        val region = StreamingProviders.REGION
        val language = StreamingProviders.LANGUAGE
        val tv = async { api.discoverTv(key, region, provider.id, language).results.map { it to true } }
        val movies = async { api.discoverMovie(key, region, provider.id, language).results.map { it to false } }
        (tv.await() + movies.await())
            .filter { it.first.backdropPath != null }
            .sortedByDescending { it.first.popularity }
            .take(MAX_ITEMS)
            .map { (item, isTv) ->
                FeaturedContent(
                    providerName = provider.name,
                    title = item.name ?: item.title.orEmpty(),
                    kind = if (isTv) "Serie" else "Película",
                    year = (item.firstAirDate ?: item.releaseDate).orEmpty().take(4),
                    backdropUrl = "$IMAGE_BASE_URL${item.backdropPath}"
                )
            }
    }

    private companion object {
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w1280"
        const val MAX_ITEMS = 8
        const val CACHE_TTL_MS = 60 * 60 * 1_000L
    }
}
