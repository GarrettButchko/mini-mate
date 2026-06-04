package com.garrettbutchko.minimate.content.tabviews.analytics.helperviews

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.garrettbutchko.minimate.content.JetBrainsMono
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.viewModels.analytics.PlayerTier
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

val PlayerTier.composeColor: Color
    get() = when (colorKey) {
        "purple" -> Color(0xFFAF52DE)
        "gray"   -> Color(0xFF8E8E93)
        "yellow" -> Color(0xFFFFCC00)
        "red"    -> Color(0xFFFF3B30)
        else     -> Color.Gray
    }

// ---------------------------------------------------------------------------
// ExpandedPlayerTierContent
// ---------------------------------------------------------------------------

@Composable
fun ExpandedPlayerTierContent(
    tier: PlayerTier,
    emails: List<String>,
    previewEmails: List<String>,
    onCopyToClipboard: () -> Unit,
    csvUri: Uri?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showCopiedCheck by remember { mutableStateOf(false) }

    // Reset the "Copied!" state after 2 seconds
    LaunchedEffect(showCopiedCheck) {
        if (showCopiedCheck) {
            delay(2.seconds)
            showCopiedCheck = false
        }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (showCopiedCheck) Color(0xFF34C759) else MaterialTheme.colorScheme.tertiary,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "copyButtonColor"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email preview list
        if (previewEmails.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Emails (${minOf(5, emails.size)} of ${emails.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .padding(top = 10.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(height = 100.dp, width = androidx.compose.ui.unit.Dp.Unspecified)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    previewEmails.forEach { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy All Emails button
            Button(
                onClick = {
                    onCopyToClipboard()
                    clipboardManager.setText(AnnotatedString(emails.joinToString("\n")))
                    showCopiedCheck = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (showCopiedCheck)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (showCopiedCheck) "Copied!" else "Copy All Emails",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Download CSV / Preparing CSV button
            if (csvUri != null) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, csvUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, "${tier.label} Players CSV")
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Download CSV",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            } else {
                // Disabled placeholder while CSV is being prepared
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Preparing CSV...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Text(
                text = tier.tierDescription,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
