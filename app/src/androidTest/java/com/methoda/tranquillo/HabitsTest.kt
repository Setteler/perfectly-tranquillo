package com.methoda.tranquillo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class HabitsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun habitsScreenRendersBothTabsAndSeedsDailyHabits() {
        // Navigate to Habits.
        composeRule.onAllNodesWithText("Habits")[0].performClick()

        // Daily + Weekly tab labels render inside the TabRow.
        composeRule.onNodeWithText("Daily").assertIsDisplayed()
        composeRule.onNodeWithText("Weekly").assertIsDisplayed()

        // At least one of the seeded daily habits is visible — "Drink water"
        // is seeded in HabitSeeder.DAILY and exact-matches a label.
        composeRule.onNodeWithText("Drink water").assertIsDisplayed()
    }
}
