package com.raularribas.firelauncher.data.apps

import com.raularribas.firelauncher.domain.model.LauncherApp
import java.util.Locale

object DefaultApps {

    private val priority = listOf(
        listOf("netflix"),
        listOf("prime video", "amazon prime video"),
        listOf("disney+", "disney plus", "disney"),
        listOf("hbo max", "hbo", "max"),
        listOf("youtube"),
        listOf("twitch"),
        listOf("spotify"),
        listOf("mediaset infinity", "mediaset", "mitele")
    )

    private val hiddenByDefault = listOf(
        "amazon kids", "amazon kids+", "kids+",
        "luna", "amazon luna",
        "amazon live", "live",
        "amazon minitv", "minitv", "mintv", "mini tv",
        "amazon music", "amazon música",
        "amazon apps", "apps", "appstore", "amazon appstore",
        "amazon photos", "amazon fotos", "fotos de amazon", "fotos", "photos"
    )

    fun priorityOf(app: LauncherApp): Int {
        val label = normalize(app.label)
        val index = priority.indexOfFirst { keys -> keys.any { matches(label, it) } }
        return if (index < 0) Int.MAX_VALUE else index
    }

    fun isHiddenByDefault(app: LauncherApp): Boolean {
        val label = normalize(app.label)
        return hiddenByDefault.any { matches(label, it) }
    }

    private fun matches(label: String, key: String): Boolean =
        label == key || label.startsWith("$key ") || (key.length >= 5 && label.startsWith(key))

    private fun normalize(value: String): String = value.trim().lowercase(Locale.ROOT)
}
