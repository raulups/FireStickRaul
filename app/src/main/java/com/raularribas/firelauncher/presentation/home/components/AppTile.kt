package com.raularribas.firelauncher.presentation.home.components

import android.content.pm.PackageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.presentation.home.MoveDirection
import com.raularribas.firelauncher.presentation.theme.Accent
import com.raularribas.firelauncher.presentation.theme.TileColor
import com.raularribas.firelauncher.presentation.theme.TileShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface AppImage {
    val bitmap: ImageBitmap

    data class Banner(override val bitmap: ImageBitmap) : AppImage
    data class Icon(override val bitmap: ImageBitmap) : AppImage
}

@Composable
fun AppTile(
    app: LauncherApp,
    isMoving: Boolean,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMove: (MoveDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    val image by rememberAppImage(app.packageName)

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocused() }
            .onPreviewKeyEvent { event ->
                if (!isMoving) return@onPreviewKeyEvent false
                val direction = when (event.key) {
                    Key.DirectionLeft -> MoveDirection.LEFT
                    Key.DirectionRight -> MoveDirection.RIGHT
                    Key.DirectionUp -> MoveDirection.UP
                    Key.DirectionDown -> MoveDirection.DOWN
                    else -> null
                } ?: return@onPreviewKeyEvent false
                if (event.type == KeyEventType.KeyDown) onMove(direction)
                true
            },
        shape = ClickableSurfaceDefaults.shape(shape = TileShape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (isMoving) 1.12f else 1.08f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(elevationColor = Color.Black.copy(alpha = 0.7f), elevation = 18.dp)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = if (isMoving) Border(BorderStroke(3.dp, Accent), shape = TileShape) else Border.None
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TileColor,
            focusedContainerColor = TileColor,
            pressedContainerColor = TileColor
        )
    ) {
        when (val current = image) {
            is AppImage.Banner -> Image(
                bitmap = current.bitmap,
                contentDescription = app.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            is AppImage.Icon -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    bitmap = current.bitmap,
                    contentDescription = app.label,
                    modifier = Modifier.size(64.dp)
                )
            }
            null -> Unit
        }
    }
}

@Composable
private fun rememberAppImage(packageName: String): State<AppImage?> {
    val packageManager = LocalContext.current.packageManager
    return produceState<AppImage?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            runCatching { loadAppImage(packageManager, packageName) }.getOrNull()
        }
    }
}

private fun loadAppImage(packageManager: PackageManager, packageName: String): AppImage {
    val component = packageManager.getLeanbackLaunchIntentForPackage(packageName)?.component
    val banner = component?.let { packageManager.getActivityBanner(it) }
        ?: packageManager.getApplicationBanner(packageName)
    return if (banner != null) {
        AppImage.Banner(banner.toBitmap(640, 360).asImageBitmap())
    } else {
        AppImage.Icon(packageManager.getApplicationIcon(packageName).toBitmap(192, 192).asImageBitmap())
    }
}
