package com.trend.now.core.util

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.isListAtTop() =
    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0