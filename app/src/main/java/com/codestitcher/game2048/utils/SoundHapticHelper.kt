package com.codestitcher.game2048.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundHapticHelper(context: Context) {
    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isHapticsEnabled: Boolean = true
    var isSoundEnabled: Boolean = true

    private var soundPool: SoundPool? = null
    private var swipeSoundId: Int = 0
    private var mergeSoundId: Int = 0
    private var isSoundLoaded: Boolean = false

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            val pkg = appContext.packageName
            val swipeResId = appContext.resources.getIdentifier("swipe", "raw", pkg)
            val mergeResId = appContext.resources.getIdentifier("merge", "raw", pkg)

            var loadedCount = 0
            val totalToLoad = (if (swipeResId != 0) 1 else 0) + (if (mergeResId != 0) 1 else 0)

            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    loadedCount++
                    if (loadedCount >= totalToLoad) {
                        isSoundLoaded = true
                    }
                }
            }

            if (swipeResId != 0) {
                swipeSoundId = pool.load(appContext, swipeResId, 1)
            }
            if (mergeResId != 0) {
                mergeSoundId = pool.load(appContext, mergeResId, 1)
            }

            soundPool = pool
        } catch (_: Exception) {
            soundPool = null
        }
    }

    fun playSwipeSound() {
        if (!isSoundEnabled || soundPool == null || !isSoundLoaded) return
        try {
            if (swipeSoundId != 0) {
                soundPool?.play(swipeSoundId, 0.65f, 0.65f, 1, 0, 1.0f)
            }
        } catch (_: Exception) {}
    }

    fun playMergeSound() {
        if (!isSoundEnabled || soundPool == null || !isSoundLoaded) return
        try {
            if (mergeSoundId != 0) {
                soundPool?.play(mergeSoundId, 0.85f, 0.85f, 2, 0, 1.0f)
            }
        } catch (_: Exception) {}
    }

    fun performMoveHaptic() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    fun performMergeHaptic() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(28)
            }
        } catch (_: Exception) {}
    }

    fun performGameOverHaptic() {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 40, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(80)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }
}
