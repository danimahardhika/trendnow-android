package com.trend.now.core.util

import com.trend.now.data.model.News
import java.net.MalformedURLException
import java.net.URL
import java.util.Calendar
import java.util.TimeZone

fun News.publisherName(): String = publisher.name.takeIf {
    it.isNotBlank()
} ?: run {
    try {
        URL(publisher.url).host
    } catch (e: MalformedURLException) {
        ""
    }
}

fun News.timeSince(
    now: Long = Calendar.getInstance().timeInMillis,
    timeZone: TimeZone = Calendar.getInstance().timeZone
): String {
    val date = this.date.toDate() ?: return ""

    val newsDate = Calendar.getInstance().apply {
        time = date
    }
    val timeDifferenceInMillis = now - newsDate.timeInMillis
    val seconds = timeDifferenceInMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    return when {
        weeks > 4 || timeDifferenceInMillis < 0 -> {
            this.date.formatDateTime(timeZone = timeZone)
        }
        weeks > 0 -> "${weeks}w ago"
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "${seconds}s ago"
    }
}