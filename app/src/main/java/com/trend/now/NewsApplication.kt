package com.trend.now

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.bitmapConfig
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NewsApplication : Application(), SingletonImageLoader.Factory {

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .bitmapConfig(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Bitmap.Config.HARDWARE
                } else {
                    // use low quality bitmap config on api < 26
                    // for lower memory usage
                    Bitmap.Config.RGB_565
                }
            )
            .build()
}
