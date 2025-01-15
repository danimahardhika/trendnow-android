package com.trend.now.ui.feature.localnews.locale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.trend.now.KEY_LANGUAGE_CHANGED
import com.trend.now.R
import com.trend.now.core.ui.state.UiState
import com.trend.now.core.util.isListAtTop
import com.trend.now.data.model.Country
import com.trend.now.data.model.Language
import com.trend.now.data.model.NewsPreference
import com.trend.now.ui.component.AppBar

enum class LocaleList { COUNTRY, LANGUAGE }

@Composable
fun LocaleListScreen(
    localeList: LocaleList,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: LocaleListViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val appBarElevation by remember {
        // top app bar elevation logic
        // show elevation when user start scrolling the news list
        derivedStateOf { if (listState.isListAtTop()) 0.dp else 8.dp }
    }

    val list by rememberSaveable { mutableStateOf(localeList) }
    val uiState by viewModel.uiState.collectAsState()
    val newsPreference by viewModel.newsPreference.collectAsState()

    Scaffold(
        topBar = {
            AppBar(
                title = if (localeList == LocaleList.LANGUAGE) {
                    stringResource(R.string.local_news_language_title)
                } else {
                    stringResource(R.string.local_news_country_title)
                },
                elevation = appBarElevation
            ) {
                navController.popBackStack()
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Success -> {
                if (list == LocaleList.COUNTRY) {
                    CountryList(
                        data = state.data.values.toList(),
                        newsPreference = newsPreference,
                        navController = navController,
                        viewModel = viewModel,
                        listState = listState,
                        modifier = modifier.padding(innerPadding)
                    )
                } else if (list == LocaleList.LANGUAGE) {
                    val country = state.data[newsPreference.country]
                    val languages = country?.languages
                    if (languages != null) {
                        LanguageList(
                            data = languages,
                            newsPreference = newsPreference,
                            viewModel = viewModel,
                            listState = listState,
                            modifier = modifier.padding(innerPadding)
                        )
                    }
                }
            }
            is UiState.Loading -> {}
            else -> {}
        }
    }
}

@Composable
private fun CountryList(
    data: List<Country>,
    newsPreference: NewsPreference,
    navController: NavHostController,
    viewModel: LocaleListViewModel,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(items = data) { country ->
            Item(
                selected = country.code == newsPreference.country,
                text = country.name
            ) {
                viewModel.setNewsCountry(country) { languageChanged ->
                    // send data to the previous screen
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_LANGUAGE_CHANGED, languageChanged)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageList(
    data: List<Language>,
    newsPreference: NewsPreference,
    viewModel: LocaleListViewModel,
    listState: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        item {
            Text(
                text = stringResource(R.string.local_news_language_info),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp)
            )
        }
        items(items = data) { language ->
            Item(
                selected = language.code == newsPreference.language,
                text = language.name
            ) {
                viewModel.setNewsLanguage(language)
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Item(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(text = text)
        }
    }
}