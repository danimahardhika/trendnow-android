package com.trend.now.util

import com.trend.now.data.model.News
import com.trend.now.data.model.Publisher
import com.trend.now.data.model.Topic

fun topic(
    id: String = "id",
    name: String = "name",
    createdAt: Long = System.currentTimeMillis()
): Topic = Topic(id, name, createdAt)

fun news(
    title: String = "title",
    url: String = "url",
    excerpt: String = "excerpt",
    thumbnail: String = "thumbnail",
    date: String = "2025-01-01T00:00:00+00:00",
    publisher: Publisher = Publisher(name = "name", url = "url", favicon = "favicon"),
) = News(
    title = title,
    url = url,
    excerpt = excerpt,
    thumbnail = thumbnail,
    date = date,
    publisher = publisher,
)