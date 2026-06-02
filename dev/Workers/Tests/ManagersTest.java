package dev.Workers.Tests;

import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role; // NEW
import dev.Workers.domain.Objects.Shift;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the domain managers layer.
 */
public class ManagersTest {

    private EmployeeHandler employeeHandler;
    private AccessHandler accessHandler;
    private ConstraintHandler constraintHandler;
    private ShiftHandler shiftHandler;
    private RoleRegistry roleRegistry; // NEW
    private BranchRegistry branchRegistry;

    // Cached roles for testing
    private Role cashier;
    private Role storekeeper;

    /**
     * Resets all singleton managers before each test
     * and rebuilds fresh instances in a safe order.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(AccessHandler.class, "instance");
        resetSingleton(ConstraintHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
        resetSingleton(RoleRegistry.class, "instance"); // Reset the new registry!

        employeeHandler = EmployeeHandler.getInstance();

        /*
         * AccessManager keeps a static reference to EmployeeManager.
         * Because classes stay loaded between tests, that field may still
         * point to an old EmployeeManager instance unless we patch it.
         */
        Field accessEmployeeManagerField = AccessHandler.class.getDeclaredField("employeeManager");
        accessEmployeeManagerField.setAccessible(true);
        accessEmployeeManagerField.set(null, employeeHandler);

        constraintHandler = ConstraintHandler.getInstance();
        accessHandler = AccessHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();

