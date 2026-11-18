package com.raularribas.firelauncher.presentation.home

import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp

data class AppsUiState(
    val apps: List<LauncherApp> = emptyList(),
    val hidden: List<LauncherApp> = emptyList(),
    val movingPackage: String? = null,
    val isEditing: Boolean = false
) {
    val movingLabel: String?
        get() = movingPackage?.let { pkg -> apps.firstOrNull { it.packageName == pkg }?.label }
}

data class HeroUiState(
    val featured: List<FeaturedContent> = emptyList(),
    val index: Int = 0
) {
    val current: FeaturedContent?
        get() = if (featured.isEmpty()) null else featured[index % featured.size]
}
