package com.garrettbutchko.minimate.content.helper.modifiers

import android.content.Intent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

data class ButtonSkim(
    val color: Color,
    val icon: ImageVector,
    val function: (() -> Unit)? = null,
    val string: String? = null
) {
    val isShared: Boolean get() = function == null && string != null
}

@Composable
fun SkimButtonView(buttonSkim: ButtonSkim, offsetX: Float, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val showThresholdPx = with(density) { -35.dp.toPx() }
    val fullyVisiblePx = with(density) { -50.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(buttonSkim.color)
            .clickable { buttonSkim.function?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (offsetX < showThresholdPx) {
            Icon(
                imageVector = buttonSkim.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(if (offsetX < fullyVisiblePx) 1f else 0f)
            )
        }
    }
}

@Composable
fun SkimShareLinkView(buttonSkim: ButtonSkim, offsetX: Float, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val showThresholdPx = with(density) { -35.dp.toPx() }
    val fullyVisiblePx = with(density) { -50.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(buttonSkim.color)
            .clickable {
                buttonSkim.string?.let { shareString ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareString)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share"))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (offsetX < showThresholdPx) {
            Icon(
                imageVector = buttonSkim.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(if (offsetX < fullyVisiblePx) 1f else 0f)
            )
        }
    }
}

/**
 * Android Jetpack Compose equivalent of SwiftUI's SwipeableRowModifier.
 * 
 * Note: In Jetpack Compose, structural changes like adding sibling action 
 * buttons beneath a row are achieved by wrapping your content in a `@Composable` 
 * container rather than using a `Modifier`.
 */
@Composable
fun SwipeableRow(
    editingID: String?,
    onEditingIDChange: (String?) -> Unit,
    id: String,
    buttonPressFunction: () -> Unit,
    buttonOne: ButtonSkim? = null,
    buttonTwo: ButtonSkim? = null,
    deleteFunction: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    var isPressed by remember { mutableStateOf(false) }
    var hasVibrated by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val density = LocalDensity.current
    val pausePointPx = with(density) { -100.dp.toPx() }
    val commitPointPx = with(density) { -50.dp.toPx() }
    val deletePointPx = with(density) { -220.dp.toPx() }
    val resetPointPx = 0f

    val showNonDeleteButtons = offsetX.value > deletePointPx
    val nonDeleteCount = (if (buttonOne != null) 1 else 0) + (if (buttonTwo != null) 1 else 0)
    val minActionWeight = 0.01f

    val nonDeleteVisibility by animateFloatAsState(
        targetValue = if (showNonDeleteButtons) 1f else minActionWeight,
        animationSpec = tween(durationMillis = 180),
        label = "nonDeleteVisibility"
    )

    val actionSpacing by animateDpAsState(
        targetValue = if (deleteFunction != null) (8f * nonDeleteVisibility).dp else 8.dp,
        animationSpec = tween(durationMillis = 180),
        label = "actionSpacing"
    )

    val deleteWeight by animateFloatAsState(
        targetValue = if (deleteFunction != null) {
            (1f + ((1f - nonDeleteVisibility) * nonDeleteCount)).coerceAtLeast(minActionWeight)
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 180),
        label = "deleteWeight"
    )

    // Close row if another row is opened
    LaunchedEffect(editingID) {
        if (editingID != id) {
            offsetX.animateTo(resetPointPx, spring())
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Background Action Buttons
        if (editingID == id) {
            BoxWithConstraints(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val maxRevealWidthPx = with(density) { maxWidth.toPx() }
                val revealWidth = max(0f, -offsetX.value - with(density) { 10.dp.toPx() })
                    .coerceAtMost(maxRevealWidthPx)
                Box(
                    modifier = Modifier
                        .width(with(density) { revealWidth.toDp() })
                        .alpha(if (offsetX.value < with(density) { -10.dp.toPx() }) 1f else 0f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(actionSpacing),
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                    ) {
                        if (deleteFunction != null) {
                            if (buttonOne != null) {
                                val buttonModifier = Modifier
                                    .weight(nonDeleteVisibility)
                                    .alpha(nonDeleteVisibility)
                                buttonOne.let {
                                    if (it.isShared) SkimShareLinkView(it, offsetX.value, buttonModifier)
                                    else SkimButtonView(it, offsetX.value, buttonModifier)
                                }
                            }
                            if (buttonTwo != null) {
                                val buttonModifier = Modifier
                                    .weight(nonDeleteVisibility)
                                    .alpha(nonDeleteVisibility)
                                buttonTwo.let {
                                    if (it.isShared) SkimShareLinkView(it, offsetX.value, buttonModifier)
                                    else SkimButtonView(it, offsetX.value, buttonModifier)
                                }
                            }
                            // Delete Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(deleteWeight)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(Color.Red)
                                    .clickable { deleteFunction.invoke() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (offsetX.value < with(density) { -35.dp.toPx() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete",
                                        tint = Color.White,
                                        modifier = Modifier.alpha(if (offsetX.value < commitPointPx) 1f else 0f)
                                    )
                                }
                            }
                        } else {
                            buttonOne?.let {
                                if (it.isShared) SkimShareLinkView(it, offsetX.value, Modifier.weight(1f))
                                else SkimButtonView(it, offsetX.value, Modifier.weight(1f))
                            }
                            buttonTwo?.let {
                                if (it.isShared) SkimShareLinkView(it, offsetX.value, Modifier.weight(1f))
                                else SkimButtonView(it, offsetX.value, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Main Content Row
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(id) {
                    detectTapGestures(
                        onPress = {
                            if (editingID != id) {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        },
                        onTap = {
                            if (editingID != id) {
                                buttonPressFunction()
                            }
                        },
                        onLongPress = {
                            if (editingID != id) {
                                buttonPressFunction()
                            }
                        }
                    )
                }
                .pointerInput(id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < deletePointPx) {
                                    deleteFunction?.invoke()
                                    offsetX.animateTo(resetPointPx, spring())
                                    onEditingIDChange(null)
                                } else if (offsetX.value < commitPointPx) {
                                    offsetX.animateTo(pausePointPx, spring())
                                } else {
                                    offsetX.animateTo(resetPointPx, spring())
                                    onEditingIDChange(null)
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(deletePointPx - 80f, resetPointPx)
                                offsetX.snapTo(newOffset)

                                if (offsetX.value < resetPointPx && editingID != id) {
                                    onEditingIDChange(id)
                                }

                                if (offsetX.value < deletePointPx && !hasVibrated) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    hasVibrated = true
                                } else if (offsetX.value > deletePointPx) {
                                    hasVibrated = false
                                }
                            }
                        }
                    )
                }
        ) {
            Box {
                content()
                if (isPressed) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.Black.copy(alpha = 0.06f))
                    )
                }
            }
        }
    }
}
