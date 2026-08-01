package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.ui.theme.PulseIconSizes
import com.example.ui.theme.PulsePadding
import com.example.ui.theme.PulseRadius
import com.example.ui.theme.PulseSpacing
import com.example.utils.PulseHaptics
import com.example.utils.rememberPulseHapticFeedback
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable

/**
 * Standard Wear OS screen shell: gives every screen the auto-hiding TimeText and
 * scroll-position indicator (fades in/out based on scroll activity) for free,
 * instead of each screen managing that itself. Screens keep using their existing
 * ScalingLazyColumn/contentPadding exactly as before inside [content].
 */
@Composable
fun PulseScreenScaffold(
    scrollState: ScalingLazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ScreenScaffold(
        scrollState = scrollState,
        modifier = modifier,
        timeText = { TimeText() }
    ) {
        content()
    }
}

@Composable
fun Modifier.pulseRotaryScroll(
    listState: ScalingLazyListState,
    focusRequester: FocusRequester = remember { FocusRequester() }
): Modifier {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    return this
        .focusRequester(focusRequester)
        .focusable()
        .rotaryScrollable(
            behavior = RotaryScrollableDefaults.behavior(scrollableState = listState),
            focusRequester = focusRequester
        )
}

@Composable
fun PulseCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = rememberPulseHapticFeedback()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PulseRadius.xl))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(PulseRadius.xl)
            )
            .clickable {
                haptic.performClick()
                onClick()
            }
            .padding(PulsePadding.Card),
        verticalArrangement = Arrangement.spacedBy(PulseSpacing.xs)
    ) {
        content()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PulseListItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    secondaryLabel: String? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val haptic = rememberPulseHapticFeedback()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(PulseRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(PulseRadius.lg)
            )
            .combinedClickable(
                onClick = {
                    haptic.performClick()
                    onClick()
                },
                onLongClick = onLongClick?.let {
                    {
                        haptic.performClick()
                        it()
                    }
                }
            )
            .padding(PulsePadding.ListItem),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.md),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(PulseSpacing.md))
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            if (secondaryLabel != null) {
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(PulseSpacing.md))
            trailingContent()
        }
    }
}

@Composable
fun PulseSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.padding(bottom = PulseSpacing.xs, top = PulseSpacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PulseButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) = PulsePrimaryButton(onClick = onClick, label = label, modifier = modifier, icon = icon)

@Composable
fun PulsePrimaryButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val haptic = rememberPulseHapticFeedback()
    Button(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.md)
                )
                Spacer(modifier = Modifier.width(PulseSpacing.sm))
            }
            Text(text = label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PulseSecondaryButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val haptic = rememberPulseHapticFeedback()
    Button(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(PulseIconSizes.md),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(PulseSpacing.sm))
            }
            Text(text = label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PulseIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val haptic = rememberPulseHapticFeedback()
    IconButton(
        onClick = {
            haptic.performClick()
            onClick()
        },
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
    ) {
        androidx.compose.animation.Crossfade(
            targetState = icon,
            animationSpec = com.example.ui.theme.PulseAnimations.standardTween(),
            label = "icon_crossfade"
        ) { targetIcon ->
            Icon(
                imageVector = targetIcon,
                contentDescription = contentDescription,
                tint = Color.White
            )
        }
    }
}

@Composable
fun PulseLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(PulseIconSizes.xl)
        )
    }
}

@Composable
fun PulseEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(PulseSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PulseIconSizes.xl),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(PulseSpacing.sm))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PulseDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = PulseSpacing.sm)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    )
}
