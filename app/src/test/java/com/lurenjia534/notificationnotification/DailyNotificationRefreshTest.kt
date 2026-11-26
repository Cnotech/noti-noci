package com.lurenjia534.notificationnotification

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZonedDateTime

class DailyNotificationRefreshTest {
    @Test
    fun schedulesTodayBeforeEightAndTomorrowAtOrAfterEight() {
        val morning = ZonedDateTime.parse("2026-11-26T08:00:00+08:00[Asia/Shanghai]")

        assertEquals(morning, nextNotificationRefreshAt(morning.minusSeconds(1)))
        assertEquals(morning.plusDays(1), nextNotificationRefreshAt(morning))
        assertEquals(morning.plusDays(1), nextNotificationRefreshAt(morning.plusHours(1)))
    }

    @Test
    fun keepsEightAsLocalTimeWhenTheUtcOffsetChanges() {
        val now = ZonedDateTime.parse("2026-03-07T20:00:00-05:00[America/New_York]")
        val tomorrow = ZonedDateTime.parse("2026-03-08T08:00:00-04:00[America/New_York]")

        assertEquals(tomorrow, nextNotificationRefreshAt(now))
    }
}
