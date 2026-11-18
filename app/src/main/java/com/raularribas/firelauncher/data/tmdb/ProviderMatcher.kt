package com.raularribas.firelauncher.data.tmdb

import com.raularribas.firelauncher.domain.model.LauncherApp
import java.util.Locale

object ProviderMatcher {

    private val nonAlphanumeric = Regex("[^a-z0-9 ]")
    private val spaces = Regex("\\s+")

    fun find(app: LauncherApp, providers: List<ProviderDto>): ProviderDto? {
        if (providers.isEmpty()) return null
        val candidates = candidates(normalize(app.label)).map { normalize(it) }.distinct()
        val normalized = providers.map { it to normalize(it.name) }
        candidates.forEach { candidate ->
            normalized.firstOrNull { it.second == candidate }?.let { return it.first }
        }
        return candidates.firstNotNullOfOrNull { candidate ->
            normalized
                .filter { (_, name) -> name.startsWith("$candidate ") && "channel" !in name && "ads" !in name }
                .minByOrNull { it.first.displayPriority }
                ?.first
        }
    }

    private fun candidates(label: String): List<String> = when {
        "prime video" in label -> listOf("amazon prime video", "prime video")
        label == "max" || "hbo" in label -> listOf("max", "hbo max")
        "disney" in label -> listOf("disney plus", "disney+")
        "mediaset" in label || "mitele" in label -> listOf("mitele", "mediaset infinity")
        "movistar" in label -> listOf("movistar plus+", "movistar plus")
        "youtube" in label -> listOf("youtube", "youtube premium")
        "rtve" in label -> listOf("rtve play", "rtve")
        else -> listOf(label)
    }

    private fun normalize(value: String): String = value
        .lowercase(Locale.ROOT)
        .replace("+", " plus ")
        .replace(nonAlphanumeric, "")
        .replace(spaces, " ")
        .trim()
}
