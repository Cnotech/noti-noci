package com.lurenjia534.notificationnotification

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat


// 返回桌面
fun backHome(context: Context) {
    Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }.let { ContextCompat.startActivity(context, it, null) }
}