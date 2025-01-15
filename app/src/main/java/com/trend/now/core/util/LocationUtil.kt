package com.trend.now.core.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Get country code based on the location
 * @param context The activity or application context
 * @return The [Address] object contains country code information
 */
suspend fun Location.countryCode(context: Context): Address? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCoroutine { continuation ->
                geocoder.getFromLocation(latitude, longitude, 1) {
                    continuation.resume(it.firstOrNull())
                }
            }
        } else {
            suspendCoroutine { continuation ->
                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
                continuation.resume(address)
            }
        }
    } catch (e: Exception) {
        null
    }
}