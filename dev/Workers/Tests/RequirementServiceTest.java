package dev.Workers.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.service.RequirementService;
import dev.Workers.service.ShiftService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for RequirementService.
 */
class RequirementServiceTest {
    private RequirementService requirementService;
    private ShiftService shiftService;
    private RequirementHandler requirementHandler;

    private Branch branch;
    private Role cashierRole;
    private Role storekeeperRole;

    @BeforeEach
    void setUp() {
        DatabaseManager.eraseDatabase();
        DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR IGNORE INTO branches (branch_name) VALUES ('Beer-Sheva');");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup test database branches", e);
        }

        BranchRegistry registry = BranchRegistry.getInstance();
        registry.registerBranch(new Branch("Beer-Sheva"));
        branch = registry.getBranchByName("Beer-Sheva");

        requirementService = RequirementService.getInstance();
        shiftService = ShiftService.getInstance();
        requirementHandler = RequirementHandler.getInstance();

        cashierRole = RoleRegistry.getInstance().getRoleByName("Cashier");
        storekeeperRole = RoleRegistry.getInstance().getRoleByName("Storekeeper");

        shiftService.initShiftsWeek(branch);
    }

    /**
     * Verifies that cashier requirements are initialized.
     */
    @Test
    void initCashierRequirement_shouldSucceed() {
        requirementService.initWeeklyReqs(
                "Cashier",
                branch,
                2);

        Shift shift = shiftService.getShift(
                branch,
                LocalDate.now().with(DayOfWeek.SUNDAY),
                ShiftType.MORNING);

        assertEquals(
                2,
                requirementHandler.countRequired(
                        shift,
                        cashierRole));
    }



    /**
     * Verifies that requirements can be updated.
     */
    @Test
    void updateRequirement_shouldSucceed() {

        requirementService.initWeeklyReqs(
                "Cashier",
                branch,
                2);

        requirementService.initWeeklyReqs(
                "Cashier",
                branch,
                5);

        Shift shift = shiftService.getShift(
                branch,
                LocalDate.now().with(DayOfWeek.SUNDAY),
                ShiftType.MORNING);

        assertEquals(
                5,
                requirementHandler.countRequired(
                        shift,
                        cashierRole));
    }

    /**
     * Verifies that negative requirement fails.
     */
    @Test
    void negativeRequirement_shouldFail() {

        Shift shift = shiftService.getShift(
                branch,
                LocalDate.now().with(DayOfWeek.SUNDAY),
                ShiftType.MORNING);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.setRequirement(
                        shift,
                        cashierRole,
                        -1));
    }

    /**
     * Verifies that zero requirement is allowed.
     */
    @Test
    void zeroRequirement_shouldBeAllowed() {

        requirementService.initWeeklyReqs(
                "Cashier",
                branch,
                0);

        Shift shift = shiftService.getShift(
                branch,
                LocalDate.now().with(DayOfWeek.SUNDAY),
                ShiftType.MORNING);

        assertEquals(
                0,
                requirementHandler.countRequired(
                        shift,
                        cashierRole));
    }

    /**
     * Verifies that requirements exist after initialization.
     */
    @Test
    void requirementsShouldExistAfterInitialization() {

        requirementService.initWeeklyReqs(
                "Cashier",
                branch,
                3);

        Shift shift = shiftService.getShift(
                branch,
                LocalDate.now().with(DayOfWeek.SUNDAY),
                ShiftType.MORNING);

        assertTrue(
                requirementHandler.countRequired(
                        shift,
                        cashierRole) > 0);
    }

}