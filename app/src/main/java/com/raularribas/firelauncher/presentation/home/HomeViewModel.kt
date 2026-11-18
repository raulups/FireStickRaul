package com.raularribas.firelauncher.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raularribas.firelauncher.domain.model.AppCatalog
import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.domain.repository.AppsRepository
import com.raularribas.firelauncher.domain.repository.FeaturedRepository
import com.raularribas.firelauncher.presentation.common.AppImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val featuredRepository: FeaturedRepository,
    private val appImageLoader: AppImageLoader
) : ViewModel() {

    private val catalog: StateFlow<AppCatalog> = appsRepository.observeCatalog()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppCatalog())

    private val draft = MutableStateFlow<List<LauncherApp>?>(null)
    private val moving = MutableStateFlow<String?>(null)
    private val editing = MutableStateFlow(false)
    private val featured = MutableStateFlow<List<FeaturedContent>>(emptyList())
    private val heroIndex = MutableStateFlow(0)
    private val focusedApp = MutableStateFlow<LauncherApp?>(null)

    val appsState: StateFlow<AppsUiState> = combine(catalog, draft, moving, editing) { current, pending, movingPkg, isEditing ->
        AppsUiState(
            apps = pending ?: current.visible,
            hidden = current.hidden,
            movingPackage = movingPkg,
            isEditing = isEditing
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppsUiState())

    val heroState: StateFlow<HeroUiState> = combine(featured, heroIndex) { items, index -> HeroUiState(items, index) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HeroUiState())

    init {
        viewModelScope.launch {
            catalog
                .map { it.visible + it.hidden }
                .filter { it.isNotEmpty() }
                .distinctUntilChangedBy { apps -> apps.map { it.packageName }.toSet() }
                .collectLatest { apps ->
                    launch { appImageLoader.preload(apps.map { it.packageName }) }
                    featuredRepository.refresh(catalog.value.visible)
                    focusedApp.value?.let { showFeaturedFor(it) }
                }
        }
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_MS)
                featuredRepository.refresh(catalog.value.visible)
            }
        }
        viewModelScope.launch {
            focusedApp
                .filterNotNull()
                .distinctUntilChangedBy { it.packageName }
                .debounce(FOCUS_DEBOUNCE_MS)
                .collectLatest { showFeaturedFor(it) }
        }
        viewModelScope.launch {
            featured
                .collectLatest { items ->
                    heroIndex.value = 0
                    if (items.size < 2) return@collectLatest
                    while (true) {
                        delay(ROTATION_MS)
                        heroIndex.value = (heroIndex.value + 1) % items.size
                    }
                }
        }
    }

    fun onAppFocused(app: LauncherApp) {
        if (moving.value != null) return
        focusedApp.value = app
    }

    fun onAppClick(packageName: String) {
        if (moving.value != null) confirmMove() else appsRepository.launch(packageName)
    }

    fun onAppLongClick(packageName: String) {
        if (moving.value != null) return
        draft.value = catalog.value.visible
        moving.value = packageName
    }

    fun onMove(direction: MoveDirection) {
        swapMoving { from -> direction.target(from) }
    }

    fun onEditorMove(up: Boolean) {
        swapMoving { from -> if (up) from - 1 else from + 1 }
    }

    fun onEditorMoveToggle(packageName: String) {
        when (moving.value) {
            packageName -> confirmMove()
            null -> {
                draft.value = catalog.value.visible
                moving.value = packageName
            }
        }
    }

    fun onToggleHidden(packageName: String, hidden: Boolean) {
        if (moving.value != null) return
        viewModelScope.launch { appsRepository.setHidden(packageName, hidden) }
    }

    fun openEditor() {
        cancelMove()
        editing.value = true
    }

    fun onEditorBack() {
        if (moving.value != null) cancelMove() else editing.value = false
    }

    fun closeEditor() {
        if (moving.value != null) confirmMove()
        editing.value = false
    }

    fun confirmMove() {
        val list = draft.value ?: return
        viewModelScope.launch {
            appsRepository.saveOrder(list.map { it.packageName })
            moving.value = null
            draft.value = null
        }
    }

    fun cancelMove() {
        moving.value = null
        draft.value = null
    }

    private suspend fun showFeaturedFor(app: LauncherApp) {
        var items = featuredRepository.cachedFor(app)
        if (items.isEmpty()) {
            featuredRepository.refresh(listOf(app))
            items = featuredRepository.cachedFor(app)
        }
        if (items.isNotEmpty() && items != featured.value) featured.value = items
    }

    private inline fun swapMoving(target: (Int) -> Int) {
        val list = draft.value?.toMutableList() ?: return
        val packageName = moving.value ?: return
        val from = list.indexOfFirst { it.packageName == packageName }
        if (from < 0) return
        val to = target(from)
        if (to !in list.indices) return
        Collections.swap(list, from, to)
        draft.value = list
    }

    private companion object {
        const val ROTATION_MS = 8_000L
        const val FOCUS_DEBOUNCE_MS = 150L
        const val REFRESH_MS = 60 * 60 * 1_000L
    }
}
