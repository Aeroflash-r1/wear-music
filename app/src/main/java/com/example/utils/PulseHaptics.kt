package com.example.utils

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class PulseHaptics(private val view: View) {
    fun performClick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun performSuccess() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
    
    fun performError() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    fun performScrollTick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}

@Composable
fun rememberPulseHapticFeedback(): PulseHaptics {
    val view = LocalView.current
    return remember(view) { PulseHaptics(view) }
}
