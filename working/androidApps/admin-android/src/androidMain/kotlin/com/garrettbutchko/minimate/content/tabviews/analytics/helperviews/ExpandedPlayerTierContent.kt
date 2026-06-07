package com.garrettbutchko.minimate.content.tabviews.analytics.helperviews

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.modelKotlinExtensions.toColor
import com.garrettbutchko.minimate.viewModels.analytics.PlayerTier
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

// Extension property that maps PlayerTier.colorKey to a Compose Color.
// Mirrors the iOS PlayerTier+UI.swift `color` computed property.
val PlayerTier.composeColor: Color
    @Composable get() = colorKey.toColor() ?: MaterialTheme.colorScheme.onSurface

@Composable
fun ExpandedPlayerTierContent(
    tier: PlayerTier,
    emails: List<String>,
    previewEmails: List<String>,
    onCopyToClipboard: () -> Unit = {},
    csvUri: Uri?
) {
    val context = LocalContext.current
    var showCopied by remember { mutableStateOf(false) }

    // Reset the "Copied!" state after 2 seconds, mirroring iOS asyncAfter behavior
    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(2_000.milliseconds)
            showCopied = false
        }
    }

    val copyButtonColor by animateColorAsState(
        targetValue = if (showCopied) Color(0xFF34C759) else colorResource(id = com.garrettbutchko.minimate.shared.R.color.subThreeOpp),
        animationSpec = spring(),
        label = "copyButtonColor"
    )

    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email preview list
        if (previewEmails.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Emails (${minOf(5, emails.size)} of ${emails.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    previewEmails.forEach { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Action buttons
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {

            val butCornRad = 10.dp
            val contentPadV = 1.dp
            val contentPadH = 12.dp
            val iconSize = 12.dp

            // Copy all emails to clipboard
            Button(
                onClick = {
                    copyEmailsToClipboard(context, emails)
                    onCopyToClipboard()
                    showCopied = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(butCornRad),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = contentPadH, vertical = contentPadV),
                colors = ButtonDefaults.buttonColors(containerColor = copyButtonColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (showCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize)
                    )
                    Text(
                        text = if (showCopied) "Copied!" else "Copy All Emails",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // CSV share / loading placeholder — mirrors iOS ShareLink + LogoLoadingView
            if (csvUri != null) {
                Button(
                    onClick = { shareCsvUri(context, csvUri, tier.label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(butCornRad),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = contentPadH, vertical = contentPadV),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.subThreeOpp)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        Text(
                            text = "Download CSV",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            } else {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(butCornRad),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = contentPadH, vertical = contentPadV),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = com.garrettbutchko.minimate.shared.R.color.subThreeOpp)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Preparing CSV...",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Tier description — mirrors iOS Text(tier.description)
            Text(
                text = tier.tierDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun copyEmailsToClipboard(context: Context, emails: List<String>) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    val clip = ClipData.newPlainText("player emails", emails.joinToString("\n"))
    clipboard.setPrimaryClip(clip)
}

private fun shareCsvUri(context: Context, uri: Uri, tierLabel: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "$tierLabel Players CSV")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share CSV"))
}
