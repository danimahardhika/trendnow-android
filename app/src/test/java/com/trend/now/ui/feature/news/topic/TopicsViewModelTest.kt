package com.trend.now.ui.feature.news.topic

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.trend.now.TestCoroutineRule
import com.trend.now.core.network.ApiResult
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.UserPrefRepositoryImpl
import com.trend.now.util.topic
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
class TopicsViewModelTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @get:Rule
    val tempFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @RelaxedMockK
    private lateinit var mockNewsRepository: NewsRepository

    private lateinit var userPrefRepository: UserPrefRepository
    private lateinit var dataStore: DataStore<Preferences>

    private val topicsViewModel: TopicsViewModel get() = TopicsViewModel(
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
    fun `should call fetchSupportedTopics when viewmodel is created and after selected topic value is set`() = runTest {
        // given
        val topics = listOf(topic())
        coEvery { mockNewsRepository.fetchSupportedTopics() } returns ApiResult.Success(topics)

        // when
        val viewModel = topicsViewModel
        advanceUntilIdle() // wait the flow emits

        //  then
        viewModel.uiState.test {
            coVerify(exactly = 1) { mockNewsRepository.fetchSupportedTopics() }
            // check the emitted value
            val uiState = awaitItem()
            assert(uiState.topics == topics)
            assert(uiState.selectedTopic == userPrefRepository.selectedTopic.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should update the ui state when selected topic value changed`() = runTest {
        // given
        val selectedTopic = "new topic"
        val topics = listOf(topic())
        coEvery { mockNewsRepository.fetchSupportedTopics() } returns ApiResult.Success(topics)
        // viewmodel initialized
        val viewModel = topicsViewModel

        // when
        viewModel.selectTopic(selectedTopic)
        advanceUntilIdle() // wait the flow emits

        // then
        viewModel.uiState.test {
            // check the emitted value
            assertEquals(selectedTopic, awaitItem().selectedTopic)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should do nothing when selected topic value changed with the same value`() = runTest {
        // given
        val selectedTopic = "new topic"
        val topics = listOf(topic())
        coEvery { mockNewsRepository.fetchSupportedTopics() } returns ApiResult.Success(topics)
        // viewmodel initialized
        val viewModel = topicsViewModel
        // select new topic
        viewModel.selectTopic(selectedTopic)
        advanceUntilIdle() // wait the flow emits

        viewModel.uiState.test {
            assertEquals(selectedTopic, awaitItem().selectedTopic)

            // when
            // select new topic again
            viewModel.selectTopic(selectedTopic)
            advanceUntilIdle() // wait the flow emits

            // then
            // no emits triggered
            expectNoEvents()
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