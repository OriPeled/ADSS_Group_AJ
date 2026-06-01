package dev.Workers.Tests;

import dev.Workers.domain.*;
import dev.Workers.Service.RoleService;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.Branch;
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

    private EmployeeManager employeeManager;
    private AccessManager accessManager;
    private ConstraintManager constraintManager;
    private RoleService roleService; // Renamed to match the type
    private ShiftManager shiftManager;
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
        resetSingleton(EmployeeManager.class, "instance");
        resetSingleton(AccessManager.class, "instance");
        resetSingleton(ConstraintManager.class, "instance");
        resetSingleton(RoleService.class, "instance");
        resetSingleton(ShiftManager.class, "instance");
        resetSingleton(RoleRegistry.class, "instance"); // Reset the new registry!

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
        roleService = RoleService.getInstance();
        accessManager = AccessManager.getInstance();
        shiftManager = ShiftManager.getInstance();

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
        employeeManager.add(
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
        constraintManager.initConstraintsForEmployee(id);
    }

    // =========================================================
    // EmployeeManager tests
    // =========================================================

    @Test
    void employee_add_shouldSucceed() {
        addEmployee(1);

        assertTrue(employeeManager.isEmployee(1));
        assertNotNull(employeeManager.getById(1));
    }

    @Test
    void employee_addDuplicateId_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class, () ->
                employeeManager.add(
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

        employeeManager.fire(1);

        assertFalse(employeeManager.getById(1).isActive(LocalDate.now()));
    }

    @Test
    void employee_rehire_shouldReactivate() {
        addEmployee(1);
        employeeManager.fire(1);

        employeeManager.rehire(1);

        assertTrue(employeeManager.getById(1).isActive(LocalDate.now()));
    }

    // =========================================================
    // AccessManager tests
    // =========================================================

    @Test
    void access_registerAndLogin_shouldSucceed() {
        addEmployee(1);

        accessManager.register(1, "1234");

        assertEquals(UserResponse.success, accessManager.login(1, "1234"));
    }

    @Test
    void access_loginWithoutRegistration_shouldReturnNotRegistered() {
        addEmployee(1);

        assertEquals(UserResponse.notRegistered, accessManager.login(1, "1234"));
    }

    @Test
    void access_loginWrongPassword_shouldFail() {
        addEmployee(1);
        accessManager.register(1, "1234");

        assertThrows(IllegalArgumentException.class,
                () -> accessManager.login(1, "9999"));
    }

    @Test
    void access_registerShortPassword_shouldFail() {
        addEmployee(1);

        assertThrows(IllegalArgumentException.class,
                () -> accessManager.register(1, "123"));
    }

    // =========================================================
    // ConstraintManager tests
    // =========================================================

    @Test
    void constraint_updateBeforeDeadline_shouldSucceed() {
        initConstraints(1);
        constraintManager.setDeadline(DayOfWeek.THURSDAY);

        assertDoesNotThrow(() ->
                constraintManager.update(
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
        constraintManager.setDeadline(DayOfWeek.MONDAY);

        assertThrows(RuntimeException.class, () ->
                constraintManager.update(
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
        constraintManager.setDeadline(DayOfWeek.SATURDAY);

        constraintManager.update(
                1,
                DayOfWeek.MONDAY,
                ShiftType.MORNING,
                LocalDate.of(2026, 4, 20)
        );

        assertTrue(constraintManager.isEmployeeAvailable(1, DayOfWeek.MONDAY, ShiftType.MORNING));
    }

    // =========================================================
    // RoleManager tests
    // =========================================================

    @Test
    void role_addRole_shouldSucceed() {
        addEmployee(1);
        roleService.addRoleToEmployee(1, cashier); // CHANGED

        assertTrue(roleService.isQualified(1, cashier)); // CHANGED
    }

    @Test
    void role_addDuplicateRole_shouldFail() {
        addEmployee(1);
        roleService.addRoleToEmployee(1, cashier); // CHANGED

        assertThrows(IllegalArgumentException.class,
                () -> roleService.addRoleToEmployee(1, cashier)); // CHANGED
    }

    @Test
    void role_removeRole_shouldSucceed() {
        addEmployee(1);
        roleService.addRoleToEmployee(1, cashier); // CHANGED

        roleService.removeSingleItem(1, cashier); // CHANGED

        assertFalse(roleService.isQualified(1, cashier)); // CHANGED
    }

    // =========================================================
    // ShiftManager tests
    // =========================================================

    @Test
    void shift_assignEmployee_shouldSucceed() {
        addEmployee(1);
        roleService.addRoleToEmployee(1, cashier); // CHANGED
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(employeeManager.getById(1).getBranch(), date, ShiftType.MORNING);
        Shift shift = shiftManager.getShift(employeeManager.getById(1).getBranch(),date, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashier, 1); // CHANGED
        shiftManager.assignEmployee(shift, cashier, 1); // CHANGED

        assertFalse(shiftManager.isNeeded(shift, cashier)); // CHANGED
    }

    @Test
    void shift_assignEmployeeWithoutRole_shouldFail() {
        addEmployee(1);
        initConstraints(1);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(employeeManager.getById(1).getBranch(),date, ShiftType.MORNING);
        Shift shift = shiftManager.getShift(employeeManager.getById(1).getBranch(),date, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashier, 1); // CHANGED

        assertThrows(IllegalArgumentException.class,
                () -> shiftManager.assignEmployee(shift, cashier, 1)); // CHANGED
    }

    @Test
    void shift_replaceEmployee_shouldSucceed() {
        addEmployee(1);
        addEmployee(2);

        roleService.addRoleToEmployee(1, cashier); // CHANGED
        roleService.addRoleToEmployee(2, cashier); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate date = LocalDate.now().plusDays(1);
        shiftManager.addShift(employeeManager.getById(1).getBranch(), date, ShiftType.MORNING);
        Shift shift = shiftManager.getShift(employeeManager.getById(1).getBranch(), date, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashier, 1); // CHANGED

        shiftManager.assignEmployee(shift, cashier, 1); // CHANGED
        shiftManager.replaceEmployee(shift, 1, 2);

        assertFalse(shiftManager.isNeeded(shift, cashier)); // CHANGED
    }

    @Test
    void shift_publishWeekSchedule_shouldSucceedWhenWeekIsFullyAssigned() {
        addEmployee(1);
        addEmployee(2);

        employeeManager.getById(1).setManager(true);

        roleService.addRoleToEmployee(1, cashier); // CHANGED
        roleService.addRoleToEmployee(2, storekeeper); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                shiftManager.addShift(employeeManager.getById(1).getBranch(), date, type);
                Shift shift = shiftManager.getShift(employeeManager.getById(1).getBranch(), date, type);

                shiftManager.setRequirement(shift, cashier, 1); // CHANGED
                shiftManager.setRequirement(shift, storekeeper, 1); // CHANGED

                shiftManager.assignEmployee(shift, cashier, 1); // CHANGED
                shiftManager.assignEmployee(shift, storekeeper, 2); // CHANGED
            }
        }

        shiftManager.publishWeekSchedule(employeeManager.getById(1).getBranch(), sunday);

        assertEquals(WeekStatus.PUBLISHED, shiftManager.getWeekStatus(employeeManager.getById(1).getBranch(), sunday));
    }

    @Test
    void shift_publishWeekScheduleWithoutManager_shouldFail() {
        addEmployee(1);
        addEmployee(2);

        roleService.addRoleToEmployee(1, cashier); // CHANGED
        roleService.addRoleToEmployee(2, storekeeper); // CHANGED

        initConstraints(1);
        initConstraints(2);

        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                shiftManager.addShift(employeeManager.getById(1).getBranch(), date, type);
                Shift shift = shiftManager.getShift(employeeManager.getById(1).getBranch(), date, type);

                shiftManager.setRequirement(shift, cashier, 1); // CHANGED
                shiftManager.setRequirement(shift, storekeeper, 1); // CHANGED

                shiftManager.assignEmployee(shift, cashier, 1); // CHANGED
                shiftManager.assignEmployee(shift, storekeeper, 2); // CHANGED
            }
        }

        assertThrows(IllegalStateException.class,
                () -> shiftManager.publishWeekSchedule(employeeManager.getById(1).getBranch(), LocalDate.now()));
    }
}