package com.trend.now.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.trend.now.core.network.ApiResult
import com.trend.now.data.model.News
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.UserPrefRepository
import kotlinx.coroutines.flow.first

class NewsPagingSource(
    private val newsRepository: NewsRepository,
    private val userPrefRepository: UserPrefRepository
) : PagingSource<Int, News>() {

    override fun getRefreshKey(state: PagingState<Int, News>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // anchorPosition is the most recent accessed item by index
            // (eg. the current visible item on the screen)
            // try to find the anchor page info based on the visible item (anchorPosition)
            val anchorPage = state.closestPageToPosition(anchorPosition)
            // get the current page from the anchor page info
            // anchor page only has prev and next page info
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        val result = newsRepository.fetchTrendingNews(
            topic = userPrefRepository.selectedTopic.first(),
            language = userPrefRepository.newsLanguage.first(),
            country = userPrefRepository.newsCountry.first(),
            // use params.key directly because we are using null value for the page 1
            page = params.key
        )
        val page = params.key ?: 1
        return when(result) {
            is ApiResult.Success -> LoadResult.Page(
                data = result.data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (result.data.isEmpty()) null else page + 1
            )
            is ApiResult.Error -> {
                val throwable = if (result.message.isNotBlank()) {
                    // prioritize the error message first if its exist
                    Exception(result.message)
                } else {
                    result.throwable ?: Exception("")
                }
                LoadResult.Error(throwable)
            }
        }
    }
}