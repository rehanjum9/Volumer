package com.volumer.app

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "volumer_settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val autoMonitoring = booleanPreferencesKey("auto_monitoring"); val publicVolume = intPreferencesKey("public_volume"); val returnMinimum = intPreferencesKey("return_minimum"); val crowdThreshold = intPreferencesKey("crowd_threshold"); val mode = stringPreferencesKey("mode"); val appAdjusted = booleanPreferencesKey("app_adjusted"); val savedVolume = intPreferencesKey("saved_volume"); val nearbyDevices = intPreferencesKey("nearby_devices"); val insidePrivate = booleanPreferencesKey("inside_private"); val privateLat = doublePreferencesKey("private_lat"); val privateLng = doublePreferencesKey("private_lng"); val privateRadius = floatPreferencesKey("private_radius"); val lastReason = stringPreferencesKey("last_reason")
    }
    val settings: Flow<VolumerSettings> = context.dataStore.data.map { p -> VolumerSettings(p[Keys.autoMonitoring] ?: false, p[Keys.publicVolume] ?: 20, p[Keys.returnMinimum] ?: 45, p[Keys.crowdThreshold] ?: 6, runCatching { EnvironmentMode.valueOf(p[Keys.mode] ?: EnvironmentMode.UNKNOWN.name) }.getOrDefault(EnvironmentMode.UNKNOWN), p[Keys.appAdjusted] ?: false, p[Keys.savedVolume] ?: 50, p[Keys.nearbyDevices] ?: 0, p[Keys.insidePrivate] ?: false, p[Keys.privateLat], p[Keys.privateLng], p[Keys.privateRadius] ?: 120f, p[Keys.lastReason] ?: "Not monitoring yet") }
    suspend fun setAutoMonitoring(v:Boolean)=edit{it[Keys.autoMonitoring]=v}; suspend fun setPublicVolume(v:Int)=edit{it[Keys.publicVolume]=v.coerceIn(5,60)}; suspend fun setReturnMinimum(v:Int)=edit{it[Keys.returnMinimum]=v.coerceIn(20,80)}; suspend fun setCrowdThreshold(v:Int)=edit{it[Keys.crowdThreshold]=v.coerceIn(2,20)}
    suspend fun setPrivatePlace(lat:Double,lng:Double,radius:Float=120f)=edit{it[Keys.privateLat]=lat;it[Keys.privateLng]=lng;it[Keys.privateRadius]=radius.coerceIn(50f,500f);it[Keys.insidePrivate]=true;it[Keys.lastReason]="Current location saved as private"}
    suspend fun clearPrivatePlace()=edit{it.remove(Keys.privateLat);it.remove(Keys.privateLng);it.remove(Keys.privateRadius);it[Keys.insidePrivate]=false;it[Keys.lastReason]="Private place removed"}
    suspend fun updateNearby(c:Int)=edit{it[Keys.nearbyDevices]=c.coerceAtLeast(0)}; suspend fun setInsidePrivate(v:Boolean)=edit{it[Keys.insidePrivate]=v}
    suspend fun markPublic(saved:Int,reason:String)=edit{it[Keys.mode]=EnvironmentMode.PUBLIC_CROWDED.name;it[Keys.appAdjusted]=true;it[Keys.savedVolume]=saved.coerceIn(0,100);it[Keys.lastReason]=reason}
    suspend fun markPublicAlreadyAdjusted(reason:String)=edit{it[Keys.mode]=EnvironmentMode.PUBLIC_CROWDED.name;it[Keys.lastReason]=reason}
    suspend fun markQuiet(reason:String)=edit{it[Keys.mode]=EnvironmentMode.PRIVATE_OR_QUIET.name;it[Keys.appAdjusted]=false;it[Keys.lastReason]=reason}
    private suspend inline fun edit(crossinline block:(MutablePreferences)->Unit){context.dataStore.edit{block(it)}}
}
