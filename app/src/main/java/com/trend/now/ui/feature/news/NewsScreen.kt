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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.trend.now.R
import com.trend.now.core.util.isListAtTop
import com.trend.now.core.util.paging.PagingEvent
import com.trend.now.ui.feature.news.component.NewsCard
import com.trend.now.ui.feature.news.localnews.LocalNewsSection
import com.trend.now.ui.feature.news.localnews.LocalNewsViewModel
import com.trend.now.ui.feature.news.topic.TopicSection
import com.trend.now.ui.feature.news.topic.TopicsViewModel
import com.trend.now.ui.navigation.AppRoute
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    // the same reason with topics viewmodel
    localNewsViewModel: LocalNewsViewModel = hiltViewModel(),
    // let the news screen manage the topics viewmodel creation
    // so that the topics section using the same viewmodel instance
    // when the topics section re-created from scrolling
    // (from invisible to visible on the screen)
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

    // collect the news paging flow
    val newsPaging = newsViewModel.newsPaging.collectAsLazyPagingItems()

    // determine pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(newsPaging.loadState) {
        val refresh = newsPaging.loadState.refresh

        // show snackbar error when load state is error that comes
        // from pull to refresh
        if (refresh is LoadState.Error && isRefreshing) {
            snackbarHostState.showSnackbar(
                message =  refresh.error.message.takeIf { !it.isNullOrBlank() } ?: run {
                    context.getString(R.string.unable_to_load_news)
                }
            )
        }

        // reset the pull to refresh state
        if (refresh is LoadState.NotLoading && isRefreshing) {
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        // observe the paging event
        newsViewModel.pagingEvent.collect { action ->
            when(action) {
                PagingEvent.RELOAD -> newsPaging.refresh()
                PagingEvent.REFRESH -> {
                    isRefreshing = true
                    newsPaging.refresh()
                }
                PagingEvent.RETRY -> newsPaging.retry()
            }
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
            // combine the flag and the paging load state
            // to determine the pull to refresh state
            isRefreshing = isRefreshing
                    && newsPaging.loadState.refresh is LoadState.Loading,
            onRefresh = {
                // special case to fetch the topics again on pull to refresh
                topicsViewModel.fetchTopics()
                newsViewModel.onPullToRefresh()
            },
            modifier = modifier.padding(innerPadding)
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
                        navController = navController,
                        viewModel = localNewsViewModel
                    )
                }
                item(key = "topics-section") {
                    TopicSection(
                        modifier = Modifier.fillParentMaxWidth(),
                        viewModel = topicsViewModel
                    )
                }

                val refresh = newsPaging.loadState.refresh
                if (refresh is LoadState.Loading && !isRefreshing) {
                    item(key = "loading") {
                        Loading(modifier = Modifier.fillParentMaxSize())
                    }
                } else if (refresh is LoadState.Error && !isRefreshing) {
                    item(key = "error") {
                        ErrorState(
                            modifier = Modifier.fillParentMaxSize(),
                            message = refresh.error.message,
                            onRetry = {
                                // trigger paging event retry
                                newsViewModel.onRetryFetch()
                            }
                        )
                    }
                } else {
                    items(
                        count = newsPaging.itemCount,
                    ) { index ->
                        newsPaging[index]?.let { news ->
                            key(news.url.hashCode()) {
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
                        }
                    }

                    val append = newsPaging.loadState.append
                    if (append is LoadState.Loading) {
                        item(key = "loading-more") {
                            Loading(modifier = Modifier.fillParentMaxWidth())
                        }
                    } else if (append is LoadState.Error) {
                        item(key = "loading-more-error") {
                            ErrorState(
                                modifier = Modifier.fillParentMaxWidth(),
                                message = append.error.message,
                                onRetry = {
                                    // trigger paging event retry
                                    newsViewModel.onRetryFetch()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = message.takeIf { !it.isNullOrBlank() } ?: run {
                stringResource(R.string.unable_to_load_news)
            }
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
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