package com.trend.now.ui.feature.news.localnews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.trend.now.R
import com.trend.now.core.util.darken
import com.trend.now.ui.navigation.AppRoute
import java.util.Locale

@Composable
fun LocalNewsSection(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: LocalNewsViewModel
) {
    // collect local news preference
    val newsPreference by viewModel.newsPreference.collectAsState()

    if (newsPreference.isNotBlank()) {
        LocalNewsCard(
            modifier = modifier,
            locale = Locale(newsPreference.language, newsPreference.country)
        ) {
            navController.navigate(route = AppRoute.LocalNewsSettings.path)
        }
    } else {
        // workaround to mark this section as visible in LazyColumn when loading
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
private fun LocalNewsCard(
    modifier: Modifier = Modifier,
    locale: Locale,
    expandedByDefault: Boolean = false,
    onSettingsClick: () -> Unit = {}
) {
    var expanded by rememberSaveable { mutableStateOf(expandedByDefault) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "expandable-arrow"
    )

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.darken(0.7f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column {
            // expandable header
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        expanded = !expanded
                    },
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            // expandable contents
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween()
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween()
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp,)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    IconButton(onClick = onSettingsClick) {
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
}

@Preview
@Composable
private fun LocalNewsCardCollapsedPreview() {
    LocalNewsCard(locale = Locale.US)
}

@Preview
@Composable
private fun LocalNewsCardExpandedPreview() {
    LocalNewsCard(locale = Locale.US, expandedByDefault = true)
}