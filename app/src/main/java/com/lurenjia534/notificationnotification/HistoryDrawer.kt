package com.lurenjia534.notificationnotification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime

@Composable
internal fun HistoryDrawer(
    drawerState: DrawerState,
    history: List<HistoryEntry>,
    cleanupSettings: HistoryCleanupSettings,
    onSaveCleanupSettings: (HistoryCleanupSettings) -> Unit,
    onClear: () -> Unit,
    content: @Composable () -> Unit,
) {
    val contentDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    var showCleanupSettings by rememberSaveable { mutableStateOf(false) }
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
                        HistoryList(
                            history = history,
                            drawerOpen = drawerState.isOpen,
                            onClear = onClear,
                            onOpenCleanupSettings = { showCleanupSettings = true },
                        )
                    }
                }
            },
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides contentDirection, content = content)
        }
    }

    if (showCleanupSettings) {
        HistoryCleanupDialog(
            settings = cleanupSettings,
            onDismiss = { showCleanupSettings = false },
            onSave = { settings ->
                onSaveCleanupSettings(settings)
                showCleanupSettings = false
            },
        )
    }
}

@Composable
private fun HistoryList(
    history: List<HistoryEntry>,
    drawerOpen: Boolean,
    onClear: () -> Unit,
    onOpenCleanupSettings: () -> Unit,
) {
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
        IconButton(onClick = onOpenCleanupSettings) {
            Icon(Icons.Default.Settings, contentDescription = "历史自动清理设置")
        }
        TextButton(onClick = onClear, enabled = history.isNotEmpty()) {
            Text("一键清空")
        }
    }

    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("暂无历史记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val now = produceState(ZonedDateTime.now(), drawerOpen) {
            value = ZonedDateTime.now()
            while (drawerOpen) {
                delay(1_000)
                value = ZonedDateTime.now()
            }
        }
        val today by remember(now) { derivedStateOf { now.value.toLocalDate() } }
        val zone by remember(now) { derivedStateOf { now.value.zone } }
        val historyByDay = remember(history, zone) { groupHistoryByDay(history, zone) }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for ((date, entries) in historyByDay) {
                item(key = date.toString()) {
                    Text(
                        text = formatHistoryDay(date, today),
                        modifier = Modifier
                            .padding(start = 4.dp, top = 4.dp)
                            .semantics { heading() },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(entries) { entry -> HistoryCard(entry, now) }
            }
        }
    }
}

@Composable
private fun HistoryCleanupDialog(
    settings: HistoryCleanupSettings,
    onDismiss: () -> Unit,
    onSave: (HistoryCleanupSettings) -> Unit,
) {
    var enabled by rememberSaveable { mutableStateOf(settings.enabled) }
    var daysText by rememberSaveable { mutableStateOf(settings.retentionDays.toString()) }
    val retentionDays = daysText.toIntOrNull()?.takeIf { it > 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("历史自动清理") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = enabled, role = Role.Switch, onValueChange = { enabled = it }),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("自动清理过期历史")
                    Switch(checked = enabled, onCheckedChange = null)
                }
                if (enabled) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = daysText,
                        onValueChange = { value ->
                            if (value.all(Char::isDigit)) daysText = value
                        },
                        label = { Text("保留天数") },
                        singleLine = true,
                        isError = daysText.isNotEmpty() && retentionDays == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(HistoryCleanupSettings(enabled, retentionDays ?: settings.retentionDays)) },
                enabled = !enabled || retentionDays != null,
            ) {
                Text("保存")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun HistoryCard(entry: HistoryEntry, now: State<ZonedDateTime>) {
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
            HistoryTimestamp(entry.createdAt, now)
        }
    }
}

@Composable
private fun HistoryTimestamp(createdAt: Long, now: State<ZonedDateTime>) {
    val displayTime = now.value
    val time = remember(createdAt, displayTime.zone) {
        Instant.ofEpochMilli(createdAt).atZone(displayTime.zone)
    }
    val relative = formatHistoryRelativeTime(time, displayTime)
    val absolute = remember(time) { formatHistoryAbsoluteTime(time) }
    val absoluteTimeColor = if (isSystemInDarkTheme()) Color(0xFF999999) else Color(0xFF666666)
    val label = remember(relative, absolute, absoluteTimeColor) {
        buildAnnotatedString {
            append(relative)
            append('\u00A0') // Keep the opening parenthesis next to the relative time.
            withStyle(SpanStyle(color = absoluteTimeColor)) {
                append("($absolute)")
            }
        }
    }
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
