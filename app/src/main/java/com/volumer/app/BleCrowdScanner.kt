package com.volumer.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class BleCrowdScanner(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    fun hasPermission(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED else ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    @SuppressLint("MissingPermission") suspend fun scanUniqueNearbyDevices(durationMs: Long = 8_000L): Int {
        if (!hasPermission()) return 0
        val adapter = bluetoothManager.adapter ?: return 0
        if (!adapter.isEnabled) return 0
        val scanner = adapter.bluetoothLeScanner ?: return 0
        val seen = ConcurrentHashMap.newKeySet<String>(); val handler = Handler(Looper.getMainLooper())
        return suspendCancellableCoroutine { c ->
            val cb = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) { if (result.rssi >= -85) seen += runCatching { result.device.address }.getOrNull() ?: result.scanRecord?.bytes?.contentHashCode()?.toString() ?: "${result.rssi}:${result.timestampNanos}" }
                override fun onBatchScanResults(results: MutableList<ScanResult>) { results.filter { it.rssi >= -85 }.forEach { r -> seen += runCatching { r.device.address }.getOrNull() ?: r.scanRecord?.bytes?.contentHashCode()?.toString() ?: "${r.rssi}:${r.timestampNanos}" } }
                override fun onScanFailed(errorCode: Int) { if (c.isActive) c.resume(0) }
            }
            val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
            try { scanner.startScan(null, settings, cb) } catch (_: Exception) { c.resume(0); return@suspendCancellableCoroutine }
            val finish = Runnable { runCatching { scanner.stopScan(cb) }; if (c.isActive) c.resume(seen.size) }
            handler.postDelayed(finish, durationMs)
            c.invokeOnCancellation { handler.removeCallbacks(finish); runCatching { scanner.stopScan(cb) } }
        }
    }
}
