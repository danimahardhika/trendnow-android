package com.trend.now.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trend.now.ui.feature.localnews.LocalNewsSettingsScreen
import com.trend.now.ui.feature.localnews.locale.LocaleList
import com.trend.now.ui.feature.localnews.locale.LocaleListScreen
import com.trend.now.ui.feature.main.MainViewModel
import com.trend.now.ui.feature.news.NewsScreen
import com.trend.now.ui.feature.onboarding.OnBoardingScreen

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    val screenWidth = LocalConfiguration.current.screenWidthDp // in dp
    val screenWidthPx = with(LocalDensity.current) { screenWidth.dp.toPx() }.toInt()

    if (!mainUiState.loading) {
        NavHost(
            navController = navController,
            startDestination = if (mainUiState.showOnBoarding) {
                AppRoute.ONBOARDING
            } else {
                AppRoute.NEWS
            },
            enterTransition = { slideInHorizontally(initialOffsetX = { screenWidthPx }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
        ) {
            composable(route = AppRoute.ONBOARDING) {
                OnBoardingScreen(modifier = modifier)
            }
            composable(route = AppRoute.NEWS) {
                NewsScreen(modifier = modifier, navController = navController)
            }
            composable(
                route = AppRoute.LOCAL_NEWS_SETTINGS,
            ) {
                LocalNewsSettingsScreen(modifier = modifier, navController = navController)
            }
            composable(route = AppRoute.COUNTRY_LIST) {
                LocaleListScreen(
                    localeList = LocaleList.Country,
                    modifier = modifier,
                    navController = navController,
                )
            }
            composable(route = AppRoute.LANGUAGE_LIST) {
                LocaleListScreen(
                    localeList = LocaleList.Language,
                    modifier = modifier,
                    navController = navController,
                )
            }
        }
    }
}