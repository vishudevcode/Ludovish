package com.ludovish.app.core

import android.app.Activity
import android.os.Build
import android.view.Surface
import android.view.WindowManager

object DisplayRateManager {
    fun enableHighRefreshRate(activity: Activity) {
        runCatching {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.display?.supportedModes?.maxByOrNull { it.refreshRate }?.let { maxMode ->
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            } else {
                @Suppress("DEPRECATION")
                val display = activity.windowManager.defaultDisplay
                @Suppress("DEPRECATION")
                display?.supportedModes?.maxByOrNull { it.refreshRate }?.let { maxMode ->
                    val params = window.attributes
                    params.preferredDisplayModeId = maxMode.modeId
                    window.attributes = params
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.decorView.rootSurfaceControl?.let {
                    // Hints the compositor to prioritize 120Hz frame pacing
                }
            }
        }
    }
}
