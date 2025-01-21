package com.trend.now.ui.feature.news

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.trend.now.TestCoroutineRule
import com.trend.now.core.network.ApiResult
import com.trend.now.core.network.Meta
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.UserPrefRepositoryImpl
import com.trend.now.util.news
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @RelaxedMockK
    private lateinit var mockNewsRepository: NewsRepository

    private lateinit var userPrefRepository: UserPrefRepository
    private lateinit var dataStore: DataStore<Preferences>

    private val newsViewModel get() = NewsViewModel(
        newsRepository = mockNewsRepository,
        userPrefRepository = userPrefRepository
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = PreferenceDataStoreFactory.create(
            scope = coroutineRule.testScope,
            produceFile = { tempFolder.newFile("test.preferences_pb") }
        )
        userPrefRepository = UserPrefRepositoryImpl(dataStore)
    }

    @Test
    fun `should call fetchTrendingNews when viewmodel is created`() = runTest {
        // given
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), any(), any())
        } returns ApiResult.Success(news)

        // when
        val viewModel = newsViewModel
        advanceUntilIdle() // wait the flow emits

        // then
        viewModel.trendingNewsUiState.test {
            val selectedTopic = userPrefRepository.selectedTopic.first()
            val language = userPrefRepository.newsLanguage.first()
            val country = userPrefRepository.newsCountry.first()

            coVerify(exactly = 1) {
                mockNewsRepository.fetchTrendingNews(
                    topic = selectedTopic,
                    language = language,
                    country = country
                )
            }

            val uiState = awaitItem()
            assertEquals(news, uiState.data)
            assertEquals(true, uiState.success)
            assertEquals(false, uiState.loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should call fetchTrendingNews when on pull to refresh`() = runTest {
        // given
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), any(), any())
        } returns ApiResult.Success(news)
        val viewModel = newsViewModel
        advanceUntilIdle() // wait the flow emits

        // when
        viewModel.onPullToRefresh()
        advanceUntilIdle()

        // then
        viewModel.trendingNewsUiState.test {
            val selectedTopic = userPrefRepository.selectedTopic.first()
            val language = userPrefRepository.newsLanguage.first()
            val country = userPrefRepository.newsCountry.first()

            coVerify(exactly = 2) {
                mockNewsRepository.fetchTrendingNews(
                    topic = selectedTopic,
                    language = language,
                    country = country
                )
            }

            val uiState = awaitItem()
            assertEquals(news, uiState.data)
            assertEquals(true, uiState.success)
            assertEquals(false, uiState.loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should call fetchTrendingNews when selected topic is changed`() = runTest {
        // given
        val topic = "sport"
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), any(), any())
        } returns ApiResult.Success(news)
        val viewModel = newsViewModel

        // when
        userPrefRepository.setSelectedTopic(topic)
        advanceUntilIdle() // wait the flow emits

        // then
        viewModel.trendingNewsUiState.test {
            val language = userPrefRepository.newsLanguage.first()
            val country = userPrefRepository.newsCountry.first()

            coVerify(exactly = 1) {
                mockNewsRepository.fetchTrendingNews(
                    topic = topic,
                    language = language,
                    country = country
                )
            }

            val uiState = awaitItem()
            assertEquals(news, uiState.data)
            assertEquals(true, uiState.success)
            assertEquals(false, uiState.loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should call fetchTrendingNews when news preference is changed`() = runTest {
        // given
        val language = "id"
        val country = "id"
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), any(), any())
        } returns ApiResult.Success(news)
        val viewModel = newsViewModel

        // when
        userPrefRepository.setNewsCountry(country)
        userPrefRepository.setNewsLanguage(language)
        advanceUntilIdle() // wait the flow emits

        // then
        viewModel.trendingNewsUiState.test {
            val selectedTopic = userPrefRepository.selectedTopic.first()

            coVerify(exactly = 1) {
                mockNewsRepository.fetchTrendingNews(
                    topic = selectedTopic,
                    language = language,
                    country = country
                )
            }

            val uiState = awaitItem()
            assertEquals(news, uiState.data)
            assertEquals(true, uiState.success)
            assertEquals(false, uiState.loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should show load more and news data when load more trending news`() = runTest {
        // given
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), country = any(), page = null)
            // result for page 1
        } returns ApiResult.Success(news, meta = Meta(size = 10, page = 1, totalPages = 2))
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), country = any(), page = 2)
            // result for page 2
        } returns ApiResult.Success(news, meta = Meta(size = 10, page = 2, totalPages = 4))
        val viewModel = newsViewModel
        advanceUntilIdle() // wait the flow emits

        viewModel.trendingNewsUiState.test {
            assertEquals(1, awaitItem().data.size)

            // when
            viewModel.loadMoreTrendingNews()
            advanceUntilIdle() // wait the flow emits

            // then
            val selectedTopic = userPrefRepository.selectedTopic.first()
            val language = userPrefRepository.newsLanguage.first()
            val country = userPrefRepository.newsCountry.first()

            coVerify(exactly = 1) {
                mockNewsRepository.fetchTrendingNews(
                    topic = selectedTopic,
                    language = language,
                    country = country,
                    page = 2
                )
            }

            val state = awaitItem()
            assertEquals(2, state.data.size)
            assertEquals(true, state.success)
            assertEquals(2, state.page)
            assertEquals(true, state.showLoadMore)
        }
    }

    @Test
    fun `should hide load more when the current page == latest page`() = runTest {
        // given
        val news = listOf(news())
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), country = any(), page = null)
            // result for page 1
        } returns ApiResult.Success(news, meta = Meta(size = 10, page = 1, totalPages = 2))
        coEvery {
            mockNewsRepository.fetchTrendingNews(any(), any(), country = any(), page = 2)
            // result for page 2
        } returns ApiResult.Success(news, meta = Meta(size = 10, page = 2, totalPages = 2))
        val viewModel = newsViewModel
        advanceUntilIdle() // wait the flow emits


        viewModel.trendingNewsUiState.test {
            assertEquals(1, awaitItem().data.size)

            // when
            viewModel.loadMoreTrendingNews()
            advanceUntilIdle() // wait the flow emits

            // then
            val selectedTopic = userPrefRepository.selectedTopic.first()
            val language = userPrefRepository.newsLanguage.first()
            val country = userPrefRepository.newsCountry.first()

            coVerify(exactly = 1) {
                mockNewsRepository.fetchTrendingNews(
                    topic = selectedTopic,
                    language = language,
                    country = country,
                    page = 2
                )
            }

            val state = awaitItem()
            assertEquals(2, state.data.size)
            assertEquals(true, state.success)
            assertEquals(2, state.page)
            // show load more should be false
            assertEquals(false, state.showLoadMore)
        }
    }

    companion object {

        @AfterClass
        @JvmStatic
        fun clearMocks() {
            clearAllMocks()
            unmockkAll()
        }
    }
}