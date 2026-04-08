package com.garrettbutchko.minimate.content.helper

import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NumberPickerView(
    selectedNumber: Int,
    onSelectedNumberChange: (Int) -> Unit,
    maxNumber: Int,
    modifier: Modifier = Modifier,
    minNumber: Int? = null
) {
    val actualMin = minNumber ?: 0

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(95.dp),
        factory = { context ->
            NumberPicker(context).apply {
                minValue = actualMin
                maxValue = maxNumber
                value = selectedNumber
                setOnValueChangedListener { _, _, newVal ->
                    onSelectedNumberChange(newVal)
                }
            }
        },
        update = { view ->
            if (view.minValue != actualMin) view.minValue = actualMin
            if (view.maxValue != maxNumber) view.maxValue = maxNumber
            if (view.value != selectedNumber) view.value = selectedNumber
        }
    )
}
