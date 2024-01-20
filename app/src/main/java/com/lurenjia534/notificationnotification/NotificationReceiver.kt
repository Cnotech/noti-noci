package com.lurenjia534.notificationnotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 从 SharedPreferences 获取之前的通知内容
        val sharedPreferences =
            context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        val title = sharedPreferences.getString(TITLE_KEY, null)
        val message = sharedPreferences.getString(CONTENT_KEY, null)

        if (title != null && message != null) {
            sendNotification(context, title, message)
        }

    }
}