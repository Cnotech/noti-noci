package com.lurenjia534.notificationnotification

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NotificationHistoryTest {
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val testPreferencesName = "NotificationHistoryTest"
    private lateinit var preferences: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setUp() {
        appContext.deleteSharedPreferences(testPreferencesName)
        preferences = appContext.getSharedPreferences(testPreferencesName, Context.MODE_PRIVATE)
        context = object : ContextWrapper(appContext) {
            override fun getSharedPreferences(name: String, mode: Int) = preferences
        }
    }

    @After
    fun tearDown() {
        appContext.deleteSharedPreferences(testPreferencesName)
    }

    @Test
    fun cleanupDefaultsToOffAndPreservesExistingHistory() {
        seedHistory(90, 2)
        val history = NotificationHistory(context)

        assertEquals(HistoryCleanupSettings(enabled = false, retentionDays = 30), history.cleanupSettings())
        history.clearExpired()

        assertEquals(listOf("2", "90"), history.read().map { it.title })
    }

    @Test
    fun backgroundCleanupDeletesOnlyExpiredHistoryWithDefaultRetention() {
        seedHistory(31, 29, 1)
        val history = NotificationHistory(context)
        history.saveCleanupSettings(history.cleanupSettings().copy(enabled = true))

        NotificationHistory(context).clearExpired()
        history.saveCleanupSettings(history.cleanupSettings().copy(enabled = false))

        assertEquals(listOf("1", "29"), NotificationHistory(context).read().map { it.title })
    }

    @Test
    fun customRetentionIsAppliedOnReadAndSurvivesClearingHistory() {
        seedHistory(8, 6)
        val settings = HistoryCleanupSettings(enabled = true, retentionDays = 7)
        NotificationHistory(context).saveCleanupSettings(settings)

        val history = NotificationHistory(context)
        assertEquals(listOf("6"), history.read().map { it.title })
        history.clear()

        assertEquals(settings, NotificationHistory(context).cleanupSettings())
    }

    @Test
    fun deletingOneLegacyDuplicatePreservesOtherRecords() {
        seedHistory(8, 6, 8)
        val history = NotificationHistory(context).read()
        assertNotEquals(history.first().id, history.last().id)

        val reopenedHistory = NotificationHistory(context)
        assertEquals(history, reopenedHistory.read())
        reopenedHistory.delete(history.last().id)
        reopenedHistory.delete(history.last().id)

        assertEquals(history.dropLast(1), reopenedHistory.read())
    }

    private fun seedHistory(vararg agesInDays: Int) {
        val now = System.currentTimeMillis()
        val entries = JSONArray()
        for (age in agesInDays) {
            entries.put(
                JSONObject()
                    .put("title", age.toString())
                    .put("content", "content")
                    .put("createdAt", now - TimeUnit.DAYS.toMillis(age.toLong()))
            )
        }
        preferences.edit().putString("entries", entries.toString()).apply()
    }
}
