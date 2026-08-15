package com.volumer.app

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {
    companion object { const val PRIVATE_GEOFENCE_ID = "volumer_private_place" }
    private val client = LocationServices.getGeofencingClient(context)
    private val pendingIntent: PendingIntent by lazy { PendingIntent.getBroadcast(context, 4201, Intent(context, GeofenceReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE) }
    fun hasFineLocation(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    fun register(lat: Double, lng: Double, radiusMeters: Float, onResult: (Boolean) -> Unit = {}) {
        if (!hasFineLocation()) { onResult(false); return }
        val geofence = Geofence.Builder().setRequestId(PRIVATE_GEOFENCE_ID).setCircularRegion(lat, lng, radiusMeters).setExpirationDuration(Geofence.NEVER_EXPIRE).setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT).build()
        val request = GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).addGeofence(geofence).build()
        try { client.addGeofences(request, pendingIntent).addOnSuccessListener { onResult(true) }.addOnFailureListener { onResult(false) } } catch (_: SecurityException) { onResult(false) }
    }
    fun remove(onResult: (Boolean) -> Unit = {}) { client.removeGeofences(pendingIntent).addOnSuccessListener { onResult(true) }.addOnFailureListener { onResult(false) } }
}
