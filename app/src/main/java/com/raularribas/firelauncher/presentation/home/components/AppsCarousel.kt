package com.raularribas.firelauncher.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.presentation.home.APP_ROWS
import com.raularribas.firelauncher.presentation.home.MoveDirection

@Composable
fun AppsCarousel(
    apps: List<LauncherApp>,
    movingPackage: String?,
    onFocused: (LauncherApp) -> Unit,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit,
    onMove: (MoveDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val requesters = remember { mutableMapOf<String, FocusRequester>() }
    var initialFocusDone by remember { mutableStateOf(false) }

    LaunchedEffect(apps) {
        val first = apps.firstOrNull()?.packageName ?: return@LaunchedEffect
        if (initialFocusDone) return@LaunchedEffect
        withFrameNanos { }
        runCatching { requesters[first]?.requestFocus() }
        initialFocusDone = true
    }

    LaunchedEffect(apps, movingPackage) {
        val target = movingPackage ?: return@LaunchedEffect
        withFrameNanos { }
        runCatching { requesters[target]?.requestFocus() }
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(APP_ROWS),
        modifier = modifier,
        contentPadding = PaddingValues(start = 48.dp, end = 48.dp, top = 18.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            val requester = requesters.getOrPut(app.packageName) { FocusRequester() }
            AppTile(
                app = app,
                isMoving = app.packageName == movingPackage,
                focusRequester = requester,
                onFocused = { onFocused(app) },
                onClick = { onClick(app.packageName) },
                onLongClick = { onLongClick(app.packageName) },
                onMove = onMove,
                modifier = Modifier
                    .animateItem()
                    .size(width = 196.dp, height = 110.dp)
            )
        }
    }
}
