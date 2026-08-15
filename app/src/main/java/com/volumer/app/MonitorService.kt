package com.volumer.app

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class MonitorService : Service() {
    companion object {
        const val ACTION_START = "com.volumer.app.START_MONITOR"
        const val ACTION_STOP = "com.volumer.app.STOP_MONITOR"
        private const val CHANNEL_ID = "volumer_monitor"
        private const val NOTIFICATION_ID = 1707
        @Volatile var isRunning: Boolean = false; private set
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private lateinit var repository: SettingsRepository
    private lateinit var scanner: BleCrowdScanner
    private lateinit var environment: EnvironmentController
    override fun onCreate() { super.onCreate(); repository = SettingsRepository(applicationContext); scanner = BleCrowdScanner(applicationContext); environment = EnvironmentController(applicationContext); isRunning = true; createChannel() }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { if (intent?.action == ACTION_STOP) stopMonitoringAndRestore() else startMonitoring(); return START_NOT_STICKY }
    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        startForeground(NOTIFICATION_ID, buildNotification("Starting context monitor…"))
        monitorJob = scope.launch {
            repository.setAutoMonitoring(true); var quietStreak = 0
            while (isActive) {
                val settings = repository.settings.first()
                if (settings.insidePrivatePlace) { quietStreak++; environment.enterQuiet("Inside your private place"); updateNotification("Private place • volume safe"); delay(60_000); continue }
                val count = scanner.scanUniqueNearbyDevices(8_000); repository.updateNearby(count)
                if (VolumePolicy.isCrowded(count, settings.crowdDeviceThreshold)) { quietStreak = 0; environment.enterPublic("Crowd detected: $count nearby BLE devices"); updateNotification("Public/crowded • media ${settings.publicVolumePercent}%") }
                else { quietStreak++; updateNotification("$count nearby BLE devices • quiet signal"); if (quietStreak >= 2) environment.enterQuiet("Quiet/private signal confirmed: $count nearby BLE devices") }
                delay(52_000)
            }
        }
    }
    private fun stopMonitoringAndRestore() { monitorJob?.cancel(); monitorJob = null; scope.launch { environment.enterQuiet("Auto monitor stopped"); repository.setAutoMonitoring(false); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() } }
    override fun onDestroy() { isRunning = false; monitorJob?.cancel(); scope.cancel(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    private fun createChannel() { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(NotificationChannel(CHANNEL_ID, "Volumer auto monitor", NotificationManager.IMPORTANCE_LOW)) }
    private fun buildNotification(text: String): Notification {
        val openIntent = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getService(this, 2, Intent(this, MonitorService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_lock_silent_mode_off).setContentTitle("Volumer is monitoring").setContentText(text).setContentIntent(openIntent).setOngoing(true).setOnlyAlertOnce(true).addAction(0, "Stop", stopIntent).build()
    }
    private fun updateNotification(text: String) { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, buildNotification(text)) }
}
