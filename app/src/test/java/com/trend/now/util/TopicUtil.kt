package com.trend.now.util

import com.trend.now.data.model.Topic

fun topic(
    id: String = "id",
    name: String = "name",
    createdAt: Long = System.currentTimeMillis()
): Topic = Topic(id, name, createdAt)