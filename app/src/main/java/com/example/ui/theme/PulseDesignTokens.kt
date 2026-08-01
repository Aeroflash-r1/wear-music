package com.example.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object PulseSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object PulseRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 28.dp
    val full = 999.dp
}

object PulseIconSizes {
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 32.dp
}

object PulsePadding {
    val ScreenHorizontal = 12.dp
    val ScreenVertical = 32.dp
    val ScreenContent = PaddingValues(horizontal = ScreenHorizontal, vertical = ScreenVertical)
    val Card = 16.dp
    val ListItem = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
}

object PulseTouch {
    val MinTarget = 48.dp
}

object PulseElevation {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 3.dp
    val Level3 = 6.dp
    val Level4 = 8.dp
    val Level5 = 12.dp
}

object PulseAnimations {
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 500

    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)

    fun <T> standardTween() = tween<T>(durationMillis = DurationMedium, easing = StandardEasing)
    fun <T> emphasizedTween() = tween<T>(durationMillis = DurationMedium, easing = EmphasizedEasing)
    fun <T> bouncySpring() = spring<T>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    fun <T> subtleSpring() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
}
