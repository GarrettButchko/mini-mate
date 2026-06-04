package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garrettbutchko.minimate.shared.R

@Composable
fun SectionCard(
    title: String? = null,
    content: @Composable () -> Unit
) {
    val mainOppColor = colorResource(id = R.color.mainOpp)
    val subColor = colorResource(id = R.color.sub)

    Column {
        if (title != null) {
            Text(
                title,
                modifier = Modifier.padding(start = 16.dp),
                fontWeight = FontWeight.Bold,
                color = mainOppColor
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .subtleShadow(),
            shape = RoundedCornerShape(25.dp),
            color = subColor
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}
