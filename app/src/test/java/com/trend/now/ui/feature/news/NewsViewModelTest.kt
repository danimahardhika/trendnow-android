package com.trend.now.ui.feature.news

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.trend.now.TestCoroutineRule
import com.trend.now.data.repository.NewsRepository
import com.trend.now.data.repository.UserPrefRepository
import com.trend.now.data.repository.UserPrefRepositoryImpl
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import org.junit.AfterClass
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder

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

    companion object {

        @AfterClass
        @JvmStatic
        fun clearMocks() {
            clearAllMocks()
            unmockkAll()
        }
    }
}