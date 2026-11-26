package com.lurenjia534.notificationnotification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

internal data class HistoryEntry(
    val title: String,
    val content: String,
    val createdAt: Long,
)

internal data class HistoryCleanupSettings(
    val enabled: Boolean,
    val retentionDays: Int,
) {
    companion object {
        const val DEFAULT_RETENTION_DAYS = 30
    }
}

internal class NotificationHistory(context: Context) {
    private val preferences = context.getSharedPreferences("NotificationHistory", Context.MODE_PRIVATE)

    fun add(title: String, content: String) = synchronized(lock) {
        val entries = readEntries()
        val now = System.currentTimeMillis()
        val lastIndex = entries.length() - 1
        val lastEntry = entries.optJSONObject(lastIndex)
        val entry = JSONObject()
            .put("title", title)
            .put("content", content)
            .put("createdAt", now)

        if (lastEntry != null && lastEntry.getString("title") == title &&
            lastEntry.getString("content") == content
        ) {
            entries.put(lastIndex, entry)
        } else {
            entries.put(entry)
        }
        writeEntries(unexpiredEntries(entries, now))
    }

    fun read(): List<HistoryEntry> = synchronized(lock) {
        val entries = pruneExpiredEntries()
        buildList {
            for (index in entries.length() - 1 downTo 0) {
                val entry = entries.getJSONObject(index)
                add(
                    HistoryEntry(
                        title = entry.getString("title"),
                        content = entry.getString("content"),
                        createdAt = entry.getLong("createdAt"),
                    )
                )
            }
        }
    }

    fun clear() = synchronized(lock) {
        preferences.edit().remove(ENTRIES_KEY).apply()
    }

    fun cleanupSettings() = HistoryCleanupSettings(
        enabled = preferences.getBoolean(CLEANUP_ENABLED_KEY, false),
        retentionDays = preferences.getInt(RETENTION_DAYS_KEY, HistoryCleanupSettings.DEFAULT_RETENTION_DAYS),
    )

    fun saveCleanupSettings(settings: HistoryCleanupSettings) = synchronized(lock) {
        require(settings.retentionDays > 0)
        preferences.edit()
            .putBoolean(CLEANUP_ENABLED_KEY, settings.enabled)
            .putInt(RETENTION_DAYS_KEY, settings.retentionDays)
            .apply()
    }

    fun clearExpired() {
        synchronized(lock) {
            if (cleanupSettings().enabled) {
                pruneExpiredEntries()
            }
        }
    }

    private fun pruneExpiredEntries(): JSONArray {
        val entries = readEntries()
        val unexpired = unexpiredEntries(entries, System.currentTimeMillis())
        if (unexpired.length() != entries.length()) {
            writeEntries(unexpired)
        }
        return unexpired
    }

    private fun unexpiredEntries(entries: JSONArray, now: Long): JSONArray {
        val settings = cleanupSettings()
        if (!settings.enabled) return entries

        val cutoff = now - TimeUnit.DAYS.toMillis(settings.retentionDays.toLong())
        val unexpired = JSONArray()
        for (index in 0 until entries.length()) {
            val entry = entries.getJSONObject(index)
            if (entry.getLong("createdAt") >= cutoff) {
                unexpired.put(entry)
            }
        }
        return unexpired
    }

    private fun readEntries(): JSONArray = JSONArray(preferences.getString(ENTRIES_KEY, "[]"))

    private fun writeEntries(entries: JSONArray) {
        preferences.edit().putString(ENTRIES_KEY, entries.toString()).apply()
    }

    private companion object {
        const val ENTRIES_KEY = "entries"
        const val CLEANUP_ENABLED_KEY = "cleanupEnabled"
        const val RETENTION_DAYS_KEY = "retentionDays"
        val lock = Any()
    }
}
