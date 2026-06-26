package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.garrettbutchko.minimate.content.helper.InfoButton
import com.garrettbutchko.minimate.content.helper.SectionStatsCard
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import com.garrettbutchko.minimate.content.tabviews.analytics.AnalyticsLoadingSkeleton
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.ExpandedPlayerTierContent
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.composeColor
import com.garrettbutchko.minimate.dataModels.courseModels.CourseEmail
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.viewModels.analytics.PlayerTier
import java.io.File

@Composable
internal fun RetentionView(vm: AnalyticsViewModel) {
    val retVM = vm.retentionVM
    val loadingEmails by vm.loadingEmails.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedContent(
            targetState = loadingEmails,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "retentionLoadingTransition"
        ) { isLoading ->
            if (isLoading) {
                AnalyticsLoadingSkeleton()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DataCard(
                            data = retVM.getAvgTimeToReturn(),
                            cornerRadius = 16.dp,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f).subtleShadow()
                        )
                        DataCard(
                            data = retVM.get30DayRetention(),
                            cornerRadius = 16.dp,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f).subtleShadow()
                        )
                    }

                    SectionStatsCard(title = "Player Tiers") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${retVM.allEmails.size} Unique Players",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            InfoButton(infoText = "Players are categorized into tiers based on their activity level and return behavior. This helps identify patterns and opportunities for engagement. Click on the arrow to get their emails and take action!")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PlayerTierCard(tier = PlayerTier.NEW, players = retVM.cachedNewPlayers, vm = vm)
                            PlayerTierCard(tier = PlayerTier.MID_TIER, players = retVM.cachedMidTierPlayers, vm = vm)
                            PlayerTierCard(tier = PlayerTier.FREQUENT, players = retVM.cachedFrequentPlayers, vm = vm)
                            PlayerTierCard(tier = PlayerTier.AT_RISK, players = retVM.cachedAtRiskPlayers, vm = vm)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerTierCard(
    tier: PlayerTier,
    players: Map<String, CourseEmail>,
    vm: AnalyticsViewModel
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isExpanded by remember { mutableStateOf(false) }
    var csvUri by remember { mutableStateOf<Uri?>(null) }

    val emails = remember(players) { players.keys.sorted() }
    val previewEmails = remember(emails) { emails.take(5) }

    LaunchedEffect(isExpanded, players) {
        if (isExpanded && emails.isNotEmpty()) {
            csvUri = generateCsvUri(context, vm.retentionVM.generateCSVContent(emails), tier.label)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondary
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = tierIcon(tier),
                    contentDescription = null,
                    tint = tier.composeColor,
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tier.label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = tier.miniDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${players.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "players",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded && players.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    )
                    ExpandedPlayerTierContent(
                        tier = tier,
                        emails = emails,
                        previewEmails = previewEmails,
                        onCopyToClipboard = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                        csvUri = csvUri
                    )
                }
            }
        }
    }
}

private fun tierIcon(tier: PlayerTier): ImageVector = when (tier) {
    PlayerTier.NEW      -> Icons.Default.Star
    PlayerTier.MID_TIER -> Icons.Default.MilitaryTech
    PlayerTier.FREQUENT -> Icons.Default.WorkspacePremium
    PlayerTier.AT_RISK  -> Icons.Default.Warning
}

private fun generateCsvUri(context: Context, csvContent: String, tierLabel: String): Uri? {
    return try {
        val fileName = "players_${tierLabel.lowercase()}.csv"
        val cacheDir = File(context.cacheDir, "csv").also { it.mkdirs() }
        val file = File(cacheDir, fileName)
        file.writeText(csvContent)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (_: Exception) {
        null
    }
}
