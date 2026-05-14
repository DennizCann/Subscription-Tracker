package com.denizcan.substracktion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.denizcan.substracktion.ui.navigation.SubstracktionNavHost
import com.denizcan.substracktion.ui.theme.SubstracktionTheme

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
