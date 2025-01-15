package com.trend.now.ui.feature.news

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.trend.now.R
import com.trend.now.core.util.isListAtTop
import com.trend.now.ui.feature.news.component.NewsCard
import com.trend.now.ui.feature.news.localnews.LocalNewsSection
import com.trend.now.ui.feature.news.topic.TopicSection
import com.trend.now.ui.feature.news.topic.TopicsViewModel
import com.trend.now.ui.navigation.AppRoute
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    topicsViewModel: TopicsViewModel = hiltViewModel(),
    newsViewModel: NewsViewModel = hiltViewModel()
) {
    val newsListState = rememberLazyListState()
    val appBarElevation by remember {
        // top app bar elevation logic
        // show elevation when user start scrolling the news list
        derivedStateOf { if (newsListState.isListAtTop()) 0.dp else 8.dp }
    }
    val showFab by remember {
        // fab visibility logic to scroll to the top of of news list
        derivedStateOf { newsListState.firstVisibleItemIndex > 1 }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // collect the trending news ui state state flow
    val trendingNewsUiState by newsViewModel.trendingNewsUiState.collectAsState()
    // collect the selected topic state flow
    val selectedTopic by newsViewModel.selectedTopic.collectAsState()
    // collect the news preference state flow
    val localNewsUiState by newsViewModel.localNewsUiState.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { newsListState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: -1
                val totalItems = newsListState.layoutInfo.totalItemsCount
                Pair(lastVisibleItemIndex, totalItems)
            }
            .collect { (lastVisibleItemIndex, totalItems) ->
                if (trendingNewsUiState.data.isEmpty()) return@collect

                if (lastVisibleItemIndex >= totalItems - 1) {
                    // load more trending news user reached at the bottom of the list
                    newsViewModel.loadMoreTrendingNews()
                }
            }
    }

    LaunchedEffect(trendingNewsUiState) {
        // success is false and news data is not empty
        // that means the error comes from the load more request
        if (!trendingNewsUiState.success && trendingNewsUiState.data.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.unable_to_load_more_news)
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = appBarElevation,
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Black
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                val coroutineScope = rememberCoroutineScope()
                SmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            newsListState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowUp, contentDescription = null)
                }
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = trendingNewsUiState.refreshing,
            onRefresh = {
                topicsViewModel.fetchTopics()
                newsViewModel.onPullToRefresh()
            },
            modifier = modifier.padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.animateContentSize(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 16.dp),
                state = newsListState,
            ) {
                item(key = "local-news") {
                    LocalNewsSection(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(horizontal = 16.dp)
                            .animateItem(),
                        localNewsUiState = localNewsUiState
                    ) {
                        navController.navigate(route = AppRoute.localNewsSettings)
                    }
                }
                item(key = "topics-section") {
                    TopicSection(
                        modifier = Modifier.fillParentMaxWidth().animateContentSize(),
                        selectedTopic = selectedTopic,
                        viewModel = topicsViewModel
                    )
                }
                if (trendingNewsUiState.loading) {
                    item(key = "loading") {
                        Loading(modifier = Modifier.fillParentMaxSize())
                    }
                }
                if (trendingNewsUiState.success || trendingNewsUiState.data.isNotEmpty()) {
                    items(items = trendingNewsUiState.data, key = { it.url.hashCode() }) { news ->
                        NewsCard(
                            modifier = Modifier.fillParentMaxWidth(),
                            news = news
                        ) {
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                            customTabsIntent.launchUrl(context, Uri.parse(news.url))
                        }
                    }
                    if (trendingNewsUiState.showLoadMore) {
                        item(key = "loading-more") {
                            Loading(modifier = Modifier.fillParentMaxWidth())
                        }
                    }
                } else if  (!trendingNewsUiState.loading) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillParentMaxSize()
                        ) {
                            Text(
                                text = trendingNewsUiState.message.takeIf { it.isNotBlank() } ?: run {
                                    stringResource(R.string.unable_to_load_news)
                                }
                            )
                            Button(onClick = { newsViewModel.fetchTrendingNews() }) {
                                Text(text = stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}