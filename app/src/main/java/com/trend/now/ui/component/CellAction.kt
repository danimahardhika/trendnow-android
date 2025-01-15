package com.trend.now.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CellAction(
    text: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    color: Color = LocalContentColor.current,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(contentPadding)
        ) {
            leadingIcon()
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = text, color = color)
                if (description?.isNotBlank() == true) {
                    Text(text = description, color = color)
                }
            }
            trailingIcon()
        }
    }
}