package com.garrettbutchko.minimate.content.helper

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.garrettbutchko.minimate.content.helper.modifiers.subVsColor
import com.garrettbutchko.minimate.content.helper.modifiers.subtleShadow
import com.garrettbutchko.minimate.di.KoinHelper
import com.garrettbutchko.minimate.logic.ScoreCardBusinessLogic
import com.garrettbutchko.minimate.modelKotlinExtensions.scoreCardColor
import com.garrettbutchko.minimate.shared.R


@Composable
fun rememberCourseBannerAdVisible(
    isGuest: Boolean,
    isPro: Boolean?,
    isConnected: Boolean
): Boolean {
    val logic = remember { ScoreCardBusinessLogic() }
    return remember(isGuest, isPro, isConnected) {
        logic.shouldShowAds(isConnected, isGuest, isPro)
    }
}

@Composable
fun CourseBannerAdView(
    adUnitID: String,
    isGuest: Boolean,
    isPro: Boolean?,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
    onBannerAdLoaded: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val course = remember { KoinHelper.getGameViewModel() }.course.collectAsState().value
    val logic = remember { ScoreCardBusinessLogic() }
    val primaryColor = colorResource(R.color.mainOpp)
    val secondaryColor = colorResource(R.color.subTwoOpp)

    val bgColor = Color.subVsColor(makeColor = course?.scoreCardColor)
    val styledMod = if (course?.scoreCardColor != null) {
        modifier.background(bgColor, RoundedCornerShape(25.dp)).padding(16.dp)
    } else {
        modifier.subtleShadow().background(bgColor, RoundedCornerShape(25.dp)).padding(16.dp)
    }

    when {
        course?.customAdActive == true -> Surface(
            onClick = {
                course.adLink?.takeIf { it.isNotBlank() }?.let {
                    context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri()))
                }
            },
            modifier = styledMod.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    course.adTitle?.let { Text(it, fontWeight = FontWeight.SemiBold, color = primaryColor) }
                    course.adDescription?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = secondaryColor) }
                }
                course.adImage?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.width(16.dp))
                    AsyncImage(model = it, contentDescription = "Ad", modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)))
                }
                Spacer(Modifier.width(16.dp))
            }
        }

        logic.shouldShowAds(isConnected, isGuest, isPro) -> {
            BannerAdView(
                adUnitID = adUnitID,
                modifier = styledMod.fillMaxWidth(),
                onAdLoaded = { onBannerAdLoaded(it) }
            )
        }
    }
}
