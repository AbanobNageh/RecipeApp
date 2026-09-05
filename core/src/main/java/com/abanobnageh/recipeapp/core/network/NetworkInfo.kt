package com.abanobnageh.recipeapp.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

interface NetworkInfo {
    suspend fun isInternetConnected(): Boolean
}

/**
 * Reports connectivity from the platform's own view of the active network.
 *
 * This deliberately does not probe a remote host. A previous implementation opened a raw TCP
 * socket to 8.8.8.8:53, which reported "no internet" on any network that does not route outbound
 * traffic to an external DNS resolver — the Android emulator's user-mode NAT being the common
 * case. Because the repository short-circuits on a negative result, every request failed before
 * it was ever attempted, on a device with working internet.
 */
class NetworkInfoImpl(private val context: Context) : NetworkInfo {
    override suspend fun isInternetConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
