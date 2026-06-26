package com.garrettbutchko.minimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.garrettbutchko.minimate.content.App
import com.garrettbutchko.minimate.di.initKoin
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.maps.MapsInitializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        
        if (GlobalContext.getOrNull() == null) {
            initKoin {
                androidContext(applicationContext)
                androidLogger()
            }
        }
        NetworkChecker.initialize(applicationContext)
        MobileAds.initialize(this) {}
        MapsInitializer.initialize(applicationContext)

        setContent {
            App()
        }
    }
}
