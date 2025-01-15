package com.trend.now.data.repository.ui.feature.news.topic

import app.cash.turbine.test
import com.trend.now.FakeUserPrefRepository
import com.trend.now.TestCoroutineRule
import com.trend.now.core.network.ApiResult
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import com.trend.now.ui.feature.news.topic.TopicsViewModel
import com.trend.now.util.topic
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopicsViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @RelaxedMockK
    private lateinit var mockNewsRepository: NewsRepository

    private lateinit var fakeUserPrefRepository: UserPrefRepository

    private val topicsViewModel: TopicsViewModel get() = TopicsViewModel(
        newsRepository = mockNewsRepository,
        userPrefRepository = fakeUserPrefRepository
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        fakeUserPrefRepository  = FakeUserPrefRepository()
    }

    @Test
    fun `should call fetchTopics when viewmodel is created`() = runTest {
        // given
        val topics = listOf(topic())
        coEvery { mockNewsRepository.fetchSupportedTopics() } returns ApiResult.Success(topics)
        // when
        val viewModel = topicsViewModel
        // wait until we get the value from the flow
        advanceUntilIdle()
        // then
        viewModel.topics.test {
            coVerify(exactly = 1) { mockNewsRepository.fetchSupportedTopics() }
            // check the emitted value
            assert(awaitItem() == topics)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchTopics should be called only once when selected topic value changed multiple times`() = runTest {
        // given
        val topics = listOf(topic())
        coEvery { mockNewsRepository.fetchSupportedTopics() } returns ApiResult.Success(topics)
        val viewModel = topicsViewModel
        advanceUntilIdle()
        viewModel.topics.test {
            coVerify(exactly = 1) { mockNewsRepository.fetchSupportedTopics() }
            assert(awaitItem() == topics)
            cancelAndIgnoreRemainingEvents()
        }
        // when
        // simulate change selected topic value
        viewModel.selectTopic("topic 1")
        // wait until we get the value from the flow
        advanceUntilIdle()
        // then
        viewModel.topics.test {
            // make sure the fetch supported topics not called again
            coVerify(exactly = 1) { mockNewsRepository.fetchSupportedTopics() }
            cancelAndIgnoreRemainingEvents()
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