package com.alternadv.vedhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alternadv.vedhelper.ui.MainAppScreen
import com.alternadv.vedhelper.ui.theme.VedHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VedHelperTheme {
                MainAppScreen()
            }
        }
    }
}