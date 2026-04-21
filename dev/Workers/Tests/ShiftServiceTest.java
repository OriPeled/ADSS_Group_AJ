package dev.Workers.Tests;

import dev.Workers.Service.ShiftService;
import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.RoleManager;
import dev.Workers.domain.ShiftManager;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.WeekSchedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.Set;

import static dev.Workers.domain.Enums.ShiftType.morning;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShiftService week publishing functionality.
 *
 * This class verifies that the service correctly publishes
 * different weeks and correctly delegates scheduling logic.
 *
 * Important note:
 * The system uses singleton managers, so each test must start
 * from a clean state in order to avoid interference from previous tests.
 */
public class ShiftServiceTest {

    /**
     * Resets all singleton managers before each test.
     *
     * This prevents shared state from one test affecting another.
     */
    @BeforeEach
    void resetSystem() throws Exception {
        clearEmployeeManager();
        clearRoleManager();
        clearConstraintManager();
        clearShiftManager();
    }

    /**
     * Registers an employee in all required managers.
     *
     * This helper method:
     * - adds the employee to EmployeeManager
     * - assigns the given role in RoleManager
     * - initializes the employee constraints in ConstraintManager
     *
     * @param id employee ID
     * @param name employee name
     * @param role employee role
     */
    private void registerEmployee(int id, String name, Role role) {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        employeeManager.add(
                name,
                id,
                100000 + id,
                5000,
                new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2),
                LocalDate.of(2026, 4, 1)
        );

