package cachet.plugins.health

import cachet.plugins.health.HealthDataReader.Companion.overlapFraction
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlapFractionTest {

    private fun at(time: String) = Instant.parse("2026-10-27T${time}:00Z")

    @Test
    fun aRideTouchingTheWalkByOneMinuteAddsOnlyThatMinute() {
        // One app wrote a 63-minute ride (24.772 km) and a 17-minute walk
        // (65 m) that starts a minute before the ride ends.
        val walkStart = at("09:44")
        val walkEnd = at("10:01")
        val ride = 24772.0 * overlapFraction(at("08:42"), at("09:45"), walkStart, walkEnd)
        val walk = 65.0 * overlapFraction(walkStart, walkEnd, walkStart, walkEnd)

        assertEquals(24772.0 / 63 + 65.0, ride + walk, 0.001)
    }

    @Test
    fun recordsInsideTheWindowCountInFull() {
        assertEquals(1.0, overlapFraction(at("10:05"), at("10:10"), at("10:00"), at("11:00")), 0.0)
    }

    @Test
    fun recordsSpanningTheWindowCountForTheWindowOnly() {
        assertEquals(0.25, overlapFraction(at("08:00"), at("12:00"), at("09:00"), at("10:00")), 0.0)
    }

    @Test
    fun recordsOutsideTheWindowCountForNothing() {
        assertEquals(0.0, overlapFraction(at("08:00"), at("09:00"), at("09:00"), at("10:00")), 0.0)
    }

    @Test
    fun instantRecordsCountWhenInsideTheWindow() {
        assertEquals(1.0, overlapFraction(at("09:30"), at("09:30"), at("09:00"), at("10:00")), 0.0)
        assertEquals(0.0, overlapFraction(at("10:30"), at("10:30"), at("09:00"), at("10:00")), 0.0)
    }
}
