package dev.Workers.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.service.ShiftService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShiftService.
 */
class ShiftServiceTest {

    private ShiftService shiftService;
    private EmployeeHandler employeeHandler;
    private PreferenceHandler preferenceHandler;
    private RoleRegistry roleRegistry;
    private Branch branch;

    private Role cashierRole;

    private int nextEmployeeId = 10000;
    private int nextShiftOffset = 0;
    private void resetSingleton(Class<?> clazz, String fieldName)
            throws Exception {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }
    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.eraseDatabase();
        DatabaseInitializer.initializeDatabase();

        resetSingleton(EmployeeHandler.class,"instance");
        resetSingleton(ShiftHandler.class,"instance");
        resetSingleton(ShiftService.class,"instance");
        resetSingleton(PreferenceHandler.class,"instance");
        resetSingleton(RoleRegistry.class,"instance");
        resetSingleton(BranchRegistry.class,"instance");

        shiftService = ShiftService.getInstance();
        employeeHandler = EmployeeHandler.getInstance();
        preferenceHandler = PreferenceHandler.getInstance();
        roleRegistry = RoleRegistry.getInstance();

        branch = BranchRegistry.getInstance()
                .getBranchByName("Beer-Sheva");

        cashierRole = roleRegistry.getRoleByName("Cashier");

        nextEmployeeId = 10000;
        nextShiftOffset = 0;

        preferenceHandler.setDeadline(null);
    }

    /**
     * Creates an employee with a specific role.
     */
    private int createEmployee(Role role, boolean manager) {

        int id = nextEmployeeId++;

        employeeHandler.add(
                "Employee" + id,
                id,
                branch,
                1000 + id,
                5000,
                new EmployeeTerms(
                        JobStatus.fullTime,
                        SalaryType.global,
                        2,
                        DayOfWeek.WEDNESDAY),
                LocalDate.of(2025,1,1));

        employeeHandler.addRole(id, role);

        employeeHandler.getEmployee(id).setManager(manager);

        preferenceHandler.initPreferences(id);

        for (DayOfWeek day : DayOfWeek.values()) {
            preferenceHandler.update(id, day, ShiftType.ANY);
        }

        return id;
    }
    /**
     * Creates a unique future shift.
     */
    private Shift createFutureMorningShift() {

        LocalDate date =
                LocalDate.now()
                        .plusWeeks(4)
                        .plusDays(nextShiftOffset++);

        return shiftService.getShift(
                branch,
                date,
                ShiftType.MORNING);
    }

    /**
     * Verifies that assigning a qualified employee succeeds.
     */
    @Test
    void assignEmployee_shouldSucceed() {

        int empId = createEmployee(cashierRole, true);

        Shift shift = createFutureMorningShift();

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        assertDoesNotThrow(() ->
                shiftService.assignEmployee(
                        shift,
                        cashierRole,
                        empId));

    }

    /**
     * Verifies that assigning the same employee twice fails.
     */
    @Test
    void assignEmployeeTwice_shouldFail() {

        int empId = createEmployee(cashierRole, false);

        Shift shift = createFutureMorningShift();

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                2);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                empId);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.assignEmployee(
                        shift,
                        cashierRole,
                        empId));
    }

    /**
     * Verifies that replacing an employee with himself fails.
     */
    @Test
    void replaceEmployeeWithSameEmployee_shouldFail() {

        int empId = createEmployee(cashierRole, false);

        Shift shift = createFutureMorningShift();

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                empId);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.replaceEmployee(
                        shift,
                        empId,
                        empId));
    }

    /**
     * Verifies that publishing an incomplete week fails.
     */
    @Test
    void publishIncompleteWeek_shouldFail() {

        assertThrows(
                IllegalStateException.class,
                () -> shiftService.publishNextWeekSchedule(branch));
    }

    /**
     * Verifies that shift history returns a valid string.
     */
    @Test
    void getShiftHistory_shouldReturnString() {

        String history =
                shiftService.getShiftHistory(branch);

        assertNotNull(history);
    }
    /**
     * Verifies that removing an assigned employee succeeds.
     */
    @Test
    void removeEmployee_shouldSucceed() {

        int empId = createEmployee(cashierRole, false);

        Shift shift = createFutureMorningShift();

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                empId);

        shiftService.removeEmployee(
                shift,
                empId);

        assertFalse(
                shiftService.isShiftAssigned(shift));
    }

    /**
     * Verifies that negative extra hours are rejected.
     */
    @Test
    void updateExtraHours_negativeValue_shouldFail() {

        Shift shift = createFutureMorningShift();

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.updateExtraHours(
                        shift,
                        1,
                        -1));
    }

    /**
     * Verifies that extra hours above four are rejected.
     */
    @Test
    void updateExtraHours_aboveFour_shouldFail() {

        Shift shift = createFutureMorningShift();

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftService.updateExtraHours(
                        shift,
                        1,
                        5));
    }

    /**
     * Verifies that displaying week assignments returns a string.
     */
    @Test
    void displayWeekAssignments_shouldReturnString() {

        String result =
                shiftService.displayWeekAssignments(branch);

        assertNotNull(result);
    }

    /**
     * Verifies that shift details return a string.
     */
    @Test
    void getShiftDetails_shouldReturnString() {

        Shift shift = createFutureMorningShift();

        String details =
                shiftService.getShiftDetails(shift);

        assertNotNull(details);
    }


    /**
     * Verifies that current week display returns a string.
     */
    @Test
    void displayCurrentWeek_shouldReturnString() {

        String result =
                shiftService.displayCurrentWeek(branch);

        assertNotNull(result);
    }

    /**
     * Verifies that next week display returns a string.
     */
    @Test
    void displayNextWeek_shouldReturnString() {

        String result =
                shiftService.displayNextWeek(branch);

        assertNotNull(result);
    }

    /**
     * Verifies that empty week status can be obtained.
     */
    @Test
    void getWeekStatus_shouldReturnStatus() {

        assertNotNull(
                shiftService.getWeekStatus(branch));
    }

    /**
     * Verifies that resetting a shift does not throw exceptions.
     */
    @Test
    void resetShift_shouldNotThrow() {

        Shift shift = createFutureMorningShift();

        assertDoesNotThrow(
                () -> shiftService.resetShift(shift));
    }
}