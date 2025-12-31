package com.godaplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.godaplayer.app.ui.navigation.GodaPlayerNavHost
import com.godaplayer.app.ui.theme.GodaPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GodaPlayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GodaPlayerNavHost()
                }
            }
        }
    }
}
