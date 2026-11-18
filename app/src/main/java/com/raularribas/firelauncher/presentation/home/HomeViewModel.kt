package com.raularribas.firelauncher.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raularribas.firelauncher.domain.model.FeaturedContent
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.domain.repository.AppsRepository
import com.raularribas.firelauncher.domain.repository.FeaturedRepository
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val featuredRepository: FeaturedRepository
) : ViewModel() {

    private val draft = MutableStateFlow<List<LauncherApp>?>(null)
    private val moving = MutableStateFlow<String?>(null)
    private val featured = MutableStateFlow<List<FeaturedContent>>(emptyList())
    private val heroIndex = MutableStateFlow(0)
    private val focusedApp = MutableStateFlow<LauncherApp?>(null)

    private val visibleApps = combine(appsRepository.observeApps(), draft) { saved, pending -> pending ?: saved }

    val state: StateFlow<HomeUiState> = combine(visibleApps, featured, heroIndex, moving) { apps, items, index, movingPkg ->
        HomeUiState(apps = apps, featured = items, heroIndex = index, movingPackage = movingPkg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            focusedApp
                .filterNotNull()
                .distinctUntilChangedBy { it.packageName }
                .debounce(FOCUS_DEBOUNCE_MS)
                .collectLatest { app ->
                    val items = featuredRepository.featuredFor(app)
                    if (items.isNotEmpty()) featured.value = items
                }
        }
        viewModelScope.launch {
            featured.collectLatest { items ->
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
        draft.value = state.value.apps
        moving.value = packageName
    }

    fun onMove(direction: MoveDirection) {
        val list = draft.value?.toMutableList() ?: return
        val packageName = moving.value ?: return
        val from = list.indexOfFirst { it.packageName == packageName }
        if (from < 0) return
        val to = direction.target(from)
        if (to !in list.indices) return
        Collections.swap(list, from, to)
        draft.value = list
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

    private companion object {
        const val ROTATION_MS = 8_000L
        const val FOCUS_DEBOUNCE_MS = 400L
    }
}
