package com.trend.now.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_cache")
data class NewsCache(
    @PrimaryKey(autoGenerate = false) val url: String,
    val parentUrl: String,
    val createdAt: Long
)