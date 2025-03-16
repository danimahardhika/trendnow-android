package com.trend.now.ui.features.news

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trend.now.HiltTestActivity
import com.trend.now.core.ui.theme.TrendNowTheme
import com.trend.now.ui.feature.news.NewsScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NewsScreenTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Test
    fun shouldShowAppTitleOnTheTopAppBar() {
        // when
        composeTestRule.setContent {
            TrendNowTheme {
                NewsScreen(navController = rememberNavController())
            }
        }

        // then
        composeTestRule.onNodeWithText("TrendNow").assertIsDisplayed()
    }
}