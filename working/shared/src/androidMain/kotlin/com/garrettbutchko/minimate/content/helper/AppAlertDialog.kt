package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.shared.R

/**
 * App-wide alert dialog, styled consistently across Mini Manager and Mini Mate.
 *
 * Replaces the ad-hoc `AlertDialog` usages so every native dialog shares the
 * same look: a `mainOpp` title/body, a `swiftBlue` primary action, and a
 * neutral `mainOpp` cancel action (destructive confirms render in `swiftRed`).
 *
 * @param onDismissRequest invoked when the dialog is dismissed (back press / outside tap)
 * @param title dialog title
 * @param confirmText label for the primary button; pass null to hide it
 * @param onConfirm invoked when the primary button is tapped
 * @param message optional body copy shown above [content]
 * @param dismissText label for the secondary button; pass null to hide it
 * @param onDismiss invoked when the secondary button is tapped (defaults to [onDismissRequest])
 * @param confirmEnabled whether the primary button is tappable
 * @param destructive render the primary button in `swiftRed` instead of `swiftBlue`
 * @param content optional extra content (e.g. text fields) placed under [message]
 */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String? = null,
    onConfirm: () -> Unit = {},
    message: String? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = onDismissRequest,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    val mainOpp = colorResource(id = R.color.mainOpp)
    val swiftBlue = colorResource(id = R.color.swiftBlue)
    val swiftRed = colorResource(id = R.color.swiftRed)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, color = mainOpp) },
        text = {
            if (message != null || content != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (message != null) {
                        Text(message, color = mainOpp)
                    }
                    content?.invoke(this)
                }
            }
        },
        confirmButton = {
            if (confirmText != null) {
                Button(
                    onClick = onConfirm,
                    enabled = confirmEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (destructive) swiftRed else swiftBlue, // Custom background color
                        contentColor = Color.White         // Text / icon color
                    )) {
                    Text(confirmText)
                }
            }
        },
        dismissButton = {
            if (dismissText != null) {
                TextButton(onClick = onDismiss) {
                    Text(dismissText, color = swiftBlue)
                }
            }
        }
    )
}
