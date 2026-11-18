package com.raularribas.firelauncher.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.raularribas.firelauncher.MainActivity

class HomeRedirectService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var longPressHandled = false
    private val longPress = Runnable {
        longPressHandled = true
        openSettings()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_HOME) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (event.repeatCount == 0) {
                longPressHandled = false
                handler.postDelayed(longPress, LONG_PRESS_MS)
            }
            KeyEvent.ACTION_UP -> {
                handler.removeCallbacks(longPress)
                if (!longPressHandled) openLauncher()
            }
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.packageName?.toString() != AMAZON_LAUNCHER) return
        openLauncher()
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacks(longPress)
        super.onDestroy()
    }

    private fun openLauncher() {
        runCatching {
            startActivity(
                Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            )
        }
    }

    private fun openSettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private companion object {
        const val AMAZON_LAUNCHER = "com.amazon.tv.launcher"
        const val LONG_PRESS_MS = 600L
    }
}
