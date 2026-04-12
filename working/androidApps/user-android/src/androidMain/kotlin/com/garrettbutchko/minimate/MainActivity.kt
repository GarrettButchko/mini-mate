package com.garrettbutchko.minimate

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.garrettbutchko.minimate.content.App
import com.garrettbutchko.minimate.di.initKoin
import com.garrettbutchko.minimate.utilities.NetworkChecker
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.maps.MapsInitializer
import com.google.android.libraries.places.api.Places
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (GlobalContext.getOrNull() == null) {
            initKoin {
                androidContext(applicationContext)
                androidLogger()
            }
        }
        NetworkChecker.initialize(applicationContext)
        MobileAds.initialize(this) {}
        MapsInitializer.initialize(applicationContext)
        
        val apiKey = try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
        
        if (apiKey != null && !Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        setContent {
            App()
        }
    }
}
