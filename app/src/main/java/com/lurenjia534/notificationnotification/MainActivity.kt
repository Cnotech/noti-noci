package com.lurenjia534.notificationnotification

import android.os.Bundle
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Button
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

@Composable
@Preview
fun AppUI() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column {
            var title by remember { mutableStateOf(TextFieldValue(read(context, TITLE_KEY))) }
            var content by remember { mutableStateOf(TextFieldValue(read(context, CONTENT_KEY))) }
            OutlinedCard(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    Modifier
                        .padding(start = 30.dp, end = 30.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = title, onValueChange = { newText -> title = newText },
                        label = { Text("标题") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Row(
                    Modifier
                        .padding(start = 30.dp, end = 30.dp)
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = content, onValueChange = { newText -> content = newText },
                        label = { Text("内容") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Create,
                                contentDescription = null
                            )
                        },
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .padding(start = 30.dp, end = 30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            title = TextFieldValue("")
                            content = TextFieldValue("")
                            clearNotification(context)
                        },
                        modifier = Modifier.padding(bottom = 10.dp),
                        enabled = title.text.isNotEmpty() || content.text.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = null
                        )
                        Text(text = "清除", fontWeight = FontWeight.Bold)
                    }
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