package com.garrettbutchko.minimate.content.helper

import co.touchlab.kermit.Logger

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * A Jetpack Compose implementation of InterstitialAdView.
 * 
 * Unlike SwiftUI where we need a transparent UIViewControllerRepresentable,
 * in Compose we can simply use a side effect (LaunchedEffect) to load and show
 * the Activity-level interstitial overlay without polluting the UI hierarchy.
 */
@Composable
fun InterstitialAdView(
    adUnitID: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(adUnitID) {
        if (activity == null) {
            Logger.i("Error: InterstitialAdView requires an Activity context.")
            onDismiss()
            return@LaunchedEffect
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Logger.i("Failed to load interstitial ad: ${adError.message}")
                    onDismiss()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onDismiss()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Logger.i("Ad failed to present with error: ${adError.message}")
                            onDismiss()
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Ad successfully showed
                        }
                    }
                    
                    // Show the ad over the current activity
                    interstitialAd.show(activity)
                }
            }
        )
    }
}
