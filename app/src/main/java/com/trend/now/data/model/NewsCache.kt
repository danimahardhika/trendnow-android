package com.trend.now.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "news_cache",
    // make sure the url field marked as unique
    // so we can easily insert a new data with the same url to replace the existing data
    indices = [Index(value = ["url"], unique = true)]
)
data class NewsCache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val parentUrl: String,
    val createdAt: Long
)