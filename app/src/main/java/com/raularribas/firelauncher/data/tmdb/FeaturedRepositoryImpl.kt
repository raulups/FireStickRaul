package com.raularribas.firelauncher.data.tmdb

import com.raularribas.firelauncher.data.image.BackdropPreloader
import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.domain.repository.FeaturedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeaturedRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val diskCache: FeaturedDiskCache,
    private val preloader: BackdropPreloader
) : FeaturedRepository {

    private data class Candidate(val item: DiscoverItem, val isTv: Boolean) {
        val key: String get() = (if (isTv) "tv:" else "movie:") + item.id
    }

    private val refreshMutex = Mutex()
    private val loadMutex = Mutex()
    private val domainCache = ConcurrentHashMap<Int, List<FeaturedContent>>()

    @Volatile
    private var snapshot: FeaturedSnapshot? = null

    override suspend fun cachedFor(app: LauncherApp): List<FeaturedContent> {
        val current = snapshot()
        val provider = ProviderMatcher.find(app, current.providers) ?: return emptyList()
        return domainCache.getOrPut(provider.id) {
            current.featured[provider.id.toString()]?.items?.map { it.toDomain() }.orEmpty()
        }
    }

    override suspend fun refresh(apps: List<LauncherApp>) {
        if (TmdbConfig.apiKey.isBlank()) return
        refreshMutex.withLock {
            var current = snapshot()
            var changed = false
            val now = System.currentTimeMillis()

            if (current.providers.isEmpty() || now - current.providersAt > PROVIDERS_TTL_MS) {
                runCatching { fetchProviders() }.getOrNull()?.takeIf { it.isNotEmpty() }?.let {
                    current = current.copy(providers = it, providersAt = now)
                    changed = true
                }
            }

            val matched = apps.mapNotNull { ProviderMatcher.find(it, current.providers) }.distinctBy { it.id }
            val stale = matched.filter { provider ->
                current.featured[provider.id.toString()]?.let { now - it.savedAt > FEATURED_TTL_MS } ?: true
            }

            if (stale.isNotEmpty()) {
                val trendingRank = runCatching { fetchTrendingRank() }.getOrDefault(emptyMap())
                val fetched = coroutineScope {
                    stale.map { provider ->
                        async { provider.id to runCatching { fetchFeatured(provider, trendingRank) }.getOrNull().orEmpty() }
                    }.awaitAll()
                }.filter { it.second.isNotEmpty() }

                if (fetched.isNotEmpty()) {
                    current = current.copy(
                        featured = current.featured + fetched.associate { (id, items) -> id.toString() to CachedFeatured(now, items) }
                    )
                    fetched.forEach { (id, _) -> domainCache.remove(id) }
                    changed = true
                }
            }

            if (changed) {
                snapshot = current
                diskCache.write(current)
            }

            preloader.preload(
                matched.mapNotNull { provider -> current.featured[provider.id.toString()]?.items?.map { it.backdropUrl } }
            )
        }
    }

    private suspend fun snapshot(): FeaturedSnapshot =
        snapshot ?: loadMutex.withLock {
            snapshot ?: diskCache.read()
                .let { if (it.version == CACHE_VERSION) it else FeaturedSnapshot(version = CACHE_VERSION) }
                .also { snapshot = it }
        }

    private suspend fun fetchProviders(): List<ProviderDto> = coroutineScope {
        val key = TmdbConfig.apiKey
        val movies = async { api.movieProviders(key, TmdbConfig.REGION, TmdbConfig.LANGUAGE).results }
        val tv = async { api.tvProviders(key, TmdbConfig.REGION, TmdbConfig.LANGUAGE).results }
        (movies.await() + tv.await()).distinctBy { it.id }
    }

    private suspend fun fetchTrendingRank(): Map<String, Int> = coroutineScope {
        val key = TmdbConfig.apiKey
        val tv = (1..TRENDING_PAGES).map { page -> async { api.trendingTv(key, TmdbConfig.LANGUAGE, page).results } }
        val movies = (1..TRENDING_PAGES).map { page -> async { api.trendingMovies(key, TmdbConfig.LANGUAGE, page).results } }
        val rank = HashMap<String, Int>()
        tv.awaitAll().flatten().forEachIndexed { index, item -> rank.putIfAbsent("tv:${item.id}", index) }
        movies.awaitAll().flatten().forEachIndexed { index, item -> rank.putIfAbsent("movie:${item.id}", index) }
        rank
    }

    private suspend fun fetchFeatured(provider: ProviderDto, trendingRank: Map<String, Int>): List<CachedItem> = coroutineScope {
        val key = TmdbConfig.apiKey
        val tvFrom = dateMonthsAgo(TV_RECENT_MONTHS)
        val movieFrom = dateMonthsAgo(MOVIE_RECENT_MONTHS)
        val tv = (1..DISCOVER_PAGES).map { page ->
            async {
                api.discoverTv(key, TmdbConfig.REGION, provider.id, TmdbConfig.LANGUAGE, tvFrom, page).results.map { Candidate(it, true) }
            }
        }
        val movies = (1..DISCOVER_PAGES).map { page ->
            async {
                api.discoverMovie(key, TmdbConfig.REGION, provider.id, TmdbConfig.LANGUAGE, movieFrom, page).results.map { Candidate(it, false) }
            }
        }
        val candidates = (tv.awaitAll().flatten() + movies.awaitAll().flatten())
            .filter { it.item.backdropPath != null }
            .distinctBy { it.key }

        val trending = candidates
            .filter { it.key in trendingRank }
            .sortedBy { trendingRank.getValue(it.key) }
        val rest = candidates
            .filter { it.key !in trendingRank }
            .sortedByDescending { it.item.popularity }

        (trending + rest)
            .take(MAX_ITEMS)
            .map { (item, isTv) ->
                CachedItem(
                    providerName = provider.name,
                    title = item.name ?: item.title.orEmpty(),
                    kind = if (isTv) "Serie" else "Película",
                    year = (item.firstAirDate ?: item.releaseDate).orEmpty().take(4),
                    backdropUrl = "${TmdbConfig.IMAGE_BASE_URL}${item.backdropPath}"
                )
            }
    }

    private fun dateMonthsAgo(months: Int): String {
        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, -months) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    }

    private companion object {
        const val CACHE_VERSION = 2
        const val MAX_ITEMS = 8
        const val TRENDING_PAGES = 5
        const val DISCOVER_PAGES = 3
        const val TV_RECENT_MONTHS = 6
        const val MOVIE_RECENT_MONTHS = 18
        const val FEATURED_TTL_MS = 3 * 60 * 60 * 1_000L
        const val PROVIDERS_TTL_MS = 7 * 24 * 60 * 60 * 1_000L
    }
}
