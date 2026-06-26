package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPickerView(
    selectedNumber: Int,
    onSelectedNumberChange: (Int) -> Unit,
    maxNumber: Int,
    modifier: Modifier = Modifier,
    minNumber: Int? = null
) {
    val actualMin = minNumber ?: 0
    val numbers = remember(actualMin, maxNumber) { (actualMin..maxNumber).toList() }
    val itemHeight = 36.dp

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedNumber - actualMin).coerceIn(0, numbers.lastIndex)
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Report value when scroll settles
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val value = (actualMin + listState.firstVisibleItemIndex).coerceIn(actualMin, maxNumber)
            if (value != selectedNumber) onSelectedNumberChange(value)
        }
    }

    // Sync programmatic changes
    LaunchedEffect(selectedNumber) {
        val target = (selectedNumber - actualMin).coerceIn(0, numbers.lastIndex)
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != target) {
            listState.scrollToItem(target)
        }
    }

    val fadeColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier.fillMaxWidth().height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            items(numbers.size) { index ->

                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numbers[index].toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 1f
                        )
                    )
                }
            }
        }

        // Selection indicator pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(itemHeight)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )

        // Fade overlay matching host background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to fadeColor,
                        0.25f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1f to fadeColor
                    )
                )
        )
    }
}
