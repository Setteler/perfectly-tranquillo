package com.methoda.tranquillo

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun allFourTabsPresentAndHabitsNavigates() {
        composeRule.onAllNodesWithText("Home").assertCountEquals(2)  // tab + placeholder title
        composeRule.onAllNodesWithText("Habits").assertCountEquals(1)
        composeRule.onAllNodesWithText("Mandala").assertCountEquals(1)
        composeRule.onAllNodesWithText("Garden").assertCountEquals(1)

        // For the click, just click the first match (tab)
        composeRule.onAllNodesWithText("Habits")[0].performClick()
        composeRule.onNodeWithText("SMALL THINGS, OFTEN").assertIsDisplayed()
    }
}
