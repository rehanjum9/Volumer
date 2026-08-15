package com.volumer.app

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application.applicationContext
    private val repository = SettingsRepository(app)
    private val volume = VolumeController(app)
    private val geofence = GeofenceManager(app)
    private val environment = EnvironmentController(app)
    init { if (!MonitorService.isRunning) viewModelScope.launch { repository.setAutoMonitoring(false) } }
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), VolumerSettings())
    fun currentVolumePercent(): Int = volume.currentPercent()
    fun startMonitor() { ContextCompat.startForegroundService(app, Intent(app, MonitorService::class.java).setAction(MonitorService.ACTION_START)) }
    fun stopMonitor() { app.startService(Intent(app, MonitorService::class.java).setAction(MonitorService.ACTION_STOP)) }
    fun simulatePublic() = viewModelScope.launch { environment.enterPublic("Manual test: public mode") }
    fun simulateQuiet() = viewModelScope.launch { environment.enterQuiet("Manual test: private/quiet mode") }
    fun markCurrentLocationPrivate(onResult: (Boolean, String) -> Unit) {
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { onResult(false, "Location permission is required first."); return }
        val manager = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isLocationEnabled) { onResult(false, "Turn on phone Location first."); return }
        viewModelScope.launch {
            try {
                val location = LocationServices.getFusedLocationProviderClient(app).getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).await()
                if (location == null) { onResult(false, "Could not get your current location."); return@launch }
                repository.setPrivatePlace(location.latitude, location.longitude, 120f)
                geofence.register(location.latitude, location.longitude, 120f) { ok -> onResult(ok, if (ok) "Private place saved." else "Saved, but geofence needs background location permission.") }
            } catch (e: Exception) { onResult(false, e.message ?: "Could not save private place.") }
        }
    }
    fun clearPrivatePlace() = viewModelScope.launch { geofence.remove(); repository.clearPrivatePlace() }
    fun hasBackgroundLocation(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
}
