package com.raularribas.firelauncher.domain.repository

import com.raularribas.firelauncher.domain.model.LauncherApp
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    fun observeApps(): Flow<List<LauncherApp>>
    suspend fun saveOrder(packageNames: List<String>)
    fun launch(packageName: String)
}