        // Initialize Registry and cache the roles for our tests
        roleRegistry = RoleRegistry.getInstance();
        cashier = roleRegistry.getRoleByName("Cashier");
        storekeeper = roleRegistry.getRoleByName("Storekeeper");
    }

    /**
     * Resets a singleton static instance field using reflection.
     */
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * Adds a valid active employee to the system.
     */
    private void addEmployee(int id) {
        employeeHandler.add(
                "Employee" + id,
                id,
                branchRegistry.getBranchByName("Ofakim"),
                1000 + id,
                5000,
                new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY),
                LocalDate.now()
        );
    }

    /**
     * Initializes default weekly constraints for an employee.
     */
    private void initConstraints(int id) {
        constraintHandler.initConstraintsForEmployee(id);
    }

    // =========================================================
    // EmployeeManager tests
    // =========================================================

    @Test
    void employee_add_shouldSucceed() {
        addEmployee(1);

        assertTrue(employeeHandler.isEmployee(1));
        assertNotNull(employeeHandler.getEmployee(1));
    }

    @Test
    void employee_addDuplicateId_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class, () ->
                employeeHandler.add(
                        "AnotherEmployee",
                        1,
                        branchRegistry.getBranchByName("Ofakim"),
                        2222,
                        6000,
                        new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY),
                        LocalDate.now()
                )
        );
    }

    @Test
    void employee_fire_shouldDeactivate() {
        addEmployee(1);

        employeeHandler.fire(1);

        assertFalse(employeeHandler.getEmployee(1).isActive(LocalDate.now()));
    }

    @Test
    void employee_rehire_shouldReactivate() {
        addEmployee(1);
        employeeHandler.fire(1);

        employeeHandler.rehire(1);

        assertTrue(employeeHandler.getEmployee(1).isActive(LocalDate.now()));
    }

    // =========================================================
    // AccessManager tests
    // =========================================================

    @Test
    void access_registerAndLogin_shouldSucceed() {
        addEmployee(1);

        accessHandler.register(1, "1234");

        assertEquals(UserResponse.success, accessHandler.login(1, "1234"));
    }

    @Test
    void access_loginWithoutRegistration_shouldReturnNotRegistered() {
        addEmployee(1);

        assertEquals(UserResponse.notRegistered, accessHandler.login(1, "1234"));
    }

    @Test
    void access_loginWrongPassword_shouldFail() {
        addEmployee(1);
        accessHandler.register(1, "1234");

        assertThrows(IllegalArgumentException.class,
                () -> accessHandler.login(1, "9999"));
    }

    @Test
    void access_registerShortPassword_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class,
                () -> accessHandler.register(1, "123"));
    }

    // =========================================================
    // ConstraintManager tests
    // =========================================================

    @Test
    void constraint_updateBeforeDeadline_shouldSucceed() {
        initConstraints(1);
        constraintHandler.setDeadline(DayOfWeek.THURSDAY);

        assertDoesNotThrow(() ->
                constraintHandler.update(
                        1,
                        DayOfWeek.MONDAY,
                        ShiftType.MORNING,
                        LocalDate.of(2026, 4, 20)
                )
        );
    }

    @Test
    void constraint_updateAfterDeadline_shouldFail() {
        initConstraints(1);
        constraintHandler.setDeadline(DayOfWeek.MONDAY);

        assertThrows(RuntimeException.class, () ->
                constraintHandler.update(
                        1,
                        DayOfWeek.TUESDAY,
                        ShiftType.MORNING,
                        LocalDate.of(2026, 4, 21)
                )
        );
    }

    @Test
    void constraint_isEmployeeAvailable_shouldReturnTrue() {
        initConstraints(1);
        constraintHandler.setDeadline(DayOfWeek.SATURDAY);

        constraintHandler.update(
                1,
                DayOfWeek.MONDAY,
                ShiftType.MORNING,
                LocalDate.of(2026, 4, 20)
        );

        assertTrue(constraintHandler.isEmployeeAvailable(1, DayOfWeek.MONDAY, ShiftType.MORNING));
    }

    // =========================================================
    // RoleManager tests
    // =========================================================

    @Test
    void role_addRole_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier); // CHANGED

        assertTrue(cashier.isQualified(1)); // CHANGED
    }

    @Test
    void role_addDuplicateRole_shouldFail() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier); // CHANGED

        assertThrows(IllegalArgumentException.class,
                () -> employeeHandler.addRole(1, cashier)); // CHANGED
    }

    @Test
    void role_removeRole_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier); // CHANGED

        employeeHandler.removeRole(1, cashier); // CHANGED

        assertFalse(cashier.isQualified(1)); // CHANGED
    }

    // =========================================================
    // ShiftManager tests
    // =========================================================

    @Test
    void shift_assignEmployee_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier); // CHANGED
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftHandler.addShift(employeeHandler.getEmployee(1).getBranch(), date, ShiftType.MORNING);
        Shift shift = shiftHandler.getShift(employeeHandler.getEmployee(1).getBranch(),date, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashier, 1); // CHANGED
        shiftHandler.assignEmployee(shift, cashier, 1); // CHANGED

        assertFalse(shiftHandler.isNeeded(shift, cashier)); // CHANGED
    }

    @Test
    void shift_assignEmployeeWithoutRole_shouldFail() {
        addEmployee(1);
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftHandler.addShift(employeeHandler.getEmployee(1).getBranch(),date, ShiftType.MORNING);
        Shift shift = shiftHandler.getShift(employeeHandler.getEmployee(1).getBranch(),date, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashier, 1); // CHANGED

        assertThrows(IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(shift, cashier, 1)); // CHANGED
    }

    @Test
    void shift_replaceEmployee_shouldSucceed() {
        addEmployee(1);
        addEmployee(2);

        employeeHandler.addRole(1, cashier); // CHANGED
        employeeHandler.addRole(2, cashier); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftHandler.addShift(employeeHandler.getEmployee(1).getBranch(), date, ShiftType.MORNING);
        Shift shift = shiftHandler.getShift(employeeHandler.getEmployee(1).getBranch(), date, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashier, 1); // CHANGED

        shiftHandler.assignEmployee(shift, cashier, 1); // CHANGED
        shiftHandler.replaceEmployee(shift, 1, 2);

        assertFalse(shiftHandler.isNeeded(shift, cashier)); // CHANGED
    }

    @Test
    void shift_publishWeekSchedule_shouldSucceedWhenWeekIsFullyAssigned() {
        addEmployee(1);
        addEmployee(2);

        employeeHandler.getEmployee(1).setManager(true);

        employeeHandler.addRole(1, cashier); // CHANGED
        employeeHandler.addRole(2, storekeeper); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                shiftHandler.addShift(employeeHandler.getEmployee(1).getBranch(), date, type);
                Shift shift = shiftHandler.getShift(employeeHandler.getEmployee(1).getBranch(), date, type);

                shiftHandler.setRequirement(shift, cashier, 1); // CHANGED
                shiftHandler.setRequirement(shift, storekeeper, 1); // CHANGED

                shiftHandler.assignEmployee(shift, cashier, 1); // CHANGED
                shiftHandler.assignEmployee(shift, storekeeper, 2); // CHANGED
            }
        }

        shiftHandler.publishWeekSchedule(employeeHandler.getEmployee(1).getBranch(), sunday);

        assertEquals(WeekStatus.PUBLISHED, shiftHandler.getWeekStatus(employeeHandler.getEmployee(1).getBranch(), sunday));
    }

    @Test
    void shift_publishWeekScheduleWithoutManager_shouldFail() {
        addEmployee(1);
        addEmployee(2);

        employeeHandler.addRole(1, cashier); // CHANGED
        employeeHandler.addRole(2, storekeeper); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                shiftHandler.addShift(employeeHandler.getEmployee(1).getBranch(), date, type);
                Shift shift = shiftHandler.getShift(employeeHandler.getEmployee(1).getBranch(), date, type);

                shiftHandler.setRequirement(shift, cashier, 1); // CHANGED
                shiftHandler.setRequirement(shift, storekeeper, 1); // CHANGED

                shiftHandler.assignEmployee(shift, cashier, 1); // CHANGED
                shiftHandler.assignEmployee(shift, storekeeper, 2); // CHANGED
            }
        }

        assertThrows(IllegalStateException.class,
                () -> shiftHandler.publishWeekSchedule(employeeHandler.getEmployee(1).getBranch(), LocalDate.now()));
    }
}