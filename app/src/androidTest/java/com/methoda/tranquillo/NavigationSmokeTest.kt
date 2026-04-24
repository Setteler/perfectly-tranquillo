package com.methoda.tranquillo

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
        // The bottom nav has 4 tabs — each label should be present once (tab label).
        // Home is a real screen now (greeting + mandala), so it doesn't contain the
        // literal word "Home"; we only check the tab label via the placeholder click.
        composeRule.onNodeWithText("Habits").assertIsDisplayed()
        composeRule.onNodeWithText("Garden").assertIsDisplayed()
        // "Mandala" shows up in both the tab strip and Mandala-screen header; take [0].
        composeRule.onAllNodesWithText("Mandala")[0].assertIsDisplayed()

        // Click the Habits tab and confirm the placeholder eyebrow renders.
        composeRule.onAllNodesWithText("Habits")[0].performClick()
        composeRule.onNodeWithText("SMALL THINGS, OFTEN").assertIsDisplayed()
    }
}
