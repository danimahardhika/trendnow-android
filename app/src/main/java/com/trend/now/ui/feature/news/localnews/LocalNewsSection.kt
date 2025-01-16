package com.trend.now.ui.feature.news.localnews

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.trend.now.R
import com.trend.now.core.ui.state.UiState
import com.trend.now.core.util.darken
import com.trend.now.ui.navigation.AppRoute
import java.util.Locale

@Composable
fun LocalNewsSection(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: LocalNewsViewModel
) {
    // collect local news ui state
    val uiState by viewModel.uiState.collectAsState()

    val state = uiState
    if (state is UiState.Success) {
        val expanded = rememberSaveable { mutableStateOf(false) }
        val arrowRotation by animateFloatAsState(
            targetValue = if (expanded.value) 180f else 0f,
            label = "dropdown-arrow"
        )
        val locale = Locale(state.data.language, state.data.country)

        Column(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                // add animate content size
                // to show animation when expanded state is changed
                .animateContentSize()
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .zIndex(1f)
                    .clickable {
                        expanded.value = !expanded.value
                    },
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.local_news_info),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = locale.displayCountry,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        modifier = Modifier
                            .rotate(arrowRotation)
                            .padding(horizontal = 20.dp),
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            if (expanded.value) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(0f)
                        .offset(y = (-12).dp),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                    color = MaterialTheme.colorScheme.primary.darken(0.75f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = 8.dp,
                                top = 20.dp,
                                bottom = 8.dp
                            )
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = locale.displayLanguage,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate(route = AppRoute.LOCAL_NEWS_SETTINGS)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    } else {
        // workaround to mark this section as visible in LazyColumn when loading
        Spacer(modifier = Modifier.height(1.dp))
    }
}