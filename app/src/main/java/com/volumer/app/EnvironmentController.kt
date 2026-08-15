package com.volumer.app

import android.content.Context
import kotlinx.coroutines.flow.first

class EnvironmentController(context: Context) {
    private val appContext = context.applicationContext
    private val repository = SettingsRepository(appContext)
    private val volume = VolumeController(appContext)
    suspend fun enterPublic(reason: String) { val settings = repository.settings.first(); if (!settings.appAdjustedVolume) { val before = volume.currentPercent(); volume.setPercent(settings.publicVolumePercent); repository.markPublic(before, reason) } else { volume.setPercent(settings.publicVolumePercent); repository.markPublicAlreadyAdjusted(reason) } }
    suspend fun enterQuiet(reason: String) { val settings = repository.settings.first(); if (settings.appAdjustedVolume) { val target = VolumePolicy.returnVolume(settings.savedVolumePercent, settings.returnMinimumPercent); volume.setPercent(target) }; repository.markQuiet(reason) }
}
