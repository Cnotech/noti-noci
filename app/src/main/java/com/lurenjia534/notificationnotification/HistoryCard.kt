package com.lurenjia534.notificationnotification

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.roundToInt

private enum class HistoryCardPosition { Closed, DeleteRevealed }

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HistoryCard(entry: HistoryEntry, now: State<ZonedDateTime>, onDelete: () -> Unit) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val deleteButtonSize = 48.dp
    val deleteButtonInset = 8.dp
    val revealWidth = deleteButtonSize + deleteButtonInset * 2
    val swipe = remember(density) {
        AnchoredDraggableState(
            initialValue = HistoryCardPosition.Closed,
            anchors = DraggableAnchors {
                HistoryCardPosition.Closed at 0f
                HistoryCardPosition.DeleteRevealed at -with(density) { revealWidth.toPx() }
            },
            positionalThreshold = { distance -> distance * 0.5f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            animationSpec = tween(durationMillis = 180),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .anchoredDraggable(state = swipe, orientation = Orientation.Horizontal, reverseDirection = false)
            .semantics {
                if (swipe.currentValue == HistoryCardPosition.Closed) {
                    customActions = listOf(CustomAccessibilityAction("显示删除按钮") {
                        scope.launch { swipe.animateTo(HistoryCardPosition.DeleteRevealed) }
                        true
                    })
                }
            },
    ) {
        if (swipe.requireOffset() < 0f) {
            Box(Modifier.matchParentSize(), contentAlignment = AbsoluteAlignment.CenterRight) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .absolutePadding(right = deleteButtonInset)
                        .size(deleteButtonSize),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .absoluteOffset { IntOffset(swipe.requireOffset().roundToInt(), 0) },
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (entry.title.isNotEmpty()) {
                    CopyableHistoryText(entry.title, "标题", MaterialTheme.typography.titleMedium)
                }
                if (entry.content.isNotEmpty()) {
                    CopyableHistoryText(entry.content, "内容", MaterialTheme.typography.bodyMedium)
                }
                HistoryTimestamp(entry.createdAt, now)
            }
        }
    }
}

@Composable
private fun CopyableHistoryText(text: String, fieldName: String, style: TextStyle) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Text(
        text = text,
        modifier = Modifier.clickable(onClickLabel = "复制$fieldName") {
            clipboard.setText(AnnotatedString(text))
            Toast.makeText(context, "已复制$fieldName", Toast.LENGTH_SHORT).show()
        },
        style = style,
    )
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
