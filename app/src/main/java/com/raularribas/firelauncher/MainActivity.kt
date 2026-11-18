package com.raularribas.firelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.raularribas.firelauncher.presentation.common.AppImageLoader
import com.raularribas.firelauncher.presentation.common.LocalAppImageLoader
import com.raularribas.firelauncher.presentation.home.HomeScreen
import com.raularribas.firelauncher.presentation.theme.FireLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appImageLoader: AppImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalAppImageLoader provides appImageLoader) {
                FireLauncherTheme {
                    HomeScreen()
                }
            }
        }
    }
}