        roleManager.addRoleToEmployee(id, role);
        constraintManager.initConstraintsForEmployee(id);
    }

    /**
     * Returns the internal WeekSchedule object for the week
     * containing the given date.
     *
     * The method accesses ShiftManager internal storage using reflection.
     *
     * @param dateInWeek any date inside the requested week
     * @return the WeekSchedule object for that week, or null if not found
     * @throws Exception if reflection access fails
     */
    private WeekSchedule getWeekSchedule(LocalDate dateInWeek) throws Exception {
        ShiftManager shiftManager = ShiftManager.getInstance();

        var field = ShiftManager.class.getDeclaredField("weekSchedules");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<LocalDate, WeekSchedule> weekSchedules =
                (Map<LocalDate, WeekSchedule>) field.get(shiftManager);

        LocalDate sunday = dateInWeek.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)
        );

        return weekSchedules.get(sunday);
    }

    /**
     * Verifies that publishWeekSchedule successfully publishes the current week.
     * * Scenario:
     * - The service targets the current week.
     * - A Shift Manager is registered and assigned to all shifts (morning and evening)
     * for every day of that week to satisfy the business rule.
     * - The service publishes the current week.
     * * Expected result:
     * - A WeekSchedule object is created for the current week.
     * - The week is successfully marked as published.
     */
    @Test
    void publishWeekSchedule_shouldPublishCurrentWeek() throws Exception {
        ShiftService shiftService = ShiftService.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        LocalDate thisSunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        // Register a Shift Manager to fulfill the publishing validation requirement
        registerEmployee(999, "Admin", Role.shiftManager);

        // Iterate through all 7 days of the target week
        for (int i = 0; i < 7; i++) {
            LocalDate date = thisSunday.plusDays(i);

            // Make the manager available for the specific day
            constraintManager.update(999, date.getDayOfWeek(), ShiftType.any, LocalDate.now());

            // Retrieve shifts and assign the manager to both morning and evening shifts
            Shift morningShift = shiftService.getShift(date, ShiftType.morning);
            shiftService.assignEmployee(morningShift, Role.shiftManager, 999);

            Shift eveningShift = shiftService.getShift(date, ShiftType.evening);
            shiftService.assignEmployee(eveningShift, Role.shiftManager, 999);
        }

        // Action: Publish the schedule
        shiftService.publishWeekSchedule();

        // Validation
        WeekSchedule week = getWeekSchedule(thisSunday);
        assertNotNull(week);
        assertTrue(week.isPublished());
    }

    /**
     * Verifies that publishNextWeekSchedule successfully publishes the next week.
     * * Scenario:
     * - The service targets the upcoming week.
     * - A Shift Manager is registered and assigned to all shifts (morning and evening)
     * for every day of that week to satisfy the business rule (every shift must have a manager).
     * - The service publishes the next week.
     * * Expected result:
     * - A WeekSchedule object is created for the next week.
     * - The week is successfully marked as published without throwing an IllegalStateException.
     */
    @Test
    void publishNextWeekSchedule_shouldPublishNextWeek() throws Exception {
        ShiftService shiftService = ShiftService.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        LocalDate nextSunday = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // Register a Shift Manager to fulfill the publishing validation requirement
        registerEmployee(999, "Admin", Role.shiftManager);

        // Iterate through all 7 days of the target week
        for (int i = 0; i < 7; i++) {
            LocalDate date = nextSunday.plusDays(i);

            // Make the manager available for the specific day, using the injected current date to avoid deadline issues
            constraintManager.update(999, date.getDayOfWeek(), ShiftType.any, LocalDate.now());

            // Retrieve shifts and assign the manager to both morning and evening shifts
            Shift morningShift = shiftService.getShift(date, ShiftType.morning);
            shiftService.assignEmployee(morningShift, Role.shiftManager, 999);

            Shift eveningShift = shiftService.getShift(date, ShiftType.evening);
            shiftService.assignEmployee(eveningShift, Role.shiftManager, 999);
        }

        // Action: Publish the schedule
        shiftService.publishNextWeekSchedule();

        // Validation
        WeekSchedule week = getWeekSchedule(nextSunday);
        assertNotNull(week);
        assertTrue(week.isPublished());
    }

    /**
     * Verifies that the current week becomes PUBLISHED
     * after all shifts in the current week are fully assigned
     * through ShiftService and publishWeekSchedule() is called.
     *
     * Scenario:
     * - Three employees are registered with the required roles
     * - All morning and evening shifts for the current week are created
     * - Each shift requires one cashier, one storekeeper, and one shift manager
     * - All required employees are assigned to every shift
     * - The current week is published
     *
     * Expected result:
     * - getWeekStatus returns PUBLISHED for the current week
     */
    @Test
    void publishWeekSchedule_shouldReturnPublishedWhenCurrentWeekIsFullyAssigned() {
        ShiftService shiftService = ShiftService.getInstance();
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        LocalDate thisSunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        registerEmployee(101, "Alice", Role.Cashier);
        registerEmployee(201, "Bob", Role.Storekeeper);
        registerEmployee(301, "Carol", Role.shiftManager);

        for (int i = 0; i < 7; i++) {
            LocalDate date = thisSunday.plusDays(i);
            DayOfWeek day = date.getDayOfWeek();

            constraintManager.update(101, day, ShiftType.any);
            constraintManager.update(201, day, ShiftType.any);
            constraintManager.update(301, day, ShiftType.any);

            ShiftType[] shiftTypes = {morning, ShiftType.evening};

            for (ShiftType shiftType : shiftTypes) {
                shiftService.addShift(date, shiftType);
                Shift shift = shiftService.getShift(date, shiftType);

                shiftService.setRequirement(shift, Role.Cashier, 1);
                shiftService.setRequirement(shift, Role.Storekeeper, 1);
                shiftService.setRequirement(shift, Role.shiftManager, 1);

                shiftService.assignEmployee(shift, Role.Cashier, 101);
                shiftService.assignEmployee(shift, Role.Storekeeper, 201);
                shiftService.assignEmployee(shift, Role.shiftManager, 301);
            }
        }

        shiftService.publishWeekSchedule();

        assertEquals(WeekStatus.PUBLISHED, shiftManager.getWeekStatus(thisSunday));
    }

    /**
     * Clears all employees from EmployeeManager.
     *
     * @throws Exception if reflection access fails
     */
    private void clearEmployeeManager() throws Exception {
        EmployeeManager manager = EmployeeManager.getInstance();
        var field = EmployeeManager.class.getDeclaredField("employees");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(manager)).clear();
    }

    /**
     * Clears all employee-role mappings from RoleManager.
     *
     * @throws Exception if reflection access fails
     */
    private void clearRoleManager() throws Exception {
        RoleManager manager = RoleManager.getInstance();
        var field = RoleManager.class.getDeclaredField("employeeRoles");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(manager)).clear();
    }

    /**
     * Clears all employee constraints from ConstraintManager.
     *
     * This method scans all fields and clears every map field found,
     * avoiding dependency on a specific internal field name.
     *
     * @throws Exception if reflection access fails
     */
    private void clearConstraintManager() throws Exception {
        ConstraintManager manager = ConstraintManager.getInstance();

        for (var field : ConstraintManager.class.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(manager);

            if (value instanceof Map) {
                ((Map<?, ?>) value).clear();
            }
        }
    }

    /**
     * Clears all shift-related data from ShiftManager,
     * including shifts, published weeks, assignments, and requirements.
     *
     * @throws Exception if reflection access fails
     */
    private void clearShiftManager() throws Exception {
        ShiftManager manager = ShiftManager.getInstance();

        var shiftsField = ShiftManager.class.getDeclaredField("shifts");
        shiftsField.setAccessible(true);
        ((Set<?>) shiftsField.get(manager)).clear();

        var weekField = ShiftManager.class.getDeclaredField("weekSchedules");
        weekField.setAccessible(true);
        ((Map<?, ?>) weekField.get(manager)).clear();

        var assignmentsField = ShiftManager.class.getDeclaredField("assignments");
        assignmentsField.setAccessible(true);
        Object assignments = assignmentsField.get(manager);

        for (var field : assignments.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(assignments);
            if (value instanceof Map) {
                ((Map<?, ?>) value).clear();
            }
        }

        var requirementsField = ShiftManager.class.getDeclaredField("requirements");
        requirementsField.setAccessible(true);
        Object requirements = requirementsField.get(manager);

        for (var field : requirements.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(requirements);
            if (value instanceof Map) {
                ((Map<?, ?>) value).clear();
            }
        }
    }
}