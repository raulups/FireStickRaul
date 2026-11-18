package com.raularribas.firelauncher.domain.model

data class AppCatalog(
    val visible: List<LauncherApp> = emptyList(),
    val hidden: List<LauncherApp> = emptyList()
)
