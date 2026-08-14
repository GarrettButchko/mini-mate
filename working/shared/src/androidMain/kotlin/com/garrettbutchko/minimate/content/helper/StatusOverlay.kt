package com.garrettbutchko.minimate.content.helper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The animation MiniMate's transient popups (ColorPickerView, SuccessOverlay,
 * LoadingOverlay) show and dismiss with. Reuse this instead of inventing a new
 * curve so every popup enters and exits with the same feel.
 *
 * Matches the iOS `statusOverlaySpring` — .spring(response: 0.35, dampingFraction: 0.8).
 */
val statusOverlaySpring = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMediumLow
)

/** How long a transient overlay stays up before auto-dismissing. */
val statusOverlayDuration: Duration = 1500.milliseconds

/** How long to keep an overlay's window alive to play its exit animation. */
val statusOverlayExitDuration: Duration = 400.milliseconds

/**
 * A one-shot message shown in a [SuccessOverlay]. Also used as the payload of
 * [TransientStatusSignal] for messages posted by a screen right before it
 * navigates away (e.g. logging out lands on SignInView, which has no other way
 * to know why it's being shown).
 */
data class StatusMessage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector = Icons.Filled.CheckCircle,
    val tint: Color? = null
)

/**
 * Process-wide handoff slot for a [StatusMessage] that outlives the screen that
 * posted it. Mirrors the iOS `TransientStatusSignal` singleton.
 */
object TransientStatusSignal {
    var pending: StatusMessage? = null
}

/**
 * Drives the show / hold / auto-dismiss cycle shared by every transient overlay.
 * Call [show] to post a message; it clears itself after [statusOverlayDuration]
 * unless a newer message replaced it meanwhile.
 */
@Stable
class TransientStatusState internal constructor(
    private val state: MutableState<StatusMessage?>
) {
    val message: StatusMessage? get() = state.value

    fun show(message: StatusMessage) {
        state.value = message
    }

    fun clear() {
        state.value = null
    }

    internal fun clearIfStill(message: StatusMessage) {
        if (state.value == message) state.value = null
    }
}

/**
 * Remembers a [TransientStatusState] and runs its auto-dismiss timer. Render the
 * result with [TransientStatusOverlay] (or read `.message` directly when the
 * message needs to appear somewhere other than a full-screen overlay).
 */
@Composable
fun rememberTransientStatus(): TransientStatusState {
    val state = remember { mutableStateOf<StatusMessage?>(null) }
    val holder = remember { TransientStatusState(state) }
    val current = state.value
    LaunchedEffect(current) {
        if (current != null) {
            delay(statusOverlayDuration)
            holder.clearIfStill(current)
        }
    }
    return holder
}

/**
 * Renders [state]'s message as a full-screen [SuccessOverlay] with the standard
 * animation. Pass [visible] to suppress it while another popup owns the screen
 * and is showing the message itself.
 */
@Composable
fun TransientStatusOverlay(
    state: TransientStatusState,
    visible: Boolean = state.message != null
) {
    // Retain the last message so the text stays put during the exit animation
    var lastMessage by remember { mutableStateOf<StatusMessage?>(null) }
    state.message?.let { lastMessage = it }

    StatusOverlayScaffold(visible = visible) {
        lastMessage?.let { message ->
            SuccessOverlayCard(
                title = message.title,
                subtitle = message.subtitle,
                icon = message.icon,
                tint = message.tint
            )
        }
    }
}

/**
 * Consumes a message posted through [TransientStatusSignal] once, on first
 * composition. Attach to any screen that can be the landing point of a signal
 * (e.g. SignInView after logout / account deletion).
 */
@Composable
fun ConsumeTransientStatusSignal() {
    val status = rememberTransientStatus()
    LaunchedEffect(Unit) {
        TransientStatusSignal.pending?.let { pending ->
            TransientStatusSignal.pending = null
            status.show(pending)
        }
    }
    TransientStatusOverlay(status)
}

/**
 * Success / notice popup. Styled to match the app's other popups (ColorPickerView
 * et al.) so transient status moments read as one consistent design language.
 */
@Composable
fun SuccessOverlay(
    visible: Boolean,
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Filled.CheckCircle,
    tint: Color? = null
) {
    StatusOverlayScaffold(visible = visible) {
        SuccessOverlayCard(title = title, subtitle = subtitle, icon = icon, tint = tint)
    }
}

/** Blocking "working on it" popup, matching [SuccessOverlay]'s card. */
@Composable
fun LoadingOverlay(
    visible: Boolean,
    message: String
) {
    StatusOverlayScaffold(visible = visible) {
        PopupCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LogoLoadingView()
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SuccessOverlayCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color?
) {
    PopupCard {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Backdrop + scale/fade transition shared by every popup in the app. No dim is
 * applied — the card floats over the screen with a tap-swallowing, transparent
 * scrim so the content underneath stays inert.
 *
 * Rendered in its own [Dialog] window so it sits above everything else on screen
 * — including Material dialogs and bottom sheets that otherwise paint on top of
 * an inline overlay. The window is kept in the composition through the exit
 * animation and then removed.
 */
@Composable
fun StatusOverlayScaffold(
    visible: Boolean,
    dismissOnScrimTap: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var rendered by remember { mutableStateOf(visible) }
    var shown by remember { mutableStateOf(visible) }

    LaunchedEffect(visible) {
        if (visible) {
            rendered = true
            shown = true
        } else if (rendered) {
            shown = false
            delay(statusOverlayExitDuration)
            rendered = false
        }
    }

    if (!rendered) return

    val interactionSource = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = { dismissOnScrimTap?.invoke() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { dismissOnScrimTap?.invoke() }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = shown,
                enter = fadeIn(statusOverlaySpring) + scaleIn(statusOverlaySpring, initialScale = 0.9f),
                exit = fadeOut(statusOverlaySpring) + scaleOut(statusOverlaySpring, targetScale = 0.9f)
            ) {
                content()
            }
        }
    }
}

/**
 * The popup surface itself: same container color, shape and elevation Material
 * gives the app's AlertDialogs (see ColorPickerView), so hand-rolled popups and
 * system dialogs are indistinguishable.
 */
@Composable
fun PopupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .widthIn(max = 400.dp),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        Box(modifier = Modifier.padding(24.dp)) {
            content()
        }
    }
}
