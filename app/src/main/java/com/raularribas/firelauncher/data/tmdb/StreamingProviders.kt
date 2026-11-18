package com.raularribas.firelauncher.data.tmdb

import com.raularribas.firelauncher.domain.model.LauncherApp
import java.util.Locale

data class StreamingProvider(
    val id: Int,
    val name: String,
    val labels: List<String>,
    val packages: List<String> = emptyList()
)

object StreamingProviders {
    const val REGION = "GB"
    const val LANGUAGE = "es-ES"

    val all = listOf(
        StreamingProvider(9, "Prime Video", listOf("prime video", "amazon prime video"), listOf("com.amazon.firebat", "com.amazon.avod", "com.amazon.avod.thirdpartyclient")),
        StreamingProvider(38, "BBC iPlayer", listOf("bbc iplayer", "iplayer"), listOf("uk.co.bbc.iplayer")),
        StreamingProvider(103, "Channel 4", listOf("channel 4", "channel4"), listOf("com.channel4.ondemand")),
        StreamingProvider(41, "ITVX", listOf("itvx", "itv x", "itv hub")),
        StreamingProvider(39, "NOW", listOf("now", "now tv")),
        StreamingProvider(8, "Netflix", listOf("netflix"), listOf("com.netflix.ninja", "com.netflix.mediaclient")),
        StreamingProvider(337, "Disney+", listOf("disney+", "disney plus"), listOf("com.disney.disneyplus"))
    )

    fun forApp(app: LauncherApp): StreamingProvider? {
        val label = app.label.trim().lowercase(Locale.ROOT)
        return all.firstOrNull { provider ->
            app.packageName in provider.packages || provider.labels.any { label == it || label.startsWith("$it ") }
        }
    }
}
