package com.raularribas.firelauncher.presentation.home

import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp

data class HomeUiState(
    val apps: List<LauncherApp> = emptyList(),
    val featured: List<FeaturedContent> = emptyList(),
    val heroIndex: Int = 0,
    val movingPackage: String? = null
) {
    val currentFeatured: FeaturedContent?
        get() = if (featured.isEmpty()) null else featured[heroIndex % featured.size]

    val movingLabel: String?
        get() = movingPackage?.let { pkg -> apps.firstOrNull { it.packageName == pkg }?.label }
}
