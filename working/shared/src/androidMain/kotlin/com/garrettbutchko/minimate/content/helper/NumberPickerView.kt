package com.garrettbutchko.minimate.content.helper

import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.garrettbutchko.minimate.shared.R

@RequiresApi(Build.VERSION_CODES.Q)
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
            val themedContext = ContextThemeWrapper(context, R.style.NumberPickerStyle)
            NumberPicker(themedContext).apply {
                minValue = actualMin
                maxValue = maxNumber
                value = selectedNumber
                setOnValueChangedListener { _, _, newVal ->
                    onSelectedNumberChange(newVal)
                }

                // Set the text color for the NumberPicker
                val color = ContextCompat.getColor(themedContext, R.color.mainOpp)
                @Suppress("DEPRECATION")
                this.textColor = color

                // This is a workaround for older API levels where textColor might not work directly
                // by iterating through the children of the NumberPicker and setting the text color
                // for any EditText found.
                for (i in 0 until this.childCount) {
                    val child = this.getChildAt(i)
                    if (child is android.widget.EditText) {
                        child.setTextColor(color)
                    }
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
