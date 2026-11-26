package com.lurenjia534.notificationnotification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalTime
import java.time.ZonedDateTime

private const val ACTION_REFRESH_NOTIFICATION = "com.cnotech.notinoci.action.REFRESH_NOTIFICATION"
private val refreshTime = LocalTime.of(8, 0)

internal class DailyNotificationRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifications = context.getSystemService(NotificationManager::class.java)
        val note = notifications.activeNote()
        val noteIsSaved = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).contains(TITLE_KEY)
        if (note == null || !noteIsSaved || !notifications.areNotificationsEnabled()) {
            cancelNotificationRefresh(context)
            return
        }

        if (intent.action == ACTION_REFRESH_NOTIFICATION) {
            note.`when` = System.currentTimeMillis()
            notifications.cancel(NOTE_NOTIFICATION_ID)
            notifications.notify(NOTE_NOTIFICATION_ID, note)
        }
        scheduleNotificationRefresh(context)
    }
}

internal fun nextNotificationRefreshAt(now: ZonedDateTime): ZonedDateTime {
    val date = now.toLocalDate()
    val today = date.atTime(refreshTime).atZone(now.zone)
    return if (today.isAfter(now)) today else date.plusDays(1).atTime(refreshTime).atZone(now.zone)
}

internal fun scheduleNotificationRefresh(context: Context) {
    val alarm = context.getSystemService(AlarmManager::class.java)
    val operation = refreshPendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
    val time = nextNotificationRefreshAt(ZonedDateTime.now()).toInstant().toEpochMilli()
    alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, operation)
}

internal fun cancelNotificationRefresh(context: Context) {
    val operation = refreshPendingIntent(context, PendingIntent.FLAG_NO_CREATE) ?: return
    context.getSystemService(AlarmManager::class.java).cancel(operation)
    operation.cancel()
}

private fun NotificationManager.activeNote(): Notification? = activeNotifications
    .firstOrNull { it.id == NOTE_NOTIFICATION_ID && it.tag == null }
    ?.notification

private fun refreshPendingIntent(context: Context, flags: Int): PendingIntent? =
    PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, DailyNotificationRefreshReceiver::class.java).setAction(ACTION_REFRESH_NOTIFICATION),
        PendingIntent.FLAG_IMMUTABLE or flags,
    )
