package com.trend.now.core.util

import java.util.concurrent.TimeUnit

fun TimeUnit.fromMs(ms: Long): Long = convert(ms, TimeUnit.MILLISECONDS)