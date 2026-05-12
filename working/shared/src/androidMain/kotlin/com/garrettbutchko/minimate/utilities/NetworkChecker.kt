package com.garrettbutchko.minimate.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import co.touchlab.kermit.Logger

actual class NetworkChecker private constructor(context: Context) {

    actual var isConnected: Boolean = false
        private set

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
                Logger.d("✅ Internet is available")
            }

            override fun onLost(network: Network) {
                isConnected = false
                Logger.d("❌ No internet")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        })

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        isConnected = capabilities?.let {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false
    }

    actual companion object {
        private var instance: NetworkChecker? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = NetworkChecker(context.applicationContext)
            }
        }

        actual val shared: NetworkChecker
            get() = instance ?: error("NetworkChecker must be initialized by calling NetworkChecker.initialize(context) before use.")
    }
}
