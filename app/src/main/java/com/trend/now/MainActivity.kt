package com.trend.now

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trend.now.core.ui.theme.TrendNowTheme
import com.trend.now.ui.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

abstract class BaseActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
}

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrendNowTheme {
                AppNavigation()
            }
        }
    }
}