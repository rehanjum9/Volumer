package com.volumer.app

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt

class VolumeController(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val stream = AudioManager.STREAM_MUSIC
    fun currentPercent(): Int { val max = audio.getStreamMaxVolume(stream).coerceAtLeast(1); val current = audio.getStreamVolume(stream); return ((current * 100.0) / max).roundToInt().coerceIn(0, 100) }
    fun setPercent(percent: Int) { val max = audio.getStreamMaxVolume(stream).coerceAtLeast(1); val index = (max * percent.coerceIn(0, 100) / 100.0).roundToInt(); audio.setStreamVolume(stream, index.coerceIn(0, max), 0) }
}
