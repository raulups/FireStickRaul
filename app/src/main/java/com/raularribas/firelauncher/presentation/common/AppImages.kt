package com.raularribas.firelauncher.presentation.common

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AppImage {
    val bitmap: ImageBitmap

    data class Banner(override val bitmap: ImageBitmap) : AppImage
    data class Icon(override val bitmap: ImageBitmap) : AppImage
}

@Singleton
class AppImageLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache = LruCache<String, AppImage>(MAX_ENTRIES)

    fun cached(packageName: String): AppImage? = cache.get(packageName)

    suspend fun load(packageName: String): AppImage? = cache.get(packageName) ?: withContext(Dispatchers.IO) {
        runCatching { decode(context.packageManager, packageName) }.getOrNull()?.also { cache.put(packageName, it) }
    }

    suspend fun preload(packageNames: List<String>) {
        withContext(Dispatchers.IO) {
            packageNames.forEach { if (cache.get(it) == null) load(it) }
        }
    }

    private fun decode(packageManager: PackageManager, packageName: String): AppImage {
        val component = packageManager.getLeanbackLaunchIntentForPackage(packageName)?.component
        val banner = component?.let { packageManager.getActivityBanner(it) }
            ?: packageManager.getApplicationBanner(packageName)
        return if (banner != null) {
            val bitmap = banner.toBitmap(BANNER_WIDTH, BANNER_HEIGHT).apply { prepareToDraw() }
            AppImage.Banner(bitmap.asImageBitmap())
        } else {
            val bitmap = packageManager.getApplicationIcon(packageName).toBitmap(ICON_SIZE, ICON_SIZE).apply { prepareToDraw() }
            AppImage.Icon(bitmap.asImageBitmap())
        }
    }

    private companion object {
        const val MAX_ENTRIES = 120
        const val BANNER_WIDTH = 400
        const val BANNER_HEIGHT = 225
        const val ICON_SIZE = 160
    }
}

val LocalAppImageLoader = staticCompositionLocalOf<AppImageLoader> { error("AppImageLoader no disponible") }

@Composable
fun AppImageView(
    packageName: String,
    label: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    val loader = LocalAppImageLoader.current
    val image by produceState(initialValue = loader.cached(packageName), packageName) {
        if (value == null) value = loader.load(packageName)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (val current = image) {
            is AppImage.Banner -> Image(
                bitmap = current.bitmap,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            is AppImage.Icon -> Image(
                bitmap = current.bitmap,
                contentDescription = label,
                modifier = Modifier.size(iconSize)
            )
            null -> Unit
        }
    }
}
