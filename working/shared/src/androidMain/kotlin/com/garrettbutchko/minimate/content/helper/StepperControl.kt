package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

sealed class ButtonColors {
    data class DIFFERENT(val minus: Color, val plus: Color) : ButtonColors()
    data class SAME(val color: Color) : ButtonColors()
    data object DEFAULT : ButtonColors() // Use 'data object' for no-data states
}

@Composable
fun StepperControl(
    modifier: Modifier = Modifier,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    textColor: Color,
    buttonColors: ButtonColors = ButtonColors.DEFAULT
) {
    var minusTint = textColor
    var plusTint = textColor

    when (buttonColors) {
        is ButtonColors.DIFFERENT -> {
            // Automatically smart-cast to 'Different'
            minusTint = buttonColors.minus
           plusTint = buttonColors.plus
        }
        is ButtonColors.SAME -> {
            // Automatically smart-cast to 'Same'
            minusTint = buttonColors.color
            plusTint = buttonColors.color
        }
        ButtonColors.DEFAULT -> {
            // No colors to extract here
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.weight(1.4f)) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = minusTint)
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
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = plusTint)
        }
    }
}
