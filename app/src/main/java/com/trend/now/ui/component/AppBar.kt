package com.trend.now.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    onNavigationClick: (() -> Unit)? = null
) {
    Surface(
        shadowElevation = elevation
    ) {
        TopAppBar(
            modifier = modifier,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                if (onNavigationClick != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }
}