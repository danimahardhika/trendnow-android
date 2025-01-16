package com.trend.now.core.util

import com.trend.now.data.model.News
import com.trend.now.data.model.Publisher
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsExtPublisherNameTest {

    @Test
    fun `should return publisher name when it's not empty`() {
        // given
        val news = news(Publisher(name = "Publisher Name", url = "", favicon = ""))

        // when
        val publisherName = news.publisherName()

        // then
        assertEquals(news.publisher.name, publisherName)
    }

    @Test
    fun `should return publisher url host when publisher name is empty`() {
        // given
        val news = news(
            Publisher(name = "", url = "https://publisher.url.com/something", favicon = "")
        )

        // when
        val publisherName = news.publisherName()

        // then
        assertEquals("publisher.url.com", publisherName)
    }

    @Test
    fun `should return empty string when publisher name and url is empty`() {
        // given
        val news = news(
            Publisher(name = "", url = "", favicon = "")
        )

        // when
        val publisherName = news.publisherName()

        // then
        assertEquals("", publisherName)
    }

    private fun news(publisher: Publisher) = News(
        title = "",
        url = "",
        excerpt = "",
        thumbnail = "",
        date = "",
        publisher = publisher,
    )
}