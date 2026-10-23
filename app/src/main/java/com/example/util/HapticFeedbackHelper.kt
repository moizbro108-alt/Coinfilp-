package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackHelper(private val context: Context) {

  private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    vibratorManager?.defaultVibrator
  } else {
    @Suppress("DEPRECATION")
    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
  }

  fun tick() {
    vibrate(20L, 40)
  }

  fun flipSpin() {
    vibrate(45L, 80)
  }

  fun winCelebration() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val timings = longArrayOf(0, 70, 60, 90, 80, 160)
      val amplitudes = intArrayOf(0, 120, 0, 180, 0, 255)
      vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    } else {
      @Suppress("DEPRECATION")
      vibrator?.vibrate(longArrayOf(0, 70, 60, 90, 80, 160), -1)
    }
  }

  fun losePulse() {
    vibrate(120L, 100)
  }

  private fun vibrate(durationMs: Long, amplitude: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
    } else {
      @Suppress("DEPRECATION")
      vibrator?.vibrate(durationMs)
    }
  }
}
