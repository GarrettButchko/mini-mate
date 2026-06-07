package com.garrettbutchko.minimate.content.helper.modifiers

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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

private val rowSnapSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun SkimButtonView(buttonSkim: ButtonSkim, offsetX: Float, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25.dp))
            .background(buttonSkim.color)
            .clickable {
                if (buttonSkim.isShared) {
                    buttonSkim.string?.let {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, it)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    }
                } else {
                    buttonSkim.function?.invoke()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (offsetX < with(density) { -35.dp.toPx() }) {
            Icon(
                imageVector = buttonSkim.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.alpha(if (offsetX < with(density) { -50.dp.toPx() }) 1f else 0f)
            )
        }
    }
}

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

    val nonDeleteCount = (if (buttonOne != null) 1 else 0) + (if (buttonTwo != null) 1 else 0)
    val showNonDeleteButtons = offsetX.value > deletePointPx
    val minActionWeight = 0.01f

    val nonDeleteVisibility by animateFloatAsState(
        targetValue = if (showNonDeleteButtons) 1f else minActionWeight,
        animationSpec = tween(durationMillis = 180),
        label = "nonDeleteVisibility"
    )

    val deleteWeight by animateFloatAsState(
        targetValue = if (deleteFunction != null) {
            if (showNonDeleteButtons) 1f else (1f + nonDeleteCount.toFloat())
        } else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "deleteWeight"
    )

    LaunchedEffect(editingID) {
        if (editingID != id) offsetX.animateTo(0f, rowSnapSpec)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (editingID == id) {
            BoxWithConstraints(
                modifier = Modifier.matchParentSize().padding(start = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                val revealWidth = max(0f, -offsetX.value - with(density) { 10.dp.toPx() })
                    .coerceAtMost(with(density) { maxWidth.toPx() })
                Box(
                    modifier = Modifier
                        .width(with(density) { revealWidth.toDp() })
                        .alpha(if (offsetX.value < with(density) { -10.dp.toPx() }) 1f else 0f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy((8f * nonDeleteVisibility).dp),
                        modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp)
                    ) {
                        if (deleteFunction != null) {
                            val btnMod = Modifier.weight(nonDeleteVisibility).alpha(nonDeleteVisibility)
                            buttonOne?.let { SkimButtonView(it, offsetX.value, btnMod) }
                            buttonTwo?.let { SkimButtonView(it, offsetX.value, btnMod) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(deleteWeight)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(Color.Red)
                                    .clickable {
                                        deleteFunction.invoke()
                                        onEditingIDChange(null)
                                    },
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
                            buttonOne?.let { SkimButtonView(it, offsetX.value, Modifier.weight(1f)) }
                            buttonTwo?.let { SkimButtonView(it, offsetX.value, Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

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
                        onTap = { if (editingID != id) buttonPressFunction() },
                        onLongPress = { if (editingID != id) buttonPressFunction() }
                    )
                }
                .pointerInput(id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value < deletePointPx -> {
                                        deleteFunction?.invoke()
                                        offsetX.animateTo(0f, rowSnapSpec)
                                        onEditingIDChange(null)
                                    }
                                    offsetX.value < commitPointPx -> offsetX.animateTo(pausePointPx, rowSnapSpec)
                                    else -> {
                                        offsetX.animateTo(0f, rowSnapSpec)
                                        onEditingIDChange(null)
                                    }
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(deletePointPx - 80f, 0f))
                                if (offsetX.value < 0f && editingID != id) onEditingIDChange(id)
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
