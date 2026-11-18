package com.raularribas.firelauncher.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raularribas.firelauncher.presentation.home.components.AppsCarousel
import com.raularribas.firelauncher.presentation.home.components.HeroBackground
import com.raularribas.firelauncher.presentation.home.components.HeroHeader
import com.raularribas.firelauncher.presentation.theme.Background

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {}
    BackHandler(enabled = state.movingPackage != null) { viewModel.cancelMove() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        HeroBackground(featured = state.currentFeatured)
        Column(modifier = Modifier.fillMaxSize()) {
            HeroHeader(
                featured = state.currentFeatured,
                featuredCount = state.featured.size,
                heroIndex = state.heroIndex,
                movingLabel = state.movingLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            AppsCarousel(
                apps = state.apps,
                movingPackage = state.movingPackage,
                onFocused = viewModel::onAppFocused,
                onClick = viewModel::onAppClick,
                onLongClick = viewModel::onAppLongClick,
                onMove = viewModel::onMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
