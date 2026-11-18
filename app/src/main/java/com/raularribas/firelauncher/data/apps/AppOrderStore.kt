package com.raularribas.firelauncher.data.apps

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.launcherDataStore by preferencesDataStore(name = "launcher")

@Singleton
class AppOrderStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.launcherDataStore
    private val orderKey = stringPreferencesKey("app_order")

    val order: Flow<List<String>> = store.data.map { prefs ->
        prefs[orderKey]?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
    }

    suspend fun save(packageNames: List<String>) {
        store.edit { it[orderKey] = packageNames.joinToString(SEPARATOR) }
    }

    private companion object {
        const val SEPARATOR = ","
    }
}
