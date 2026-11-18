package com.raularribas.firelauncher.presentation.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.presentation.common.AppImageView
import com.raularribas.firelauncher.presentation.theme.Accent
import com.raularribas.firelauncher.presentation.theme.Background
import com.raularribas.firelauncher.presentation.theme.TextPrimary
import com.raularribas.firelauncher.presentation.theme.TextSecondary
import com.raularribas.firelauncher.presentation.theme.TileColor

private const val HIDDEN_PREFIX = "hidden:"

@Composable
fun EditAppsScreen(
    visible: List<LauncherApp>,
    hidden: List<LauncherApp>,
    movingPackage: String?,
    onMoveToggle: (String) -> Unit,
    onMove: (Boolean) -> Unit,
    onToggleHidden: (String, Boolean) -> Unit,
    onDone: () -> Unit
) {
    val requesters = remember { mutableMapOf<String, FocusRequester>() }
    var pendingFocus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        val first = visible.firstOrNull()?.packageName ?: hidden.firstOrNull()?.let { HIDDEN_PREFIX + it.packageName }
        runCatching { first?.let { requesters[it]?.requestFocus() } }
    }

    LaunchedEffect(visible, movingPackage) {
        val target = movingPackage ?: return@LaunchedEffect
        withFrameNanos { }
        runCatching { requesters[target]?.requestFocus() }
    }

    LaunchedEffect(visible, hidden) {
        val target = pendingFocus ?: return@LaunchedEffect
        withFrameNanos { }
        runCatching { requesters[target]?.requestFocus() }
        pendingFocus = null
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Background.copy(alpha = 0.92f))
            .padding(horizontal = 48.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Editar inicio", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Mover: pulsa Mover y usa ↑ ↓. Vuelve a pulsar para soltar.\n\nOcultar quita la app de Inicio sin desinstalarla.",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDone) {
                Text(text = "Listo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item(key = "header_visible") { SectionHeader("En Inicio (${visible.size})") }
            items(visible, key = { it.packageName }) { app ->
                val index = visible.indexOf(app)
                val isMoving = app.packageName == movingPackage
                EditRow(
                    app = app,
                    highlighted = isMoving,
                    primaryLabel = if (isMoving) "Soltar" else "Mover",
                    primaryRequester = requesters.getOrPut(app.packageName) { FocusRequester() },
                    onPrimary = { onMoveToggle(app.packageName) },
                    onPrimaryKey = if (isMoving) onMove else null,
                    secondaryLabel = "Ocultar",
                    onSecondary = {
                        pendingFocus = (visible.getOrNull(index + 1) ?: visible.getOrNull(index - 1))?.packageName
                        onToggleHidden(app.packageName, true)
                    }
                )
            }
            if (hidden.isNotEmpty()) {
                item(key = "header_hidden") { SectionHeader("Ocultas (${hidden.size})") }
                items(hidden, key = { HIDDEN_PREFIX + it.packageName }) { app ->
                    val index = hidden.indexOf(app)
                    EditRow(
                        app = app,
                        highlighted = false,
                        primaryLabel = "Mostrar",
                        primaryRequester = requesters.getOrPut(HIDDEN_PREFIX + app.packageName) { FocusRequester() },
                        onPrimary = {
                            pendingFocus = (hidden.getOrNull(index + 1) ?: hidden.getOrNull(index - 1))
                                ?.let { HIDDEN_PREFIX + it.packageName }
                                ?: visible.lastOrNull()?.packageName
                            onToggleHidden(app.packageName, false)
                        },
                        onPrimaryKey = null,
                        secondaryLabel = null,
                        onSecondary = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun EditRow(
    app: LauncherApp,
    highlighted: Boolean,
    primaryLabel: String,
    primaryRequester: FocusRequester,
    onPrimary: () -> Unit,
    onPrimaryKey: ((Boolean) -> Unit)?,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted) Accent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 96.dp, height = 54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TileColor)
        ) {
            AppImageView(
                packageName = app.packageName,
                label = app.label,
                iconSize = 36.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = app.label,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .focusRequester(primaryRequester)
                .onPreviewKeyEvent { event ->
                    val handler = onPrimaryKey ?: return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionUp, Key.DirectionDown -> {
                            if (event.type == KeyEventType.KeyDown) handler(event.key == Key.DirectionUp)
                            true
                        }
                        Key.DirectionLeft, Key.DirectionRight -> true
                        else -> false
                    }
                },
            colors = if (highlighted) {
                ButtonDefaults.colors(containerColor = Accent, contentColor = Background, focusedContainerColor = Accent, focusedContentColor = Background)
            } else {
                ButtonDefaults.colors()
            }
        ) {
            Text(text = primaryLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        if (secondaryLabel != null && onSecondary != null) {
            Button(onClick = onSecondary, enabled = !highlighted) {
                Text(text = secondaryLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
