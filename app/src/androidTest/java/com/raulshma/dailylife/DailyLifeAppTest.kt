package com.raulshma.dailylife

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyLifeAppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_andShowsTopBar() {
        composeTestRule.onNodeWithText("DailyLife Photos").assertIsDisplayed()
    }

    @Test
    fun fabIsVisible_andOpensQuickAdd() {
        composeTestRule.onNodeWithContentDescription("Add").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.onNodeWithText("Quick add").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationTabsAreVisible() {
        composeTestRule.onNodeWithText("Photos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Search").assertIsDisplayed()
        composeTestRule.onNodeWithText("Collections").assertIsDisplayed()
    }

    @Test
    fun searchTabOpensTimelineScreen() {
        composeTestRule.onNodeWithText("Search").performClick()
        composeTestRule.onNodeWithContentDescription("Search timeline").assertIsDisplayed()
    }

    @Test
    fun settingsButtonOpensPreferences() {
        composeTestRule.onNodeWithContentDescription("Notification preferences").performClick()
        composeTestRule.onNodeWithText("Notification preferences").assertIsDisplayed()
    }

    @Test
    fun s3BackupButtonOpensBackupSettings() {
        composeTestRule.onNodeWithContentDescription("Cloud backup settings").performClick()
        composeTestRule.onNodeWithText("Cloud backup (BYOK S3)").assertIsDisplayed()
    }
}
