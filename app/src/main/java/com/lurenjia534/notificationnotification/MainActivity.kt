package com.lurenjia534.notificationnotification

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.lurenjia534.notificationnotification.ui.theme.NotificationNotificationTheme

const val PREF_KEY = "NotificationPrefs"
const val TITLE_KEY = "title"
const val CONTENT_KEY = "content"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationNotificationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    AppUI()
                }
            }
        }
    }
}

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
                startActivity(context, intent, null)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun AppUI() {
    // 获取之前的内容
    val context = LocalContext.current
    val sharedPreferences =
        context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    val titleStr = sharedPreferences.getString(TITLE_KEY, "") ?: ""
    val contentStr = sharedPreferences.getString(CONTENT_KEY, "") ?: ""

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column {
            var title by remember { mutableStateOf(TextFieldValue(titleStr)) }
            var content by remember { mutableStateOf(TextFieldValue(contentStr)) }
            OutlinedCard(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.9f), colors = CardDefaults.outlinedCardColors()
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Row {
                    Spacer(modifier = Modifier.width(50.dp))
                    OutlinedTextField(
                        value = title, onValueChange = { newText -> title = newText },
                        label = { Text("标题") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Create,
                                contentDescription = null
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row {
                    Spacer(modifier = Modifier.width(50.dp))
                    OutlinedTextField(
                        value = content, onValueChange = { newText -> content = newText },
                        label = { Text("内容") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Spacer(modifier = Modifier.width(130.dp))
                    Button(
                        onClick = {
                            sendNotification(context, title.text, content.text)
                        },
                        modifier = Modifier.padding(bottom = 10.dp),
                        enabled = title.text.isNotEmpty() || content.text.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null
                        )
                        Text(text = "确定", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}