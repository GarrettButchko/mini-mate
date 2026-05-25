package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter

/**
 * Scroll-wheel number picker that mimics iOS's UIPickerView feel.
 *
 * Shows three items at once (prev / selected / next). The centre item is
 * full-opacity and bold; adjacent items are faded to 30 %. Two hairline
 * dividers frame the selected row.
 *
 * Default itemHeight = 20 dp → total height = 60 dp, matching the
 * scorecard grid row height exactly.
 */
@Composable
fun WheelNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = 10,
    textColor: Color = Color.Unspecified,
    itemHeight: Dp = 20.dp,
) {
    // Pad with one duplicate at each end so min/max can be centred.
    val paddedValues = remember(min, max) {
        listOf(min) + (min..max).toList() + listOf(max)
    }

    // firstVisibleItemIndex == (value - min) → centres `value`.
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (value - min).coerceIn(0, max - min)
    )
    val snapFling = rememberSnapFlingBehavior(listState)

    // Reactively tracked: the padded-list index of the centred item.
    val centeredIdx by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }

    // Report the settled value upward each time scrolling stops.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect {
                val newValue = (listState.firstVisibleItemIndex + min).coerceIn(min, max)
                onValueChange(newValue)
            }
    }

    // Scroll to value when it changes externally.
    LaunchedEffect(value) {
        val target = (value - min).coerceIn(0, max - min)
        if (listState.firstVisibleItemIndex != target) {
            listState.animateScrollToItem(target)
        }
    }

    Box(modifier = modifier.height(itemHeight * 3)) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFling,
            modifier = Modifier.matchParentSize(),
            contentPadding = PaddingValues(0.dp),
            userScrollEnabled = true,
        ) {
            itemsIndexed(paddedValues) { idx, itemValue ->
                val isCentered = idx == centeredIdx
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = if (isCentered) 1f else 0.3f },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = itemValue.toString(),
                        fontSize = if (isCentered) 15.sp else 11.sp,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Hairline dividers framing the selected row.
        Column(modifier = Modifier.matchParentSize()) {
            Spacer(modifier = Modifier.height(itemHeight))
            HorizontalDivider(thickness = 0.5.dp, color = textColor.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(itemHeight - 0.5.dp))
            HorizontalDivider(thickness = 0.5.dp, color = textColor.copy(alpha = 0.25f))
        }
    }
}
