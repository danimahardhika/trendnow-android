package com.trend.now.core.util

import com.trend.now.data.model.News
import com.trend.now.data.model.Publisher
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class NewsExtTimeSinceTest {

    private val calendar = Calendar.getInstance()

    @Test
    fun `should return x seconds time since`() {
        // given
        val now = calendar.apply {
            time = "2025-01-07T14:16:45+00:00".toDate()!!
        }
        val newsDate = "2025-01-07T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then
        assertEquals("5s ago", timeSince)
    }

    @Test
    fun `should return x seconds time since when now is exactly the same with news datetime`() {
        // given
        val datetime = "2025-01-07T14:16:45+00:00"
        val now = calendar.apply {
            time = datetime.toDate()!!
        }
        val news = news(datetime)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then
        assertEquals( "0s ago", timeSince)
    }

    @Test
    fun `should return x minutes time since`() {
        // given
        val now = calendar.apply {
            time = "2025-01-07T14:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-07T14:12:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then

        assertEquals("4m ago", timeSince)
    }

    @Test
    fun `should return x (floor) hours time since `() {
        // given
        val now = calendar.apply {
            time = "2025-01-07T15:46:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-07T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then
        assertEquals("1h ago", timeSince)
    }

    @Test
    fun `should return x days time since`() {
        // given
        val now = calendar.apply {
            time = "2025-01-08T14:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-07T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then
        assertEquals("1d ago", timeSince)
    }

    @Test
    fun `should return x (floor) weeks time since`() {
        // given
        val now = calendar.apply {
            time = "2025-01-16T15:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-07T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now.timeInMillis)
        // then
        assertEquals("1w ago", timeSince)
    }

    @Test
    fun `should return exact datetime when time since more than 4 weeks`() {
        // given
        val timeZone = TimeZone.getTimeZone("UTC")
        val now = calendar.apply {
            time = "2025-03-01T15:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-01T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(
            now = now.timeInMillis,
            timeZone = timeZone
        )
        // then
        assertEquals("01 Jan 2025 02:16 PM", timeSince)
    }

    @Test
    fun `should return exact datetime when news datetime is after now (invalid)`() {
        // given
        val timeZone = TimeZone.getTimeZone("UTC")
        val now = calendar.apply {
            time = "2025-01-01T14:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-01-02T14:16:40+00:00"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(
            now = now.timeInMillis,
            timeZone = timeZone
        )
        // then
        assertEquals("02 Jan 2025 02:16 PM", timeSince)
    }

    @Test
    fun `should return empty string when news datetime is empty`() {
        // given
        val now = calendar.apply {
            time = "2025-03-01T15:16:40+00:00".toDate()!!
        }
        val newsDate = ""
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now = now.timeInMillis)
        // then
        assertEquals("", timeSince)
    }

    @Test
    fun `should return empty string when news datetime format is different`() {
        // given
        val now = calendar.apply {
            time = "2025-03-01T15:16:40+00:00".toDate()!!
        }
        val newsDate = "2025-03-01 15:16"
        val news = news(newsDate)
        // when
        val timeSince = news.timeSince(now = now.timeInMillis)
        // then
        assertEquals("", timeSince)
    }

    private fun news(date: String) = News(
        title = "",
        url = "",
        excerpt = "",
        thumbnail = "",
        date = date,
        publisher = Publisher(name = "", url = "", favicon = ""),
    )
}