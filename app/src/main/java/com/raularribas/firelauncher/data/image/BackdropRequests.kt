package com.raularribas.firelauncher.data.image

import android.content.Context
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

object BackdropRequests {

    fun build(context: Context, url: String, keepInMemory: Boolean = true): ImageRequest =
        ImageRequest.Builder(context)
            .data(url)
            .size(Size.ORIGINAL)
            .memoryCachePolicy(if (keepInMemory) CachePolicy.ENABLED else CachePolicy.DISABLED)
            .build()
}

@Singleton
class BackdropPreloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun preload(groups: List<List<String>>) {
        val loader = SingletonImageLoader.get(context)
        groups.forEach { urls ->
            urls.forEachIndexed { index, url ->
                loader.enqueue(BackdropRequests.build(context, url, keepInMemory = index < IN_MEMORY_PER_GROUP))
            }
        }
    }

    private companion object {
        const val IN_MEMORY_PER_GROUP = 2
    }
}
