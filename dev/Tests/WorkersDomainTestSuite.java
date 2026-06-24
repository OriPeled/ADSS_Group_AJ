package dev.Tests;

import dev.Workers.domain.*;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit 5 Test Suite for Workers domain logic.
 * This suite does not use Mockito or any external mocking libraries.
 * It tests pure in-memory calculations, registry constraints, and date behaviors.
 */
public class WorkersDomainTestSuite {

    private PreferenceHandler preferenceHandler;
    private RoleRegistry roleRegistry;
    private BranchRegistry branchRegistry;
    private AssignmentHandler assignmentHandler;
    private ShiftHandler shiftHandler;

    @BeforeEach
    public void setUp() {
        // Retrieve standard singleton instances without altering their inner DAO fields
        preferenceHandler = PreferenceHandler.getInstance();
        roleRegistry = RoleRegistry.getInstance();
        branchRegistry = BranchRegistry.getInstance();
        assignmentHandler = AssignmentHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();
    }

    // =========================================================================
    // PreferenceHandler Tests (Date & Deadline Logic)
    // =========================================================================

    /**
     * Verifies that a preference submission on Sunday is considered on time when the deadline is Thursday.
     * This tests the Israeli week structure logic implemented within PreferenceHandler.
     */
    @Test
    public void test01_IsOnTime_BeforeDeadline_ShouldReturnTrue() {
        // Set deadline to Thursday
        preferenceHandler.setDeadline(java.time.DayOfWeek.THURSDAY);

        // Calculate the upcoming Sunday (Sunday comes before Thursday in the Israeli week structure)
        LocalDate nextSunday =
                LocalDate.of(2026,1,4); // Sunday

        boolean onTime = preferenceHandler.isOnTime(nextSunday);
        assertTrue(onTime, "Submitting preferences on Sunday for a Thursday deadline should be on time.");
    }

    /**
     * Verifies that a preference submission on Friday is blocked (returns false) when the deadline is Thursday.
     */
    @Test
    public void test02_IsOnTime_AfterDeadline_ShouldReturnFalse() {
        // Set deadline to Thursday
        preferenceHandler.setDeadline(java.time.DayOfWeek.THURSDAY);

        // Calculate the upcoming Friday (Friday comes after Thursday, meaning it is late)
        LocalDate nextFriday =
                LocalDate.of(2026,1,9);

        boolean onTime = preferenceHandler.isOnTime(nextFriday);
        assertFalse(onTime, "Submitting preferences on Friday for a Thursday deadline should be late.");
    }

    /**
     * Verifies that updating the deadline works correctly in memory.
     */
    @Test
    public void test03_SetAndGetDeadline_ShouldUpdateSuccessfully() {
        preferenceHandler.setDeadline(java.time.DayOfWeek.TUESDAY);
        assertEquals(java.time.DayOfWeek.TUESDAY, preferenceHandler.getDeadline(),
                "The deadline day should be updated and retrieved correctly in memory.");
    }

    // =========================================================================
    // RoleRegistry Tests (Role Master List Constraints)
    // =========================================================================

    /**
     * Verifies that lookups in the RoleRegistry are case-insensitive.
     */
    @Test
    public void test04_GetRoleByName_CaseInsensitive_ShouldReturnSameRole() {
        Role roleLower = roleRegistry.getRoleByName("cashier");
        Role roleUpper = roleRegistry.getRoleByName("CASHIER");

        assertNotNull(roleLower, "The 'cashier' role should exist in the default configuration.");
        assertEquals(roleLower, roleUpper, "Role lookup must be case-insensitive.");
    }

    /**
     * Verifies that searching for a completely invalid role name throws a RuntimeException or IllegalArgumentException.
     */
    @Test
    public void test05_GetRoleByName_InvalidName_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> {
            roleRegistry.getRoleByName("NonExistentRole123");
        }, "Searching for a non-existent corporate role should throw an exception.");
    }

    /**
     * Verifies that the internal role counter returns a positive number of predefined roles.
     */
    @Test
    public void test06_GetRolesAmount_ShouldBeGreaterThanZero() {
        int amount = roleRegistry.getRolesAmount();
        assertTrue(amount > 0, "The registry should initialize with default corporate roles.");
    }

    // =========================================================================
    // BranchRegistry Tests (In-Memory Deduplication)
    // =========================================================================

    /**
     * Verifies that registering duplicate branch names (even with different casing) is prevented in memory.
     */
    @Test
    public void test07_RegisterBranch_PreventDuplicates_ShouldNotIncreaseSize() {
        int initialCount = branchRegistry.getBranchesAmount();

        Branch branch1 = new Branch("Haifa Center");
        Branch branch2 = new Branch("HAIFA CENTER"); // Duplicate in uppercase

        branchRegistry.registerBranch(branch1);
        branchRegistry.registerBranch(branch2);

        int finalCount = branchRegistry.getBranchesAmount();

        // Ensure the registry count increased by at most 1, preventing the duplicate layout
        assertTrue(finalCount <= initialCount + 1, "The branch registry should explicitly prevent duplicate names.");
    }

    // =========================================================================
    // ShiftHandler & AssignmentHandler Tests (In-Memory Statuses)
    // =========================================================================

    /**
     * Verifies that attempting to display a schedule that hasn't been published yet returns the correct warning string.
     */
    @Test
    public void test08_DisplayPublishedWeek_WhenScheduleNotPublished_ShouldReturnStatusMessage() {
        Branch branch = new Branch("Unpublished Branch");
        LocalDate futureDate =
                LocalDate.of(2030,1,1);
        String status = shiftHandler.displayPublishedWeek(branch, futureDate);

        assertEquals("Schedule not yet published.", status,
                "Should return the warning message if the schedule week state is unpublished.");
    }

    /**
     * Verifies that a new virtual branch with no past shifts returns a 'No history available' string.
     */
    @Test
    public void test09_ShiftHistory_WhenNoPastShifts_ShouldReturnNoHistoryMessage() {
        Branch temporaryBranch = new Branch("Brand New Virtual Branch");

        String history = shiftHandler.ShiftHistory(temporaryBranch);

        assertEquals("No history available.", history,
                "A branch with no recorded historical shifts should return the proper empty history message.");
    }

    /**
     * Verifies the string output format of AssignmentHandler when no assignments are set.
     */
    @Test
    public void test10_AssignmentHandler_ToString_ShouldNotBeNull() {
        String output = assignmentHandler.toString();
        assertNotNull(output, "The assignment output string should never be null.");
        assertFalse(output.trim().isEmpty(), "The output representation should return a valid string status.");
    }
}