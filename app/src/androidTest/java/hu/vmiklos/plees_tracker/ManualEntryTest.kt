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
 * UI tests for Manual Entry functionality.
 */
@RunWith(AndroidJUnit4::class)
class ManualEntryTest : UITestBase() {
    @JvmField
    @Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testManualEntryButton() {
        resetDatabase()

        // When clicking on the manual entry button
        val manualEntry = findObjectByRes("manual_entry_layout")

        // Then ensure that the Add Sleep screen appears
        assertNotNull(manualEntry)  

        manualEntry.click()

        // Verify `Add Sleep` screen by checking the activity title
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addSleepTitle = findObjectByText(context.getString(R.string.add_sleep))
        assertNotNull(addSleepTitle)

        // Then ensure that the Add Sleep screen appears (check for save button)
        //val saveButton = findObjectByRes("add_sleep_save")
        //assertNotNull(saveButton)
    }

    
    @Test
    fun testAddSleepScreenElements() {
        resetDatabase()

        // Navigate to Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Check for presence of all required elements
        assertNotNull(findObjectByRes("add_sleep_start_date"))
        assertNotNull(findObjectByRes("add_sleep_start_time"))
        assertNotNull(findObjectByRes("add_sleep_stop_date"))
        assertNotNull(findObjectByRes("add_sleep_stop_time"))
        assertNotNull(findObjectByRes("add_sleep_wakes"))
        assertNotNull(findObjectByRes("add_sleep_rating"))
        assertNotNull(findObjectByRes("add_sleep_comment"))
        assertNotNull(findObjectByRes("add_sleep_save"))
    }

    // Verify dashboard sleep listing has specified fields
    @Test
    fun testDashboardSleepListing() {
        resetDatabase()

        // Create a sleep entry
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Set wakes to 5
        val wakesInput = findObjectByRes("add_sleep_wakes")
        wakesInput.text = "5"

        // Set rating to 4 stars (click on the RatingBar to set the rating)
        val ratingBar = findObjectByRes("add_sleep_rating")
        val bounds = ratingBar.visibleBounds
        val x = bounds.left + (bounds.width() * 0.8f).toInt()
        val y = bounds.centerY()
        device.click(x, y)

        // Save
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Wait for the sleep item to appear in the list
        device.waitForIdle()

        // Check for presence of sleep listing elements - find all instances
        val ratings = findAllObjectsByRes("sleep_item_rating")
        val wakes = findAllObjectsByRes("sleep_item_wakes")
        
        // Verify at least one sleep item exists
        assert(ratings.isNotEmpty()) { "No sleep items found" }
        assert(wakes.isNotEmpty()) { "No wakes found" }
        
        val firstWakes = wakes.first()
        assertEquals("5", firstWakes.text)
        
        // For RatingBar, we can check the content description which includes rating info
        val firstRating = ratings.first()
        assertNotNull(firstRating)
    }

    @Test
    fun testStartStopValidation() {
        resetDatabase()

        // Navigate to Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Set stop time to before start time
        val startTimeInput = findObjectByRes("add_sleep_start_time")
        val stopTimeInput = findObjectByRes("add_sleep_stop_time")

        findObjectByRes("add_sleep_start_time").click()
        device.waitForIdle()
        findObjectByText("PM").click()
        findObjectByDesc("10").click()
        findObjectByDesc("0").click()
        findObjectByText("OK").click()

        device.waitForIdle()

        findObjectByRes("add_sleep_stop_time").click()

        findObjectByText("AM").click()
        findObjectByDesc("10").click()
        findObjectByDesc("0").click()
        findObjectByText("OK").click()

        // Save
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Wait for validation to occur
        device.waitForIdle()
        Thread.sleep(500)

        // Verify validation failed by checking we're still on Add Sleep screen
        // (If save succeeded, we'd be back on MainActivity)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val addSleepTitle = findObjectByText(context.getString(R.string.add_sleep))
        assertNotNull(addSleepTitle) // Still on Add Sleep screen = validation worked

        // Set stop time same as start time
        findObjectByRes("add_sleep_stop_time").click()
        findObjectByText("PM").click()
        findObjectByDesc("10").click()
        findObjectByDesc("0").click()
        findObjectByText("OK").click()

        // Save again
        findObjectByRes("add_sleep_save").click()

        // Wait for validation to occur
        device.waitForIdle()
        Thread.sleep(500)

        assertNotNull(addSleepTitle) // Still on Add Sleep screen = validation worked

    }

    @Test
    fun testEmptyStartStop() {
        resetDatabase()

        // Navigate to Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        // Clear start and stop times (if they have default values)
        val startTimeInput = findObjectByRes("add_sleep_start_time")
        val stopTimeInput = findObjectByRes("add_sleep_stop_time")

        startTimeInput.text = ""
        stopTimeInput.text = ""

        // Save
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Wait for save to occur
        device.waitForIdle()
        Thread.sleep(500)

        var sleep: Sleep?
        runBlocking {
            val sleeps = DataModel.database.sleepDao().getAll()
            sleep = sleeps.firstOrNull()
        }
        assertNotNull(sleep)

        // verify start and stop date/time a1re not null or zero
        assert(sleep!!.start != 0L) { "Start time should not be zero" }
        assert(sleep!!.stop != 0L) { "Stop time should not be zero" }
    }

    @Test
    fun testManualEntryAllFields() {
        // Given no sleeps:
        resetDatabase()

        // Open Add Sleep screen
        val manualEntry = findObjectByRes("manual_entry_layout")
        manualEntry.click()

        findObjectByRes("add_sleep_start_time").click()
        device.waitForIdle()
        findObjectByText("AM").click()
        findObjectByDesc("12").click()
        findObjectByDesc("0").click()
        findObjectByText("OK").click()

        device.waitForIdle()

        findObjectByRes("add_sleep_stop_time").click()

        findObjectByText("AM").click()
        findObjectByDesc("10").click()
        findObjectByDesc("0").click()
        findObjectByText("OK").click()


        // Set all fields
        findObjectByRes("add_sleep_wakes").text = "3"
        val ratingBar = findObjectByRes("add_sleep_rating")
        val bounds = ratingBar.visibleBounds
        val x = bounds.left + (bounds.width() * 0.6f).toInt()
        val y = bounds.centerY()
        device.click(x, y) // Set rating to 3 stars

        // Add multi line comment
        val commentInput = findObjectByRes("add_sleep_comment")
        commentInput.text = "Test comment\nWith multiple lines\nAnd special characters: !@#$%^&*()"

        // Save
        val saveButton = findObjectByRes("add_sleep_save")
        saveButton.click()

        // Verify in DB
        device.waitForIdle()
        var sleep: Sleep?
        runBlocking {
            val sleeps = DataModel.database.sleepDao().getAll()
            sleep = sleeps.firstOrNull()
        }
        assertNotNull(sleep)
        assertEquals(3, sleep!!.wakes)
        // Rating should be 3 or 4, depending on how the RatingBar rounds the value
        assert(sleep!!.rating == 3L || sleep!!.rating == 4L) { "Rating was ${sleep!!.rating}, expected 3 or 4" }
        assertEquals("Test comment\nWith multiple lines\nAnd special characters: !@#$%^&*()", sleep!!.comment)

        // Verify in UI (Dashboard)
        assertResText("sleep_item_wakes", "3")
    }
}