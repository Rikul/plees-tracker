/*
 * Copyright 2024 Miklos Vajna
 *
 * SPDX-License-Identifier: MIT
 */

package hu.vmiklos.plees_tracker

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for Times Woken Up functionality.
 */
@RunWith(AndroidJUnit4::class)
class TimesWokenTest : UITestBase() {
    @JvmField
    @Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testAddSleepWithWakes() {
        // Given no sleeps:
        resetDatabase()

        // Open Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Set wakes to 5
        val wakesInput = findObjectByRes("add_sleep_wakes")
        wakesInput.text = "5"

        // Save
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Verify in DB
        device.waitForIdle()
        var wakes: Int
        runBlocking {
            val sleeps = DataModel.database.sleepDao().getAll()
            wakes = sleeps[0].wakes
        }
        assertEquals(5, wakes)

        // Verify in UI (Dashboard)
        // Check that the sleep item displays the correct wakes value
        assertResText("sleep_item_wakes", "5")
    }

    @Test
    fun testEditSleepWakes() {
        // Given a sleep with 0 wakes
        resetDatabase()
        createSleep()
        device.waitForIdle()

        // Click on the sleep item to search for it
        val sleepItem = findObjectByRes("sleep_swipeable")
        sleepItem.click()

        // Update wakes to 3
        val wakesInput = findObjectByRes("sleep_item_wakes")
        wakesInput.text = "3"
        
        // Go back (Save is automatic on edit/back in this app logic)
        device.pressBack()
        device.waitForIdle()

        // Verify in DB
        var wakes: Int
        runBlocking {
            val sleeps = DataModel.database.sleepDao().getAll()
            wakes = sleeps[0].wakes
        }
        assertEquals(3, wakes)
        
        // Verify in Dashboard
        assertResText("sleep_item_wakes", "3")
    }

    @Test
    fun testTimesWoken() {
        // Given no sleeps:
        resetDatabase()

        // Open Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Verify that default wakes value is 0
        val wakesInput = findObjectByRes("add_sleep_wakes")
        assertEquals("0", wakesInput.text)


        // Verify that we cant save with more than 10 wakes
        wakesInput.text = "11"
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Wait for validation to occur
        device.waitForIdle()

        // Verify validation failed by checking we're still on Add Sleep screen
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addSleepTitle = findObjectByText(context.getString(R.string.add_sleep))
        assertNotNull(addSleepTitle) // Still on Add Sleep screen = validation worked


        // Set wakes to 5
        findObjectByRes("add_sleep_wakes").text = "5"

        // Save
        findObjectByRes("add_sleep_save").click()

        // Verify in DB
        device.waitForIdle()
        var wakes: Int
        runBlocking {
            val sleeps = DataModel.database.sleepDao().getAll()
            wakes = sleeps[0].wakes
        }
        assertEquals(5, wakes)

        // Verify in UI (Dashboard)
        // Check that the sleep item displays the correct wakes value
        assertResText("sleep_item_wakes", "5")
    }
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
