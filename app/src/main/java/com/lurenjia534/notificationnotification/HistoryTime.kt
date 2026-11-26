package com.lurenjia534.notificationnotification

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

private const val MILLIS_PER_MINUTE = 60_000.0
private const val MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE
private const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR
private val absoluteTimeFormatter = DateTimeFormatter.ofPattern("uuuu年M月d日 HH:mm", Locale.ROOT)
private val absoluteDateFormatter = DateTimeFormatter.ofPattern("uuuu年M月d日", Locale.ROOT)

internal fun groupHistoryByDay(history: List<HistoryEntry>, zone: ZoneId): Map<LocalDate, List<HistoryEntry>> =
    history.groupBy { entry -> Instant.ofEpochMilli(entry.createdAt).atZone(zone).toLocalDate() }
        .toSortedMap(reverseOrder())

internal fun formatHistoryDay(date: LocalDate, today: LocalDate): String {
    val daysAgo = ChronoUnit.DAYS.between(date, today)
    return when (daysAgo) {
        0L -> "今天"
        1L -> "昨天"
        in 2L..7L -> "$daysAgo 天前"
        else -> absoluteDateFormatter.format(date)
    }
}

internal fun formatHistoryAbsoluteTime(time: ZonedDateTime): String = absoluteTimeFormatter.format(time)

internal fun formatHistoryRelativeTime(time: ZonedDateTime, now: ZonedDateTime): String {
    val isFuture = time.isAfter(now)
    val start = if (isFuture) now else time
    val end = if (isFuture) time else now
    val elapsedMillis = Duration.between(start, end).toMillis()
    if (elapsedMillis < MILLIS_PER_MINUTE) return "刚刚"

    val suffix = if (isFuture) "后" else "前"
    val minutes = (elapsedMillis / MILLIS_PER_MINUTE).roundToLong()
    if (minutes <= 44) return "${minutes}分钟$suffix"

    val hours = (elapsedMillis / MILLIS_PER_HOUR).roundToLong()
    if (hours <= 21) return "${hours.coerceAtLeast(1)}小时$suffix"
    if (hours <= 35) return "1天$suffix"

    val localElapsedMillis = Duration.between(start.toLocalDateTime(), end.toLocalDateTime()).toMillis()
    val days = (localElapsedMillis / MILLIS_PER_DAY).roundToLong()
    if (days <= 25) return "${days.coerceAtLeast(1)}天$suffix"
    if (days <= 45) return "1个月$suffix"

    val months = monthsBetween(start, end)
    val roundedMonths = months.roundToLong()
    if (roundedMonths <= 10) return "${roundedMonths.coerceAtLeast(1)}个月$suffix"
    if (roundedMonths <= 17) return "1年$suffix"
    return "${(months / 12).roundToLong()}年$suffix"
}

private fun monthsBetween(start: ZonedDateTime, end: ZonedDateTime): Double {
    // Starting from the larger day of the month preserves end-of-month anniversaries.
    if (start.dayOfMonth < end.dayOfMonth) return -monthsBetween(end, start)

    val months = ChronoUnit.MONTHS.between(YearMonth.from(start), YearMonth.from(end))
    val anniversary = start.plusMonths(months)
    val remainder = Duration.between(anniversary, end).toMillis()
    val adjacentAnniversary = start.plusMonths(months + if (remainder < 0) -1 else 1)
    val monthLength = abs(Duration.between(anniversary, adjacentAnniversary).toMillis())
    return months + remainder.toDouble() / monthLength
}
