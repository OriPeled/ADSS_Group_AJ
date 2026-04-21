package dev.Workers.Tests;

import dev.Workers.domain.AccessManager;
import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.RoleManager;
import dev.Workers.domain.ShiftManager;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Shift;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the domain managers layer.
 *
 * This class tests the main behavior of:
 * - EmployeeManager
 * - AccessManager
 * - ConstraintManager
 * - RoleManager
 * - ShiftManager
 *
 * The system uses Singleton managers, so each test resets the managers
 * and rebuilds fresh instances before execution.
 */
public class ManagersTest {

    private EmployeeManager employeeManager;
    private AccessManager accessManager;
    private ConstraintManager constraintManager;
    private RoleManager roleManager;
    private ShiftManager shiftManager;

    /**
     * Resets all singleton managers before each test
     * and rebuilds fresh instances in a safe order.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EmployeeManager.class, "instance");
        resetSingleton(AccessManager.class, "instance");
        resetSingleton(ConstraintManager.class, "instance");
        resetSingleton(RoleManager.class, "instance");
        resetSingleton(ShiftManager.class, "instance");

        employeeManager = EmployeeManager.getInstance();

        /*
         * AccessManager keeps a static reference to EmployeeManager.
         * Because classes stay loaded between tests, that field may still
         * point to an old EmployeeManager instance unless we patch it.
         */
        Field accessEmployeeManagerField = AccessManager.class.getDeclaredField("employeeManager");
        accessEmployeeManagerField.setAccessible(true);
        accessEmployeeManagerField.set(null, employeeManager);

