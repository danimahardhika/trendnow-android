package com.trend.now.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtil {
    fun hasNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val netCapabilityInternet =
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return networkCapabilities != null && netCapabilityInternet == true
    }
}