package com.lurenjia534.notificationnotification

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

fun saveNotification(context: Context, title: String, message: String) {
    val sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(TITLE_KEY, title)
    editor.putString(CONTENT_KEY, message)
    editor.apply()
}

fun sendNotification(context: Context, title: String, message: String) {

    saveNotification(context, title, message)

    val deleteIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NotificationReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    if (!areNotificationsEnabled) {
        AlertDialog.Builder(context)
            .setTitle("请打开通知权限")
            .setMessage("需要获取通知权限才能正常运行，点击确定跳转到设置界面")
            .setPositiveButton("确定") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                ContextCompat.startActivity(context, intent, null)
            }
            .setNegativeButton("取消", null)
            .show()
        return
    }

    val notificationManager = ContextCompat.getSystemService(
        context, NotificationManager::class.java
    ) as NotificationManager

    // 创建一个通知通道
    val channelId = "Notice_Memo"
    val channelName = "Notice_Memo"
    val channel = NotificationChannel(
        channelId, channelName, NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)

    // 配置通知点击意图
    val notificationIntent = Intent(context, MainActivity::class.java)
    val contentIntent = PendingIntent.getActivity(
        context, 0, notificationIntent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // 构建通知
    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // 设置通知图标
        .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置优先级
        .setOngoing(true)
        .setContentIntent(contentIntent)
        .setDeleteIntent(deleteIntent)
        .build()

    // 显示通知
    notificationManager.notify(1001, notification)

    // 返回桌面
    Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }.let { ContextCompat.startActivity(context, it, null) }
}
