package com.trend.now.ui.feature.localnews

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.trend.now.KEY_LANGUAGE_CHANGED
import com.trend.now.R
import com.trend.now.core.ui.state.UiState
import com.trend.now.ui.component.AppBar
import com.trend.now.ui.component.CellAction
import com.trend.now.ui.navigation.AppRoute
import java.util.Locale

@Composable
fun LocalNewsSettingsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: LocalNewsSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(navController.currentBackStackEntry) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val changed = savedStateHandle?.get<Boolean>(KEY_LANGUAGE_CHANGED)
        if (changed == true) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.local_news_language_changed)
            )
            // remove the value from saved state handle show the snackbar
            savedStateHandle.remove<Boolean>(KEY_LANGUAGE_CHANGED)
        }
    }

    val state = uiState
    if (state is UiState.Success) {
        Scaffold(
            modifier = modifier,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                AppBar(title = stringResource(R.string.local_news_settings)) {
                    navController.popBackStack()
                }
            }
        ) { innerPadding ->
            val locale = Locale(state.data.language, state.data.country)
            Column(
                modifier = Modifier
                    .padding(innerPadding),
            ) {
                Text(
                    text = stringResource(R.string.local_news_settings_info),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
                CellAction(
                    text = stringResource(R.string.country),
                    description = locale.displayCountry,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    navController.navigate(route = AppRoute.COUNTRY_LIST)
                }
                CellAction(
                    text = stringResource(R.string.language),
                    description = locale.displayLanguage,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_language),
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    navController.navigate(route = AppRoute.LANGUAGE_LIST)
                }
            }
        }
    }
}