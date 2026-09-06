package com.abanobnageh.recipeapp.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * `ConnectivityManager` is a platform service with no usable real implementation off-device, so it
 * is mocked. Every assertion is on the boolean [NetworkInfoImpl.isInternetConnected] actually
 * returns — never on which methods the mocks received — so these tests describe behaviour rather
 * than restating the implementation.
 *
 * The guards being exercised are not decorative. [NetworkInfoImpl] replaced an implementation that
 * probed 8.8.8.8:53 over a raw socket, and [com.abanobnageh.recipeapp.feature_recipes.repositories]
 * short-circuits to a no-internet error without attempting the request whenever this returns false
 * — so a wrong `false` silently disables the whole app.
 */
class NetworkInfoImplTest {

    /** Fails if the capability check is dropped or inverted. */
    @Test
    fun `reports connected when the active network has the internet capability`() = runTest {
        val context = contextWith(capabilitiesHavingInternet(true))

        assertThat(NetworkInfoImpl(context).isInternetConnected()).isTrue()
    }

    /** Fails if the result is hardcoded true, or the capability argument is ignored. */
    @Test
    fun `reports disconnected when the active network lacks the internet capability`() = runTest {
        val context = contextWith(capabilitiesHavingInternet(false))

        assertThat(NetworkInfoImpl(context).isInternetConnected()).isFalse()
    }

    /** Fails if the null guard on activeNetwork is removed — airplane mode would then crash. */
    @Test
    fun `reports disconnected when there is no active network`() = runTest {
        val connectivityManager = mock(ConnectivityManager::class.java)
        `when`(connectivityManager.activeNetwork).thenReturn(null)

        val context = contextWithService(connectivityManager)

        assertThat(NetworkInfoImpl(context).isInternetConnected()).isFalse()
    }

    /**
     * The active network can be torn down between reading the handle and querying it, in which
     * case `getNetworkCapabilities` returns null. Fails if that null guard is removed.
     */
    @Test
    fun `reports disconnected when the active network reports no capabilities`() = runTest {
        val network = mock(Network::class.java)
        val connectivityManager = mock(ConnectivityManager::class.java)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(null)

        val context = contextWithService(connectivityManager)

        assertThat(NetworkInfoImpl(context).isInternetConnected()).isFalse()
    }

    /** Fails if the safe cast on getSystemService is removed. */
    @Test
    fun `reports disconnected when the connectivity service is unavailable`() = runTest {
        val context = contextWithService(null)

        assertThat(NetworkInfoImpl(context).isInternetConnected()).isFalse()
    }

    private fun capabilitiesHavingInternet(hasInternet: Boolean): NetworkCapabilities =
        mock(NetworkCapabilities::class.java).apply {
            `when`(hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(hasInternet)
        }

    private fun contextWith(capabilities: NetworkCapabilities): Context {
        val network = mock(Network::class.java)
        val connectivityManager = mock(ConnectivityManager::class.java)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(capabilities)
        return contextWithService(connectivityManager)
    }

    private fun contextWithService(service: Any?): Context =
        mock(Context::class.java).apply {
            `when`(getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(service)
        }
}
