package com.garrettbutchko.minimate.content.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    makeColor: Color? = null,
    cornerRadius: Dp = 12.dp,
    cardHeight: Dp? = null,
    infoText: String = "No Text Yet"
) {
    var showInfo by remember { mutableStateOf(false) }

    val valueStyle = if (color != null) {
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = color
        )
    } else {
        MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1E88E5), Color(0xFF43A047))
            )
        )
    }

    Card(
        modifier = if (cardHeight != null) modifier.height(cardHeight) else modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = makeColor ?: MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showInfo = true }) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Show stat info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row {
                Text(text = value, style = valueStyle)
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = {
                Button(onClick = { showInfo = false }) {
                    Text("OK")
                }
            },
            title = { Text("Info") },
            text = { Text(infoText) }
        )
    }
}

