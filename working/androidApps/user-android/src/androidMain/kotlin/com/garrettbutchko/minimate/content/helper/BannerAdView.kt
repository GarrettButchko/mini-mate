package com.garrettbutchko.minimate.content.helper

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    adUnitID: String,
    modifier: Modifier = Modifier,
    onAdLoaded: (Boolean) -> Unit = {}
) {
    var adLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val adView = remember { AdView(context).apply { setAdSize(AdSize.BANNER); this.adUnitId = adUnitID } }

    DisposableEffect(adView, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() { adLoaded = true; onAdLoaded(true) }
            override fun onAdFailedToLoad(error: LoadAdError) { adLoaded = false; onAdLoaded(false) }
        }
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    if (adLoaded) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            AndroidView(factory = { adView })
        }
    }
}
