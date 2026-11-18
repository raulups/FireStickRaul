package com.raularribas.firelauncher.domain.repository

import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp

interface FeaturedRepository {
    suspend fun cachedFor(app: LauncherApp): List<FeaturedContent>
    suspend fun refresh(apps: List<LauncherApp>)
}
