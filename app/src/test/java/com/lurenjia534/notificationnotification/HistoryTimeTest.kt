package com.lurenjia534.notificationnotification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class HistoryTimeTest {
    private val now = ZonedDateTime.parse("2026-11-26T12:00:00+08:00")

    @Test
    fun historyDayUsesRelativeLabelsThroughSevenDays() {
        val today = now.toLocalDate()
        assertEquals("今天", formatHistoryDay(today, today))
        assertEquals("昨天", formatHistoryDay(today.minusDays(1), today))
        assertEquals("2 天前", formatHistoryDay(today.minusDays(2), today))
        assertEquals("7 天前", formatHistoryDay(today.minusDays(7), today))
        assertEquals("2026年11月18日", formatHistoryDay(today.minusDays(8), today))
    }

    @Test
    fun historyGroupsUseLocalMidnightAndListNewestDaysFirst() {
        val yesterday = HistoryEntry("Yesterday", "", Instant.parse("2026-11-25T15:59:59Z").toEpochMilli())
        val firstToday = HistoryEntry("First today", "", Instant.parse("2026-11-25T16:00:00Z").toEpochMilli())
        val latestToday = HistoryEntry("Latest today", "", Instant.parse("2026-11-26T01:00:00Z").toEpochMilli())
        val groups = groupHistoryByDay(listOf(latestToday, yesterday, firstToday), ZoneId.of("Asia/Shanghai"))

        assertEquals(listOf(LocalDate.of(2026, 11, 26), LocalDate.of(2026, 11, 25)), groups.keys.toList())
        assertEquals(listOf(listOf(latestToday, firstToday), listOf(yesterday)), groups.values.toList())
    }

    @Test
    fun relativeTimeUsesOneMinuteForJustNowAndRoundsLargerUnits() {
        val examples = listOf(
            now.minusSeconds(59) to "刚刚",
            now.minusMinutes(1) to "1分钟前",
            now.minusSeconds(90) to "2分钟前",
            now.minusMinutes(44).minusSeconds(29) to "44分钟前",
            now.minusMinutes(44).minusSeconds(30) to "1小时前",
            now.minusMinutes(90) to "2小时前",
            now.minusHours(21).minusMinutes(30) to "1天前",
            now.minusHours(36) to "2天前",
            now.minusDays(25).minusHours(12) to "1个月前",
            now.minusMonths(6) to "6个月前",
            now.minusMonths(11) to "1年前",
            now.minusMonths(18) to "2年前",
            now.plusHours(3) to "3小时后",
        )

        for ((time, expected) in examples) {
            assertEquals(time.toString(), expected, formatHistoryRelativeTime(time, now))
        }
    }

    @Test
    fun relativeTimeHandlesShortMonthsAndDaylightSavingTime() {
        val january = ZonedDateTime.parse("2026-01-31T12:00:00+08:00")
        val march = ZonedDateTime.parse("2026-03-18T12:00:00+08:00")
        assertEquals("2个月前", formatHistoryRelativeTime(january, march))

        val beforeClockChange = ZonedDateTime.parse("2026-03-07T00:00:00-05:00[America/New_York]")
        val afterClockChange = beforeClockChange.plusDays(3).plusHours(12)
        assertEquals("4天前", formatHistoryRelativeTime(beforeClockChange, afterClockChange))
    }

    @Test
    fun absoluteTimeUsesTheDeviceZoneAndTwentyFourHourClock() {
        val time = Instant.parse("2026-01-02T07:04:00Z").atZone(ZoneId.of("Asia/Shanghai"))
        assertEquals("2026年1月2日 15:04", formatHistoryAbsoluteTime(time))
    }
}