        constraintManager = ConstraintManager.getInstance();
        roleManager = RoleManager.getInstance();
        accessManager = AccessManager.getInstance();
        shiftManager = ShiftManager.getInstance();
    }

    /**
     * Resets a singleton static instance field using reflection.
     *
     * @param clazz the singleton class
     * @param fieldName the static instance field name
     */
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * Adds a valid active employee to the system.
     *
     * @param id employee id
     */
    private void addEmployee(int id) {
        employeeManager.add(
                "Employee" + id,
                id,
                1000 + id,
                5000,
                new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2),
                LocalDate.now()
        );
    }

    /**
     * Initializes default weekly constraints for an employee.
     * By default, all days are set to 'any', so the employee is available.
     *
     * @param id employee id
     */
    private void initConstraints(int id) {
        constraintManager.initConstraintsForEmployee(id);
    }

    // =========================================================
    // EmployeeManager tests
    // =========================================================

    /**
     * Verifies that adding a new employee succeeds.
     */
    @Test
    void employee_add_shouldSucceed() {
        addEmployee(1);

        assertTrue(employeeManager.isEmployee(1));
        assertNotNull(employeeManager.getById(1));
    }

    /**
     * Verifies that adding an employee with an existing id fails.
     */
    @Test
    void employee_addDuplicateId_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class, () ->
                employeeManager.add(
                        "AnotherEmployee",
                        1,
                        2222,
                        6000,
                        new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2),
                        LocalDate.now()
                )
        );
    }

    /**
     * Verifies that firing an employee marks them inactive.
     */
    @Test
    void employee_fire_shouldDeactivate() {
        addEmployee(1);

        employeeManager.fire(1);

        assertFalse(employeeManager.getById(1).isActive());
    }

    /**
     * Verifies that rehiring an inactive employee makes them active again.
     */
    @Test
    void employee_rehire_shouldReactivate() {
        addEmployee(1);
        employeeManager.fire(1);

        employeeManager.rehire(1);

        assertTrue(employeeManager.getById(1).isActive());
    }

    // =========================================================
    // AccessManager tests
    // =========================================================

    /**
     * Verifies successful registration and login for an existing employee.
     */
    @Test
    void access_registerAndLogin_shouldSucceed() {
        addEmployee(1);

        accessManager.register(1, "1234");

        assertEquals(UserResponse.success, accessManager.login(1, "1234"));
    }

    /**
     * Verifies that login returns notRegistered
     * when an employee exists but has no password yet.
     */
    @Test
    void access_loginWithoutRegistration_shouldReturnNotRegistered() {
        addEmployee(1);

        assertEquals(UserResponse.notRegistered, accessManager.login(1, "1234"));
    }

    /**
     * Verifies that login with a wrong password fails.
     */
    @Test
    void access_loginWrongPassword_shouldFail() {
        addEmployee(1);
        accessManager.register(1, "1234");

        assertThrows(IllegalArgumentException.class,
                () -> accessManager.login(1, "9999"));
    }

    /**
     * Verifies that registration with a short password fails.
     */
    @Test
    void access_registerShortPassword_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class,
                () -> accessManager.register(1, "123"));
    }

    // =========================================================
    // ConstraintManager tests
    // =========================================================

    /**
     * Verifies that updating constraints before the deadline succeeds.
     */
    @Test
    void constraint_updateBeforeDeadline_shouldSucceed() {
        initConstraints(1);
        constraintManager.setDeadline(DayOfWeek.THURSDAY);

        assertDoesNotThrow(() ->
                constraintManager.update(
                        1,
                        DayOfWeek.MONDAY,
                        ShiftType.morning,
                        LocalDate.of(2026, 4, 20)
                )
        );
    }

    /**
     * Verifies that updating constraints after the deadline fails.
     */
    @Test
    void constraint_updateAfterDeadline_shouldFail() {
        initConstraints(1);
        constraintManager.setDeadline(DayOfWeek.MONDAY);

        assertThrows(RuntimeException.class, () ->
                constraintManager.update(
                        1,
                        DayOfWeek.TUESDAY,
                        ShiftType.morning,
                        LocalDate.of(2026, 4, 21)
                )
        );
    }

    /**
     * Verifies that a matching saved shift makes the employee available.
     */
    @Test
    void constraint_isEmployeeAvailable_shouldReturnTrue() {
        initConstraints(1);
        constraintManager.setDeadline(DayOfWeek.SATURDAY);

        constraintManager.update(
                1,
                DayOfWeek.MONDAY,
                ShiftType.morning,
                LocalDate.of(2026, 4, 20)
        );

        assertTrue(constraintManager.isEmployeeAvailable(1, DayOfWeek.MONDAY, ShiftType.morning));
    }

    // =========================================================
    // RoleManager tests
    // =========================================================

    /**
     * Verifies that adding a role to an employee succeeds.
     */
    @Test
    void role_addRole_shouldSucceed() {
        addEmployee(1);

        roleManager.addRoleToEmployee(1, Role.Cashier);

        assertTrue(roleManager.hasRole(1, Role.Cashier));
    }

    /**
     * Verifies that adding the same role twice fails.
     */
    @Test
    void role_addDuplicateRole_shouldFail() {
        addEmployee(1);
        roleManager.addRoleToEmployee(1, Role.Cashier);

        assertThrows(IllegalArgumentException.class,
                () -> roleManager.addRoleToEmployee(1, Role.Cashier));
    }

    /**
     * Verifies that removing a role succeeds.
     */
    @Test
    void role_removeRole_shouldSucceed() {
        addEmployee(1);
        roleManager.addRoleToEmployee(1, Role.Cashier);

        roleManager.removeSingleItem(1, Role.Cashier);

        assertFalse(roleManager.hasRole(1, Role.Cashier));
    }

    // =========================================================
    // ShiftManager tests
    // =========================================================

    /**
     * Verifies that assigning a qualified and available employee to a shift succeeds.
     */
    @Test
    void shift_assignEmployee_shouldSucceed() {
        addEmployee(1);
        roleManager.addRoleToEmployee(1, Role.Cashier);
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(date, ShiftType.morning);
        Shift shift = shiftManager.getShift(date, ShiftType.morning);

        shiftManager.setRequirement(shift, Role.Cashier, 1);
        shiftManager.assignEmployee(shift, Role.Cashier, 1);

        assertFalse(shiftManager.isNeeded(shift, Role.Cashier));
    }

    /**
     * Verifies that assigning an employee without the required role fails.
     */
    @Test
    void shift_assignEmployeeWithoutRole_shouldFail() {
        addEmployee(1);
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(date, ShiftType.morning);
        Shift shift = shiftManager.getShift(date, ShiftType.morning);

        shiftManager.setRequirement(shift, Role.Cashier, 1);

        assertThrows(IllegalArgumentException.class,
                () -> shiftManager.assignEmployee(shift, Role.Cashier, 1));
    }

    /**
     * Verifies that replacing one employee with another succeeds
     * when both are qualified and available.
     */
    @Test
    void shift_replaceEmployee_shouldSucceed() {
        addEmployee(1);
        addEmployee(2);

        roleManager.addRoleToEmployee(1, Role.Cashier);
        roleManager.addRoleToEmployee(2, Role.Cashier);

        initConstraints(1);
        initConstraints(2);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(date, ShiftType.morning);
        Shift shift = shiftManager.getShift(date, ShiftType.morning);

        shiftManager.setRequirement(shift, Role.Cashier, 1);

        shiftManager.assignEmployee(shift, Role.Cashier, 1);
        shiftManager.replaceEmployee(shift, 1, 2);

        assertFalse(shiftManager.isNeeded(shift, Role.Cashier));
    }

    /**
     * Verifies that publishing a fully assigned week succeeds
     * when every shift has at least one manager employee.
     */
    @Test
    void shift_publishWeekSchedule_shouldSucceedWhenWeekIsFullyAssigned() {
        addEmployee(1);
        addEmployee(2);

        employeeManager.getById(1).setManager(true);

        roleManager.addRoleToEmployee(1, Role.Cashier);
        roleManager.addRoleToEmployee(2, Role.Storekeeper);

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.morning, ShiftType.evening}) {
                shiftManager.addShift(date, type);
                Shift shift = shiftManager.getShift(date, type);

                shiftManager.setRequirement(shift, Role.Cashier, 1);
                shiftManager.setRequirement(shift, Role.Storekeeper, 1);

                shiftManager.assignEmployee(shift, Role.Cashier, 1);
                shiftManager.assignEmployee(shift, Role.Storekeeper, 2);
            }
        }

        shiftManager.publishWeekSchedule(sunday);

        assertEquals(WeekStatus.PUBLISHED, shiftManager.getWeekStatus(sunday));
    }

    /**
     * Verifies that publishing fails when no manager is assigned in the shifts.
     */
    @Test
    void shift_publishWeekScheduleWithoutManager_shouldFail() {
        addEmployee(1);
        addEmployee(2);

        roleManager.addRoleToEmployee(1, Role.Cashier);
        roleManager.addRoleToEmployee(2, Role.Storekeeper);

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.morning, ShiftType.evening}) {
                shiftManager.addShift(date, type);
                Shift shift = shiftManager.getShift(date, type);

                shiftManager.setRequirement(shift, Role.Cashier, 1);
                shiftManager.setRequirement(shift, Role.Storekeeper, 1);

                shiftManager.assignEmployee(shift, Role.Cashier, 1);
                shiftManager.assignEmployee(shift, Role.Storekeeper, 2);
            }
        }

        assertThrows(IllegalStateException.class,
                () -> shiftManager.publishWeekSchedule(LocalDate.now()));
    }
}