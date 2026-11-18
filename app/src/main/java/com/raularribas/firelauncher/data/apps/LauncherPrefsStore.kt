package com.raularribas.firelauncher.data.apps

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.launcherDataStore by preferencesDataStore(name = "launcher")

data class LauncherPrefs(
    val order: List<String> = emptyList(),
    val hidden: Set<String> = emptySet(),
    val shown: Set<String> = emptySet()
)

@Singleton
class LauncherPrefsStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.launcherDataStore
    private val orderKey = stringPreferencesKey("app_order_v2")
    private val hiddenKey = stringSetPreferencesKey("hidden_packages")
    private val shownKey = stringSetPreferencesKey("shown_packages")

    val prefs: Flow<LauncherPrefs> = store.data.map { prefs ->
        LauncherPrefs(
            order = prefs[orderKey]?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty(),
            hidden = prefs[hiddenKey].orEmpty(),
            shown = prefs[shownKey].orEmpty()
        )
    }.distinctUntilChanged()

    suspend fun saveOrder(packageNames: List<String>) {
        store.edit { it[orderKey] = packageNames.joinToString(SEPARATOR) }
    }

    suspend fun setHidden(packageName: String, hidden: Boolean) {
        store.edit { prefs ->
            val hiddenSet = prefs[hiddenKey].orEmpty().toMutableSet()
            val shownSet = prefs[shownKey].orEmpty().toMutableSet()
            if (hidden) {
                hiddenSet += packageName
                shownSet -= packageName
            } else {
                hiddenSet -= packageName
                shownSet += packageName
            }
            prefs[hiddenKey] = hiddenSet
            prefs[shownKey] = shownSet
        }
    }

    private companion object {
        const val SEPARATOR = ","
    }
}
