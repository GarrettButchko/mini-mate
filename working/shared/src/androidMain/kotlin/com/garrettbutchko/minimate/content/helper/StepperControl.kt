package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StepperControl(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.weight(1.4f)) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = textColor)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1.5f)
        ) {
            Text(
                text = value.toString(),
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
        }
        IconButton(onClick = onIncrement, modifier = Modifier.weight(1.4f)) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = textColor)
        }
    }
}
