package com.trend.now.core.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX"
private const val SIMPLE_DATETIME_FORMAT = "dd MMM yyyy hh:mm a"

fun String.formatDateTime(
    input: String = DEFAULT_DATETIME_FORMAT,
    output: String = SIMPLE_DATETIME_FORMAT,
    timeZone: TimeZone = Calendar.getInstance().timeZone
): String {
    return try {
        val date = this.toDate(format = input, timeZone = timeZone) ?: return ""
        return SimpleDateFormat(output, Locale.getDefault()).also {
            it.timeZone = timeZone
        }.format(date)
    } catch (e: ParseException) {
        ""
    }
}

fun String.toDate(
    format: String = DEFAULT_DATETIME_FORMAT,
    timeZone: TimeZone = Calendar.getInstance().timeZone
): Date? {
    return try {
        val inputFormat = SimpleDateFormat(format, Locale.getDefault())
        val date = inputFormat.parse(this) ?: return null
        return Calendar.getInstance().also {
            it.time = date
            it.timeZone = timeZone
        }.time
    } catch (e: ParseException) {
        null
    }
}