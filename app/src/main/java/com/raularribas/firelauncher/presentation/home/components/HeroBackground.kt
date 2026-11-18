package com.raularribas.firelauncher.presentation.home.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.raularribas.firelauncher.data.image.BackdropRequests
import com.raularribas.firelauncher.presentation.home.HeroUiState
import com.raularribas.firelauncher.presentation.theme.Background
import com.raularribas.firelauncher.presentation.theme.DefaultHero
import kotlinx.coroutines.flow.StateFlow

private val horizontalScrim = Brush.horizontalGradient(
    listOf(Background.copy(alpha = 0.85f), Background.copy(alpha = 0.25f), Color.Transparent)
)

private val verticalScrim = Brush.verticalGradient(
    0f to Background.copy(alpha = 0.55f),
    0.18f to Color.Transparent,
    0.35f to Color.Transparent,
    0.6f to Background.copy(alpha = 0.6f),
    1f to Background.copy(alpha = 0.92f)
)

private val fallback = Brush.radialGradient(listOf(DefaultHero, Background), radius = 1_600f)

@Composable
fun HeroBackground(heroState: StateFlow<HeroUiState>) {
    val state by heroState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = state.current?.backdropUrl,
            animationSpec = tween(CROSSFADE_MS),
            label = "hero"
        ) { url ->
            if (url != null) {
                val request = remember(url) { BackdropRequests.build(context, url) }
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fallback)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(horizontalScrim)
                .background(verticalScrim)
        )
    }
}

private const val CROSSFADE_MS = 450
