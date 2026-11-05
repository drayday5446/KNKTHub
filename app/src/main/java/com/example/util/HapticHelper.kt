package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * High-performance haptics engine tailored for Samsung One UI & modern Android devices.
 * Uses refined VibrationEffect primitives for tactile micro-interactions.
 */
object HapticHelper {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Subtle, crisp tick when sending a message.
     */
    fun messageSend(context: Context) {
        val v = getVibrator(context) ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(10, 85))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(10)
            }
        } catch (_: Exception) {}
    }

    /**
     * Crisp double pulse pattern for incoming alerts and messages.
     */
    fun incomingNotification(context: Context) {
        val v = getVibrator(context) ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 30, 60, 30)
                val amplitudes = intArrayOf(0, 90, 0, 150)
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 30, 60, 30), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Micro-haptic texture feedback when swiping cards or peeking windows.
     */
    fun gestureSwipe(context: Context) {
        val v = getVibrator(context) ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(8, 60))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(8)
            }
        } catch (_: Exception) {}
    }

    /**
     * Firm, prominent haptic accent on long-pressing items for selection / settings.
     */
    fun longPress(context: Context) {
        val v = getVibrator(context) ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(24, 180))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(24)
            }
        } catch (_: Exception) {}
    }

    /**
     * Sharp, light click for emoji reactions and toggle chips.
     */
    fun reactionClick(context: Context) {
        val v = getVibrator(context) ?: return
        if (!v.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(12, 110))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(12)
            }
        } catch (_: Exception) {}
    }
}
