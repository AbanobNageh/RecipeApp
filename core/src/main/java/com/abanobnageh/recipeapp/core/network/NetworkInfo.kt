package com.abanobnageh.recipeapp.core.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

interface NetworkInfo {
    suspend fun internetConnected(): Boolean
}

class NetworkInfoImpl: NetworkInfo {
    override suspend fun internetConnected(): Boolean {
        try {
            val timeoutMs = 1500
            val socket = Socket()
            val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
            socket.connect(socketAddress, timeoutMs)
            socket.close()
            return true
        } catch (e: IOException) {
            return false
        }
    }
}