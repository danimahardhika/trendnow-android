package com.trend.now.ui.feature.news.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.trend.now.data.model.News
import com.trend.now.core.util.publisherName
import com.trend.now.core.util.timeSince
import com.trend.now.data.model.Publisher

@Composable
fun NewsCard(modifier: Modifier = Modifier, news: News, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = news.publisher.favicon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = " – ${news.publisherName()}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
            Text(
                text = news.timeSince(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                .height(164.dp),
            color = MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(12.dp)
        ) {
            AsyncImage(
                model = news.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = news.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        )
        Text(
            text = news.excerpt,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Preview
@Composable
private fun NewsCardPreview(modifier: Modifier = Modifier) {
    NewsCard(
        modifier = modifier,
        news = News(
            title = "Lorem ipsum dolor sit amet",
            excerpt = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua",
            url = "https://news.url.com",
            thumbnail = "",
            date = "2025-01-01T15:16:40+00:00",
            publisher = Publisher(name = "Publisher", url = "", favicon = "")
        )
    ) { }
}