package dev.Tests;

import dev.Workers.domain.PreferenceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PreferenceHandler timing logic.
 */
class PreferenceHandlerTest {
    private PreferenceHandler preferenceHandler;

    @BeforeEach
    void setUp() {
        preferenceHandler = PreferenceHandler.getInstance();
    }

    /**
     * Verifies that the default deadline is Thursday.
     */
    @Test
    void defaultDeadline_shouldBeThursday() {
        assertEquals(
                DayOfWeek.THURSDAY,
                preferenceHandler.getDeadline());
    }

    /**
     * Verifies that changing the deadline succeeds.
     */
    @Test
    void setDeadline_shouldSucceed() {
        preferenceHandler.setDeadline(DayOfWeek.TUESDAY);
        assertEquals(
                DayOfWeek.TUESDAY,
                preferenceHandler.getDeadline());
    }

    /**
     * Verifies that a date before the deadline is considered on time.
     */
    @Test
    void isOnTime_beforeDeadline_shouldReturnTrue() {
        preferenceHandler.setDeadline(DayOfWeek.THURSDAY);
        assertTrue(
                preferenceHandler.isOnTime(
                        LocalDate.of(2025, 1, 1))); // Wednesday
    }

    /**
     * Verifies that a date after the deadline is considered late.
     */
    @Test
    void isOnTime_afterDeadline_shouldReturnFalse() {
        preferenceHandler.setDeadline(DayOfWeek.THURSDAY);
        assertFalse(
                preferenceHandler.isOnTime(
                        LocalDate.of(2025, 1, 3))); // Friday
    }

    /**
     * Verifies that changing the deadline affects the result.
     */
    @Test
    void changingDeadline_shouldAffectOnTimeLogic() {
        preferenceHandler.setDeadline(DayOfWeek.MONDAY);
        assertFalse(
                preferenceHandler.isOnTime(
                        LocalDate.of(2025, 1, 7))); // Tuesday
    }

}