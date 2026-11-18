package com.raularribas.firelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.raularribas.firelauncher.presentation.home.HomeScreen
import com.raularribas.firelauncher.presentation.theme.FireLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FireLauncherTheme {
                HomeScreen()
            }
        }
    }
}
