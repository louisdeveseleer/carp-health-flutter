package cachet.plugins.health

import cachet.plugins.health.HealthDataReader.Companion.belongsToWindow
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BelongsToWindowTest {

    private fun at(time: String) = Instant.parse("2026-10-27T${time}:00Z")

    @Test
    fun aRideTouchingTheWalkByOneMinuteIsNotPartOfTheWalk() {
        // One app wrote a 63-minute ride and a 17-minute walk that starts a
        // minute before the ride ends.
        val walkStart = at("09:44")
        val walkEnd = at("10:01")

        assertFalse(belongsToWindow(at("08:42"), at("09:45"), walkStart, walkEnd))
        assertTrue(belongsToWindow(walkStart, walkEnd, walkStart, walkEnd))
    }

    @Test
    fun recordsInsideTheWindowBelong() {
        assertTrue(belongsToWindow(at("10:05"), at("10:10"), at("10:00"), at("11:00")))
    }

    @Test
    fun recordsMostlyInsideTheWindowBelong() {
        assertTrue(belongsToWindow(at("10:50"), at("11:02"), at("10:00"), at("11:00")))
    }

    @Test
    fun recordsMostlyOutsideTheWindowDoNot() {
        assertFalse(belongsToWindow(at("08:00"), at("12:00"), at("09:00"), at("10:00")))
        assertFalse(belongsToWindow(at("08:00"), at("09:00"), at("09:00"), at("10:00")))
    }

    @Test
    fun instantRecordsBelongWhenInsideTheWindow() {
        assertTrue(belongsToWindow(at("09:30"), at("09:30"), at("09:00"), at("10:00")))
        assertFalse(belongsToWindow(at("10:30"), at("10:30"), at("09:00"), at("10:00")))
    }
}
