package com.raularribas.firelauncher.data.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.raularribas.firelauncher.domain.model.LauncherApp
import com.raularribas.firelauncher.domain.repository.AppsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val orderStore: AppOrderStore
) : AppsRepository {

    private val packageManager: PackageManager get() = context.packageManager

    override fun observeApps(): Flow<List<LauncherApp>> =
        combine(installedApps(), orderStore.order) { apps, order ->
            val positions = order.withIndex().associate { it.value to it.index }
            apps.sortedWith(
                compareBy<LauncherApp> { positions[it.packageName] ?: Int.MAX_VALUE }
                    .thenBy { it.label.lowercase() }
            )
        }

    override suspend fun saveOrder(packageNames: List<String>) {
        orderStore.save(packageNames)
    }

    override fun launch(packageName: String) {
        val intent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: packageManager.getLaunchIntentForPackage(packageName)
            ?: return
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun installedApps(): Flow<List<LauncherApp>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                launch { send(queryApps()) }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        send(queryApps())
        awaitClose { context.unregisterReceiver(receiver) }
    }.conflate().flowOn(Dispatchers.IO)

    private fun queryApps(): List<LauncherApp> {
        val leanback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return (packageManager.queryIntentActivities(leanback, 0) + packageManager.queryIntentActivities(launcher, 0))
            .map { it.activityInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != context.packageName }
            .map { LauncherApp(it.packageName, it.loadLabel(packageManager).toString()) }
    }
}
