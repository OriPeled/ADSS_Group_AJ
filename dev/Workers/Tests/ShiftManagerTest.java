package dev.Workers.Tests;

import dev.Workers.domain.AccessManager;
import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Enums.*;
import dev.Workers.Service.RoleService;
import dev.Workers.domain.RoleRegistry;
import dev.Workers.domain.ShiftManager;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the ShiftManager class.
 *
 * This test class verifies the core shift assignment logic of the system.
 * It focuses on the interaction between ShiftManager and the supporting managers:
 * - EmployeeManager
 * - RoleManager
 * - ConstraintManager
 *
 * Covered scenarios:
 * 1. Valid employee assignment
 * 2. Invalid assignment due to missing qualification
 * 3. Invalid assignment due to lack of availability
 * 4. Replacing an assigned employee with another valid employee
 * 5. Rejecting replacement when the new employee is not qualified
 * 6. Reporting missing employees for a shift role
 * 7. Detecting when no valid employee is available for assignment
 */
public class ShiftManagerTest {

    private Role cashierRole;
    private Role storekeeperRole;
    private Role shiftManagerRole;

    /**
     * Resets all singleton managers before each test
     * and rebuilds fresh instances to prevent state leakage.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EmployeeManager.class, "instance");
        resetSingleton(AccessManager.class, "instance");
        resetSingleton(ConstraintManager.class, "instance");
        resetSingleton(RoleService.class, "instance");
        resetSingleton(ShiftManager.class, "instance");
        resetSingleton(RoleRegistry.class, "instance");

        RoleRegistry registry = RoleRegistry.getInstance();
        cashierRole = registry.getRoleByName("Cashier");
        storekeeperRole = registry.getRoleByName("Storekeeper");
        shiftManagerRole = registry.getRoleByName("Shift Manager");
    }

    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * Creates a valid EmployeeTerms object for test employees.
     *
     * @return a default EmployeeTerms instance
     */
    private EmployeeTerms createTerms() {
        return new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY);
    }

    private Shift createShift(int dayOffset) {
        ShiftManager shiftManager = ShiftManager.getInstance();
        LocalDate date = LocalDate.of(2026, 4, 20).plusDays(dayOffset);
        shiftManager.addShift(date, ShiftType.MORNING);
        return shiftManager.getShift(date, ShiftType.MORNING);
    }

    private void registerEmployee(int id, String name, Role role) {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        RoleService roleService = RoleService.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        EmployeeTerms terms = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY);
        employeeManager.add(name, id, LicenseType.A, 100000, 5000, terms, LocalDate.of(2026, 4, 1));
        roleService.addRoleToEmployee(id, role);
        constraintManager.initConstraintsForEmployee(id);
    }

    /**
     * Verifies that a qualified and available employee
     * can be assigned successfully to a required role in a shift.
     */
    @Test
    void assignEmployee_shouldSucceedForValidEmployee() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(1, "Alice", cashierRole);
        Shift shift = createShift(0);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(1, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 1);

        assertEquals(0, shiftManager.leftToAssign(shift, cashierRole));
        assertFalse(shiftManager.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that assignment fails when the employee
     * does not have the required role.
     */
    @Test
    void assignEmployee_should_Fail_When_Employee_Is_Not_Qualified() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(2, "Bob", storekeeperRole);
        Shift shift = createShift(1);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(2, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, cashierRole, 2)
        );
    }

    /**
     * Verifies that assignment fails when the employee
     * is not available for the requested shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsNotAvailable() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(3, "Charlie", cashierRole);
        Shift shift = createShift(2);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(3, day, ShiftType.EVENING);

        shiftManager.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, cashierRole, 3)
        );
    }

    /**
     * Verifies that an assigned employee can be replaced
     * by another employee who is qualified and available.
     */
    @Test
    void replaceEmployee_shouldReplaceAssignedEmployeeSuccessfully() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(10, "David", cashierRole);
        registerEmployee(11, "Eve", cashierRole);

        Shift shift = createShift(3);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(10, day, ShiftType.MORNING);
        constraintManager.update(11, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 10);
        shiftManager.replaceEmployee(shift, 10, 11);

        assertEquals(0, shiftManager.leftToAssign(shift, cashierRole));
        assertFalse(shiftManager.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that replacing an assigned employee fails
     * when the new employee does not have the required role.
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeIsNotQualified() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(20, "Frank", cashierRole);
        registerEmployee(21, "Grace", storekeeperRole);

        Shift shift = createShift(4);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(20, day, ShiftType.MORNING);
        constraintManager.update(21, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 20);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.replaceEmployee(shift, 20, 21)
        );
    }

    /**
     * Verifies that ShiftManager correctly calculates
     * how many employees are still missing for a role in a shift.
     */
    @Test
    void leftToAssign_shouldReportMissingEmployeesCorrectly() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(30, "Hannah", cashierRole);
        Shift shift = createShift(5);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(30, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 2);
        assertEquals(2, shiftManager.leftToAssign(shift, cashierRole));

        shiftManager.assignEmployee(shift, cashierRole, 30);
        assertEquals(1, shiftManager.leftToAssign(shift, cashierRole));
    }

    /**
     * Verifies that a terminated employee cannot be assigned to a shift.
     */
    @Test
    void assignEmployee_shouldFail_WhenEmployeeIsTerminated() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();
        EmployeeManager employeeManager = EmployeeManager.getInstance();

        registerEmployee(60, "Liam", cashierRole);
        employeeManager.fire(60);

        Shift shift = createShift(6);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(60, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, cashierRole, 60)
        );
    }

    /**
     * Verifies that a newly added employee can replace
     * another assigned employee in a shift.
     */
    @Test
    void replaceEmployee_shouldSucceedAfterAddingTwoEmployees() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(70, "Mia", cashierRole);
        registerEmployee(71, "Noah", cashierRole);

        Shift shift = createShift(7);
        DayOfWeek day = shift.getDate().getDayOfWeek();

        constraintManager.update(70, day, ShiftType.MORNING);
        constraintManager.update(71, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 70);
        shiftManager.replaceEmployee(shift, 70, 71);

        assertEquals(0, shiftManager.leftToAssign(shift, cashierRole));
        assertFalse(shiftManager.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that replacement fails when the new employee
     * was added to the system and later terminated.
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeWasTerminated() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();
        EmployeeManager employeeManager = EmployeeManager.getInstance();

        registerEmployee(80, "Olivia", cashierRole);
        registerEmployee(81, "Emma", cashierRole);

        Shift shift = createShift(8);
        DayOfWeek day = shift.getDate().getDayOfWeek();

        constraintManager.update(80, day, ShiftType.MORNING);
        constraintManager.update(81, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 80);
        employeeManager.fire(81);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.replaceEmployee(shift, 80, 81)
        );
    }

    /**
     * Verifies that a terminated employee cannot receive a new role.
     */
    @Test
    void addRoleToEmployee_shouldFailWhenEmployeeIsTerminated() {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        RoleService roleService = RoleService.getInstance();

        registerEmployee(90, "Sophia", cashierRole);
        employeeManager.fire(90);

        assertThrows(IllegalArgumentException.class, () ->
                roleService.addRoleToEmployee(90, storekeeperRole)
        );
    }

    /**
     * Verifies that replacing an employee with the same employee ID
     * is not allowed.
     */
    @Test
    void replaceEmployee_shouldFailWhenReplacingWithSameEmployee() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(110, "Lior", cashierRole);
        Shift shift = createShift(10);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(110, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 1);
        shiftManager.assignEmployee(shift, cashierRole, 110);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.replaceEmployee(shift, 110, 110)
        );
    }

    /**
     * Verifies that publishing a week where at least one shift has no assigned
     * shift manager throws IllegalStateException.
     */
    @Test
    void publishWeekSchedule_shouldFail_WhenShiftHasNoManager() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        LocalDate isolatedDate = LocalDate.of(2027, 6, 1);
        assertThrows(IllegalStateException.class, () ->
                shiftManager.publishWeekSchedule(isolatedDate)
        );
    }

    /**
     * Verifies that setting the shift manager requirement to 0 throws
     * IllegalArgumentException, since every shift must have at least one.
     */
    @Test
    void setRequirement_shouldFail_WhenShiftManagerCountIsZero() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        Shift shift = createShift(20);

        /*if (role == Role.shiftManager && count < 1) {
        throw new IllegalArgumentException("Cannot set shift manager requirement below 1...");
        }*/

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.setRequirement(shift, shiftManagerRole, 0)
        );
    }

    /**
     * Verifies that an employee cannot be assigned twice
     * to the same shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeAlreadyAssignedToSameShift() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        registerEmployee(100, "Daniel", cashierRole);
        Shift shift = createShift(9);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        constraintManager.update(100, day, ShiftType.MORNING);

        shiftManager.setRequirement(shift, cashierRole, 2);
        shiftManager.assignEmployee(shift, cashierRole, 100);

        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, cashierRole, 100)
        );
    }
}