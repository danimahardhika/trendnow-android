package com.trend.now.ui.feature.news.topic

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trend.now.data.model.Topic

@Composable
fun TopicSection(
    modifier: Modifier = Modifier,
    viewModel: TopicsViewModel,
    topicListState: LazyListState = rememberLazyListState()
) {
    // collect the topics ui state state flow
    val uiState by viewModel.uiState.collectAsState()

    var firstTopicLoad by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.topics, topicListState) {
        if (!firstTopicLoad || uiState.topics.isEmpty()) return@LaunchedEffect

        val topicIndex = viewModel.indexOfTopic(uiState.selectedTopic)
        val firstVisibleIndex = topicListState.firstVisibleItemIndex
        val lastVisibleIndex = topicListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        if (topicIndex < firstVisibleIndex || topicIndex > lastVisibleIndex) {
            // scroll topics list to the selected topic on first load if needed
            topicListState.animateScrollToItem(
                viewModel.indexOfTopic(uiState.selectedTopic)
            )
        }
        firstTopicLoad = false
    }

    TopicsRow(
        // add animate content size
        // to show animation when the topics is loaded
        modifier = modifier.animateContentSize(
            animationSpec = tween()
        ),
        selectedTopic = uiState.selectedTopic,
        topics = uiState.topics,
        topicListState = topicListState
    ) { topic ->
        viewModel.selectTopic(topic.id)
    }
}

@Composable
private fun TopicsRow(
    modifier: Modifier = Modifier,
    selectedTopic: String,
    topics: List<Topic>,
    topicListState: LazyListState,
    onItemClick: (topic: Topic) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp
        ),
        state = topicListState
    ) {
        items(topics) { topic ->
            val selected = topic.id == selectedTopic
            FilterChip(
                label = {
                    Text(
                        text = topic.name,
                        fontWeight = if (selected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                },
                shape = CircleShape,
                // check if the current topic equals to the selected topic or not
                selected = selected,
                onClick = {
                    // save and update the selected topic
                    // use the topic id as identifier
                    onItemClick(topic)
                },
            )
        }
    }
}

@Preview
@Composable
private fun TopicsRowPreview(modifier: Modifier = Modifier) {
    TopicsRow(
        modifier = modifier.background(color = MaterialTheme.colorScheme.surface),
        selectedTopic = "science",
        topics = listOf(
            Topic(id = "general", name = "General"),
            Topic(id = "business", name = "Business"),
            Topic(id = "science", name = "Science")
        ),
        topicListState = rememberLazyListState()
    ) { }
}