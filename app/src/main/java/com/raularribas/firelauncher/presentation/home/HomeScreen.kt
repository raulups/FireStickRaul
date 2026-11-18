package com.raularribas.firelauncher.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raularribas.firelauncher.presentation.edit.EditAppsScreen
import com.raularribas.firelauncher.presentation.home.components.AppsCarousel
import com.raularribas.firelauncher.presentation.home.components.HeroBackground
import com.raularribas.firelauncher.presentation.home.components.HeroHeader
import com.raularribas.firelauncher.presentation.theme.Background

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.appsState.collectAsStateWithLifecycle()

    val onFocused = remember(viewModel) { viewModel::onAppFocused }
    val onClick = remember(viewModel) { viewModel::onAppClick }
    val onLongClick = remember(viewModel) { viewModel::onAppLongClick }
    val onMove = remember(viewModel) { viewModel::onMove }
    val onEdit = remember(viewModel) { viewModel::openEditor }
    val onEditorMoveToggle = remember(viewModel) { viewModel::onEditorMoveToggle }
    val onEditorMove = remember(viewModel) { viewModel::onEditorMove }
    val onToggleHidden = remember(viewModel) { viewModel::onToggleHidden }
    val onCloseEditor = remember(viewModel) { viewModel::closeEditor }

    BackHandler(enabled = true) {}
    BackHandler(enabled = state.movingPackage != null && !state.isEditing) { viewModel.cancelMove() }
    BackHandler(enabled = state.isEditing) { viewModel.onEditorBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HeroBackground(heroState = viewModel.heroState)
        if (state.isEditing) {
            EditAppsScreen(
                visible = state.apps,
                hidden = state.hidden,
                movingPackage = state.movingPackage,
                onMoveToggle = onEditorMoveToggle,
                onMove = onEditorMove,
                onToggleHidden = onToggleHidden,
                onDone = onCloseEditor
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                HeroHeader(
                    heroState = viewModel.heroState,
                    movingLabel = state.movingLabel,
                    onEditClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                AppsCarousel(
                    apps = state.apps,
                    movingPackage = state.movingPackage,
                    onFocused = onFocused,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onMove = onMove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}
