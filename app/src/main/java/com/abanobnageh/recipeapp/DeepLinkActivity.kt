package com.abanobnageh.recipeapp

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeepLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeeplink(intent)
        setContent {}
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeeplink(intent)
    }

    private fun handleDeeplink(intent: Intent?) {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent?.data?.let { mainActivityIntent.data = it }
        startActivity(mainActivityIntent)
        finish()
    }
}