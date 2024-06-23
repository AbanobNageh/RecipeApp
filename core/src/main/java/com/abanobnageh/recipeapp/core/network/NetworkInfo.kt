package com.abanobnageh.recipeapp.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

interface NetworkInfo {
    suspend fun isInternetConnected(): Boolean
}

class NetworkInfoImpl: NetworkInfo {
    override suspend fun isInternetConnected(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val timeoutMs = 1500
            val socket = Socket()
            val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, timeoutMs)
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}