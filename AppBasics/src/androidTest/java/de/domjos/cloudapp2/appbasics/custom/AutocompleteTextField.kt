/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import org.junit.Rule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.TextFieldValue
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import org.junit.Test

class AutocompleteTextField {
    private val test = "Test"
    private val test1 = "Test 1"
    private val test2 = "Test 2"
    private val selected = "Selected"

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testInitialAutocompleteTest1() {
        this.buildComponent(test1)

        this.composeTestRule.onNodeWithText(test1).assertIsDisplayed()
        this.composeTestRule.onNodeWithText(test2).assertIsNotDisplayed()
        this.composeTestRule.onNodeWithText(test).assertIsDisplayed()
        this.composeTestRule.onNodeWithText("$selected: $test1").assertIsDisplayed()
    }

    @Test
    fun testInitialAutocompleteTest2() {
        this.buildComponent(test2)

        this.composeTestRule.onNodeWithText(test2).assertIsDisplayed()
        this.composeTestRule.onNodeWithText(test1).assertIsNotDisplayed()
        this.composeTestRule.onNodeWithText(test).assertIsDisplayed()
        this.composeTestRule.onNodeWithText("$selected: $test2").assertIsDisplayed()
    }

    @Test
    fun testAutocompleteTest1() {
        this.buildComponent("")

        this.composeTestRule.onNodeWithText(test1).assertIsNotDisplayed()
        this.composeTestRule.onNodeWithTag("textField").performTextInput(test)
        this.composeTestRule.onNodeWithTag("icon").assertIsDisplayed()
        this.composeTestRule.onNodeWithTag("icon").performClick()
        this.composeTestRule.onNodeWithTag("dropDownMenu").assertIsDisplayed()
        this.composeTestRule.onNodeWithText(test1).performClick()
        this.composeTestRule.onNodeWithText("$selected: $test1").assertIsDisplayed()
    }

    @Test
    fun testAutocompleteTest2() {
        this.buildComponent("")

        this.composeTestRule.onNodeWithText(test2).assertIsNotDisplayed()
        this.composeTestRule.onNodeWithTag("textField").performTextInput(test)
        this.composeTestRule.onNodeWithTag("icon").assertIsDisplayed()
        this.composeTestRule.onNodeWithTag("icon").performClick()
        this.composeTestRule.onNodeWithTag("dropDownMenu").assertIsDisplayed()
        this.composeTestRule.onNodeWithText(test2).performClick()
        this.composeTestRule.onNodeWithText("$selected: $test2").assertIsDisplayed()
    }

    private fun buildComponent(initial: String) {
        composeTestRule.setContent {
            var selectedItem by remember { mutableStateOf(initial) }


            CloudAppTheme {
                val items = mutableListOf<String>()
                items.add(test1)
                items.add(test2)
                Row {
                    AutocompleteTextField(
                        value = TextFieldValue(selectedItem),
                        onValueChange = {selectedItem = it.text},
                        label = { Text(test) },
                        onAutoCompleteChange = {items.filter { text -> text.contains(it.text) }.toList()},
                        multi = false
                    )
                }

                Row {
                    Text("$selected: $selectedItem")
                }
            }
        }
    }
}