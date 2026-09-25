package com.mojealterego.newgpt.nexus

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager

data class DeviceComputeSnapshot(
    val sdk: Int,
    val abis: List<String>,
    val cpuThreads: Int,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val lowRamDevice: Boolean,
    val batteryPercent: Int?,
    val batteryCurrentMicroAmps: Int?,
    val batteryTemperatureC: Double?,
    val thermalStatus: Int?,
    val powerSaveMode: Boolean
)

class RealDeviceComputeRuntime(private val context: Context) {

    fun snapshot(): DeviceComputeSnapshot {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memory = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memory)

        val battery = context.registerReceiver(
            null,
            IntentFilterCompat.batteryChanged()
        )

        val batteryPercent = battery?.let {
            val level = it.getIntExtra("level", -1)
            val scale = it.getIntExtra("scale", -1)
            if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else null
        }

        val current = if (Build.VERSION.SDK_INT >= 21) {
            context.getSystemService(BatteryManager::class.java)
                ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                ?.takeIf { it != Int.MIN_VALUE }
        } else null

        val temperature = battery?.getIntExtra("temperature", Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
            ?.div(10.0)

        val thermal = if (Build.VERSION.SDK_INT >= 29) {
            context.getSystemService(PowerManager::class.java)?.currentThermalStatus
        } else null

        val powerSave = context.getSystemService(PowerManager::class.java)?.isPowerSaveMode ?: false

        return DeviceComputeSnapshot(
            sdk = Build.VERSION.SDK_INT,
            abis = Build.SUPPORTED_ABIS.toList(),
            cpuThreads = Runtime.getRuntime().availableProcessors(),
            totalRamBytes = memory.totalMem,
            availableRamBytes = memory.availMem,
            lowRamDevice = memory.lowMemory,
            batteryPercent = batteryPercent,
            batteryCurrentMicroAmps = current,
            batteryTemperatureC = temperature,
            thermalStatus = thermal,
            powerSaveMode = powerSave
        )
    }

    fun recommendExecution(snapshot: DeviceComputeSnapshot): String {
        if (snapshot.lowRamDevice || snapshot.availableRamBytes < 768L * 1024L * 1024L) {
            return "LIGHT"
        }
        if (snapshot.powerSaveMode) return "EFFICIENT"
        if ((snapshot.thermalStatus ?: 0) >= 4) return "THERMAL_LIMITED"
        return "NORMAL"
    }
}

private object IntentFilterCompat {
    fun batteryChanged(): android.content.IntentFilter =
        android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
}
