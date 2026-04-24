package com.methoda.tranquillo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class HomeMandalaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tapBodyRowOpensPetalSheet() {
        // Move from Home to Mandala by clicking the Mandala tab.
        composeRule.onAllNodesWithText("Mandala")[0].performClick()

        // "Body" is the first resource label in the 8-row list; tap opens the sheet.
        composeRule.onNodeWithText("Body").performClick()

        // The sheet header renders "THIS MORNING" or "THIS EVENING" depending on
        // the device clock. Either one proves the sheet is open.
        val morningShowing = composeRule.onAllNodesWithText("THIS MORNING").fetchSemanticsNodes().isNotEmpty()
        val eveningShowing = composeRule.onAllNodesWithText("THIS EVENING").fetchSemanticsNodes().isNotEmpty()
        assert(morningShowing || eveningShowing) {
            "Expected either 'THIS MORNING' or 'THIS EVENING' to be visible in the opened PetalSheet"
        }
    }
}
