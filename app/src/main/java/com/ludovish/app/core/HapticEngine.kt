package com.ludovish.app.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(context: Context) {
    private val vibrator: Vibrator? = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }.getOrNull()

    fun tick() = safeVibrate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK) == true
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
                    .compose()
            )
        } else {
            vibrator?.vibrate(VibrationEffect.createOneShot(10, 60))
        }
    }

    fun tokenHop() = safeVibrate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK) == true
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f)
                    .compose()
            )
        } else {
            vibrator?.vibrate(VibrationEffect.createOneShot(18, 120))
        }
    }

    fun diceRollBurst() = safeVibrate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator?.areAllPrimitivesSupported(
                VibrationEffect.Composition.PRIMITIVE_TICK,
                VibrationEffect.Composition.PRIMITIVE_THUD
            ) == true
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f, 0)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.8f, 35)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 45)
                    .compose()
            )
        } else {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 15, 30, 15, 30, 25), intArrayOf(0, 90, 0, 140, 0, 220), -1))
        }
    }

    fun captureImpact() = safeVibrate {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator?.areAllPrimitivesSupported(
                VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                VibrationEffect.Composition.PRIMITIVE_THUD
            ) == true
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 50)
                    .compose()
            )
        } else {
            vibrator?.vibrate(VibrationEffect.createOneShot(85, 255))
        }
    }

    fun victoryFanfare() = safeVibrate {
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 40, 60, 40, 60, 120),
                intArrayOf(0, 180, 0, 220, 0, 255),
                -1
            )
        )
    }

    private inline fun safeVibrate(block: () -> Unit) {
        runCatching {
            if (vibrator?.hasVibrator() == true) block()
        }
    }
}
