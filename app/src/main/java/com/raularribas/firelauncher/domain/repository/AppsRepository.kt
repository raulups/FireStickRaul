package com.raularribas.firelauncher.domain.repository

import com.raularribas.firelauncher.domain.model.AppCatalog
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    fun observeCatalog(): Flow<AppCatalog>
    suspend fun saveOrder(packageNames: List<String>)
    suspend fun setHidden(packageName: String, hidden: Boolean)
    fun launch(packageName: String)
}
