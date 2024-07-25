/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

import org.junit.Test

import org.junit.Rule

class DropDownTest {
    private val test1 = "Test 1"
    private val test2 = "Test 2"
    private val test = "Test"
    private val selected = "Selected"

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialValueTest1() {
        buildComponent(test1)

        composeTestRule.onNodeWithText(test).assertIsDisplayed()
        composeTestRule.onNodeWithText(test1).assertIsDisplayed()
        composeTestRule.onNodeWithText(test2).assertIsNotDisplayed()
        composeTestRule.onNodeWithText("$selected: $test1").assertIsDisplayed()
    }

    @Test
    fun testInitialValueTest2() {
        buildComponent(test2)

        composeTestRule.onNodeWithText(test).assertIsDisplayed()
        composeTestRule.onNodeWithText(test2).assertIsDisplayed()
        composeTestRule.onNodeWithText(test1).assertIsNotDisplayed()
        composeTestRule.onNodeWithText("$selected: $test2").assertIsDisplayed()
    }

    @Test
    fun testClickTest1() {
        buildComponent(test1)

        composeTestRule.onNodeWithText("$selected: $test1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DropDownIcon").performClick()
        composeTestRule.onNodeWithText(test2).performClick()
        composeTestRule.onNodeWithText("$selected: $test2").assertIsDisplayed()
    }

    @Test
    fun testClickTest2() {
        buildComponent(test2)

        composeTestRule.onNodeWithText("$selected: $test2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("DropDownIcon").performClick()
        composeTestRule.onNodeWithText(test1).performClick()
        composeTestRule.onNodeWithText("$selected: $test1").assertIsDisplayed()
    }

    private fun buildComponent(initial: String) {
        composeTestRule.setContent {
            var selectedItem by remember { mutableStateOf(initial) }
            CloudAppTheme {
                val items = mutableListOf<String>()
                items.add(test1)
                items.add(test2)
                DropDown(items = items, initial = initial, onSelected = {selectedItem=it}, label = test)
                Text("$selected: $selectedItem")
            }
        }
    }
}