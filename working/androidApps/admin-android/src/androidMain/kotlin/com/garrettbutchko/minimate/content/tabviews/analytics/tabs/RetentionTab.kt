package com.garrettbutchko.minimate.content.tabviews.analytics.tabs

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.DataCard
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.ExpandedPlayerTierContent
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.composeColor
import com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.skeleton
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel
import com.garrettbutchko.minimate.dataModels.courseModels.CourseEmail
import com.garrettbutchko.minimate.viewModels.analytics.DataPointObject
import com.garrettbutchko.minimate.viewModels.analytics.PlayerTier
import com.garrettbutchko.minimate.viewModels.analytics.RetentionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RetentionTab(
    vm: RetentionViewModel,
    analyticsVM: AnalyticsViewModel,
    modifier: Modifier = Modifier,
) {
    val loadingEmails by analyticsVM.loadingEmails.collectAsState()

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (loadingEmails) {
            RetentionLoadingSkeleton()
        } else {
            // Avg Days Return + 30-Day Retention row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DataCard(
                    data = DataPointObject(
                        value = vm.cachedAvgTimeToReturn.toString(),
                        delta = null,
                        deltaColor = null
                    ),
                    title = "Avg Days, Return",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    cornerRadius = 16.dp
                )
                DataCard(
                    data = DataPointObject(
                        value = "%.0f%%".format(vm.cached30DayRetention * 100),
                        delta = null,
                        deltaColor = null
                    ),
                    title = "30-Day Retention",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    cornerRadius = 16.dp
                )
            }

            // Player Tiers card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Player Tiers",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${vm.allEmails.count()} Unique Players",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // InfoButton omitted — no Android equivalent yet (see open questions)
                        Spacer(modifier = Modifier.size(8.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    PlayerTierCard(tier = PlayerTier.NEW, players = vm.cachedNewPlayers, vm = vm)
                    PlayerTierCard(tier = PlayerTier.MID_TIER, players = vm.cachedMidTierPlayers, vm = vm)
                    PlayerTierCard(tier = PlayerTier.FREQUENT, players = vm.cachedFrequentPlayers, vm = vm)
                    PlayerTierCard(tier = PlayerTier.AT_RISK, players = vm.cachedAtRiskPlayers, vm = vm)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PlayerTierCard
// ---------------------------------------------------------------------------
@Composable
private fun PlayerTierCard(
    tier: PlayerTier,
    players: Map<String, CourseEmail>,
    vm: RetentionViewModel
) {
    val emails = remember(players) { players.keys.sorted() }
    val previewEmails = remember(emails) { emails.take(5) }

    var isExpanded by remember { mutableStateOf(value = false) }
    var csvUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Generate CSV whenever players change and card is expanded
    LaunchedEffect(players, isExpanded) {
        if (isExpanded && players.isNotEmpty()) {
            scope.launch {
                val csvContent = withContext(Dispatchers.IO) {
                    vm.generateCSVContent(emails = emails)
                }
                try {
                    val fileName = "players_${tier.label.lowercase()}.csv"
                    val file = File(context.cacheDir, fileName)
                    file.writeText(csvContent)
                    csvUri = Uri.fromFile(file)
                } catch (_: Exception) { /* silently fail */ }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Header row — tappable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tier icon — SF Symbol names mapped to closest Material icons
                val tierIcon = when (tier.colorKey) {
                    "purple" -> Icons.Default.EmojiEvents
                    "yellow" -> Icons.Default.Star
                    "red"    -> Icons.Default.Warning
                    else     -> Icons.Default.Person
                }
                Icon(
                    imageVector = tierIcon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = tier.composeColor
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = tier.label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = tier.miniDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded content
            AnimatedVisibility(visible = isExpanded && players.isNotEmpty()) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                    ExpandedPlayerTierContent(
                        tier = tier,
                        emails = emails,
                        previewEmails = previewEmails,
                        onCopyToClipboard = { /* clipboard is handled inside ExpandedPlayerTierContent */ },
                        csvUri = csvUri
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Retention loading skeleton — mirrors SwiftUI loadingState / LoadingSkeleton
// ---------------------------------------------------------------------------
@Composable
private fun RetentionLoadingSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .skeleton(active = true),
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {}
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .skeleton(active = true),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {}
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .skeleton(active = true),
                        shape = RoundedCornerShape(17.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {}
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .skeleton(active = true),
                shape = RoundedCornerShape(17.dp),
                color = MaterialTheme.colorScheme.surface
            ) {}
        }
    }
}
