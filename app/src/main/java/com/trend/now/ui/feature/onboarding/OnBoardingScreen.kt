package com.trend.now.ui.feature.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.LOCATION_SERVICE
import android.location.LocationManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.trend.now.DEFAULT_LANGUAGE
import com.trend.now.R
import com.trend.now.core.util.countryCode
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnBoardingScreen(
    modifier: Modifier = Modifier,
    viewModel: OnBoardingViewModel = hiltViewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current
    val locationManager =
        context.getSystemService(LOCATION_SERVICE) as LocationManager
    val locationPermission = rememberPermissionState(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val hasRequestedPermission = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            val location = locationManager.getLastKnownLocation(
                LocationManager.GPS_PROVIDER
            ) ?: locationManager.getLastKnownLocation(
                LocationManager.NETWORK_PROVIDER
            )
            if (location != null) {
                val address = location.countryCode(context)
                // set the local news based on device language and location
                viewModel.setupLocalNews(
                    language = Locale.getDefault().language,
                    country = address?.countryCode ?: ""
                )
            }
        } else {
            // need to add this variable as workaround
            // somehow the permission status is denied with show rationale = true
            // even when the app has not request the location permission
            if (hasRequestedPermission.value) {
                // user did not grant the location permission
                // set the local news with default language
                viewModel.setupLocalNews(language = DEFAULT_LANGUAGE)
            } else if (locationPermission.status.shouldShowRationale) {
                // TODO: user did not grant the location permission permanently
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                // set the inner padding first
                .padding(innerPadding)
                // then override the horizontal padding
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(onClick = {
                    viewModel.setupLocalNews(language = Locale.getDefault().language)
                }) {
                    Text(text = stringResource(R.string.skip))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_location),
                    contentDescription = "image-location-permission",
                    modifier = Modifier.size(280.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(
                        R.string.onboarding_desc,
                        "\"${stringResource(R.string.app_name)}\""
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            AnimatedButton (
                text = if (locationPermission.status.shouldShowRationale
                    && !hasRequestedPermission.value) {
                    stringResource(R.string.go_to_settings)
                } else {
                    stringResource(R.string.allow_permission)
                },
                loading = loading
            ) {
                if (locationPermission.status.shouldShowRationale
                    && !hasRequestedPermission.value) {
                    // TODO: go to settings
                } else {
                    viewModel.setLoading(true)
                    locationPermission.launchPermissionRequest()
                    hasRequestedPermission.value = true
                }
            }
            Spacer(modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

@Composable
fun AnimatedButton(
    text: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    val widthFraction by animateFloatAsState(
        targetValue = if (loading) 0f else 0.9f,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )
    val buttonText = remember {
        derivedStateOf { if (widthFraction == 0.9f) text else "" }
    }

    Button(
        modifier = modifier
            .height(48.dp)
            .requiredWidthIn(min = 48.dp)
            .fillMaxWidth(fraction = widthFraction),
        contentPadding = PaddingValues(0.dp),
        onClick = {
            if (!loading) onClick()
        }
    ) {
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.surface,
                strokeWidth = 3.dp
            )
        }
        if (!loading) {
            Text(
                text = buttonText.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}