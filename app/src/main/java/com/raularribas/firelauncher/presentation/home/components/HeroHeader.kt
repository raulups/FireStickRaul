package com.raularribas.firelauncher.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.raularribas.firelauncher.presentation.home.HeroUiState
import com.raularribas.firelauncher.presentation.theme.Accent
import com.raularribas.firelauncher.presentation.theme.Background
import com.raularribas.firelauncher.presentation.theme.TextPrimary
import com.raularribas.firelauncher.presentation.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HeroHeader(
    heroState: StateFlow<HeroUiState>,
    movingLabel: String?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.padding(horizontal = 48.dp, vertical = 24.dp)) {
        Button(
            onClick = onEditClick,
            modifier = Modifier.align(Alignment.TopStart),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
            colors = ButtonDefaults.colors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = TextPrimary,
                focusedContainerColor = TextPrimary,
                focusedContentColor = Background
            )
        ) {
            Text(text = "Editar inicio", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Clock(modifier = Modifier.align(Alignment.TopEnd))
        FeaturedInfo(heroState = heroState, modifier = Modifier.align(Alignment.BottomStart))
        if (movingLabel != null) {
            Text(
                text = "Moviendo «$movingLabel» · flechas para mover, OK para soltar",
                color = Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun FeaturedInfo(heroState: StateFlow<HeroUiState>, modifier: Modifier = Modifier) {
    val state by heroState.collectAsStateWithLifecycle()
    val featured = state.current ?: return
    Column(
        modifier = modifier.fillMaxWidth(0.6f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${featured.providerName} · Popular ahora",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = featured.title,
            color = TextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOf(featured.kind, featured.year).filter { it.isNotBlank() }.joinToString(" · "),
            color = TextSecondary,
            fontSize = 14.sp
        )
        Dots(count = state.featured.size, selected = state.index % state.featured.size.coerceAtLeast(1))
    }
}

@Composable
private fun Clock(modifier: Modifier = Modifier) {
    val locale = remember { Locale("es", "ES") }
    val timeFormat = remember { SimpleDateFormat("HH:mm", locale) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM", locale) }
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1_000)
        }
    }
    val time = timeFormat.format(now)
    val date = remember(time) { dateFormat.format(now).replaceFirstChar { it.titlecase(locale) } }
    Column(modifier = modifier, horizontalAlignment = Alignment.End) {
        Text(text = time, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Text(text = date, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.End)
    }
}

@Composable
private fun Dots(count: Int, selected: Int) {
    if (count <= 1) return
    Row(
        modifier = Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(width = if (index == selected) 18.dp else 4.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (index == selected) Color.White else Color.White.copy(alpha = 0.35f))
            )
        }
    }
}
