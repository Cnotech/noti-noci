package com.lurenjia534.notificationnotification

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun HistoryDrawer(
    drawerState: DrawerState,
    history: List<HistoryEntry>,
    onClear: () -> Unit,
    content: @Composable () -> Unit,
) {
    val contentDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // The Material drawer opens from the start edge. Mirror the drawer placement and gesture.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .widthIn(max = DrawerDefaults.MaximumDrawerWidth)
                        .fillMaxWidth(0.9f),
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                        HistoryList(history, onClear)
                    }
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides contentDirection, content = content)
        }
    }
}

@Composable
private fun HistoryList(history: List<HistoryEntry>, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "历史记录",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
        )
        TextButton(onClick = onClear, enabled = history.isNotEmpty()) {
            Text("一键清空")
        }
    }

    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(history) { entry -> HistoryCard(entry) }
        }
    }
}

@Composable
private fun HistoryCard(entry: HistoryEntry) {
    val time = DateUtils.formatDateTime(
        LocalContext.current,
        entry.createdAt,
        DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME,
    )
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (entry.title.isNotEmpty()) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
            }
            if (entry.content.isNotEmpty()) {
                Text(entry.content, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
