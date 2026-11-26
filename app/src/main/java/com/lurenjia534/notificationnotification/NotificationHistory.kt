package com.lurenjia534.notificationnotification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal data class HistoryEntry(
    val title: String,
    val content: String,
    val createdAt: Long,
)

internal class NotificationHistory(context: Context) {
    private val preferences = context.getSharedPreferences("NotificationHistory", Context.MODE_PRIVATE)

    fun add(title: String, content: String) {
        val entries = readEntries()
        val lastIndex = entries.length() - 1
        val lastEntry = entries.optJSONObject(lastIndex)
        val entry = JSONObject()
            .put("title", title)
            .put("content", content)
            .put("createdAt", System.currentTimeMillis())

        if (lastEntry != null && lastEntry.getString("title") == title &&
            lastEntry.getString("content") == content
        ) {
            entries.put(lastIndex, entry)
        } else {
            entries.put(entry)
        }
        preferences.edit().putString(ENTRIES_KEY, entries.toString()).apply()
    }

    fun read(): List<HistoryEntry> {
        val entries = readEntries()
        return buildList {
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

    fun clear() {
        preferences.edit().remove(ENTRIES_KEY).apply()
    }

    private fun readEntries(): JSONArray = JSONArray(preferences.getString(ENTRIES_KEY, "[]"))

    private companion object {
        const val ENTRIES_KEY = "entries"
    }
}
