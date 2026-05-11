package com.example.substracktion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.substracktion.ui.navigation.SubstracktionNavHost
import com.example.substracktion.ui.theme.SubstracktionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubstracktionTheme {
                SubstracktionNavHost()
            }
        }
    }
}
