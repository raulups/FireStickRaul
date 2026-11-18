package com.raularribas.firelauncher.presentation.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.presentation.common.AppImageView
import com.raularribas.firelauncher.presentation.home.MoveDirection
import com.raularribas.firelauncher.presentation.theme.Accent
import com.raularribas.firelauncher.presentation.theme.TileColor
import com.raularribas.firelauncher.presentation.theme.TileShape

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
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = when {
            isMoving && focused -> 1.12f
            focused -> 1.08f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tileScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tileElevation"
    )

    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .zIndex(if (focused) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation * 24.dp.toPx()
                shape = TileShape
                clip = true
            }
            .focusRequester(focusRequester)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocused()
            }
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
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f, pressedScale = 0.97f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = if (isMoving) Border(BorderStroke(3.dp, Accent), shape = TileShape) else Border.None
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TileColor,
            focusedContainerColor = TileColor,
            pressedContainerColor = TileColor
        )
    ) {
        AppImageView(
            packageName = app.packageName,
            label = app.label,
            modifier = Modifier.fillMaxSize()
        )
    }
}
