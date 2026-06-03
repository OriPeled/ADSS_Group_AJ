package dev.Workers.Tests;

import dev.Workers.domain.*;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Enums.*;
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
public class ShiftHandlerTest {

    private Role cashierRole;
    private Role storekeeperRole;
    private Role shiftManagerRole;

    private BranchRegistry branchRegistry;

    Branch dimona = branchRegistry.getBranchByName("Beer-Sheva");

    /**
     * Resets all singleton managers before each test
     * and rebuilds fresh instances to prevent state leakage.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(AccessHandler.class, "instance");
        resetSingleton(PreferenceHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
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
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        LocalDate date = LocalDate.of(2026, 4, 20).plusDays(dayOffset);
        shiftHandler.addShift(dimona, date, ShiftType.MORNING);
        return shiftHandler.getShift(dimona, date, ShiftType.MORNING);
    }

    private void registerEmployee(int id, String name, Role role) {
        EmployeeHandler employeeHandler = EmployeeHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        EmployeeTerms terms = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY);
        employeeHandler.add(name, id, dimona, 100000, 5000, terms, LocalDate.of(2026, 4, 1));
        employeeHandler.addRole(id, role);
        preferenceHandler.initPreferences(id);
    }

    /**
     * Verifies that a qualified and available employee
     * can be assigned successfully to a required role in a shift.
     */
    @Test
    void assignEmployee_shouldSucceedForValidEmployee() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(1, "Alice", cashierRole);
        Shift shift = createShift(0);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(1, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 1);

        assertEquals(0, shiftHandler.leftToAssign(shift, cashierRole));
        assertFalse(shiftHandler.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that assignment fails when the employee
     * does not have the required role.
     */
    @Test
    void assignEmployee_should_Fail_When_Employee_Is_Not_Qualified() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(2, "Bob", storekeeperRole);
        Shift shift = createShift(1);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(2, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.assignEmployee(shift, cashierRole, 2)
        );
    }

    /**
     * Verifies that assignment fails when the employee
     * is not available for the requested shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsNotAvailable() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(3, "Charlie", cashierRole);
        Shift shift = createShift(2);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(3, day, ShiftType.EVENING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.assignEmployee(shift, cashierRole, 3)
        );
    }

    /**
     * Verifies that an assigned employee can be replaced
     * by another employee who is qualified and available.
     */
    @Test
    void replaceEmployee_shouldReplaceAssignedEmployeeSuccessfully() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(10, "David", cashierRole);
        registerEmployee(11, "Eve", cashierRole);

        Shift shift = createShift(3);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(10, day, ShiftType.MORNING);
        preferenceHandler.update(11, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 10);
        shiftHandler.replaceEmployee(shift, 10, 11);

        assertEquals(0, shiftHandler.leftToAssign(shift, cashierRole));
        assertFalse(shiftHandler.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that replacing an assigned employee fails
     * when the new employee does not have the required role.
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeIsNotQualified() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(20, "Frank", cashierRole);
        registerEmployee(21, "Grace", storekeeperRole);

        Shift shift = createShift(4);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(20, day, ShiftType.MORNING);
        preferenceHandler.update(21, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 20);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.replaceEmployee(shift, 20, 21)
        );
    }

    /**
     * Verifies that ShiftManager correctly calculates
     * how many employees are still missing for a role in a shift.
     */
    @Test
    void leftToAssign_shouldReportMissingEmployeesCorrectly() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(30, "Hannah", cashierRole);
        Shift shift = createShift(5);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(30, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 2);
        assertEquals(2, shiftHandler.leftToAssign(shift, cashierRole));

        shiftHandler.assignEmployee(shift, cashierRole, 30);
        assertEquals(1, shiftHandler.leftToAssign(shift, cashierRole));
    }

    /**
     * Verifies that a terminated employee cannot be assigned to a shift.
     */
    @Test
    void assignEmployee_shouldFail_WhenEmployeeIsTerminated() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();
        EmployeeHandler employeeHandler = EmployeeHandler.getInstance();

        registerEmployee(60, "Liam", cashierRole);
        employeeHandler.fire(60);

        Shift shift = createShift(6);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(60, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.assignEmployee(shift, cashierRole, 60)
        );
    }

    /**
     * Verifies that a newly added employee can replace
     * another assigned employee in a shift.
     */
    @Test
    void replaceEmployee_shouldSucceedAfterAddingTwoEmployees() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(70, "Mia", cashierRole);
        registerEmployee(71, "Noah", cashierRole);

        Shift shift = createShift(7);
        DayOfWeek day = shift.getDate().getDayOfWeek();

        preferenceHandler.update(70, day, ShiftType.MORNING);
        preferenceHandler.update(71, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 70);
        shiftHandler.replaceEmployee(shift, 70, 71);

        assertEquals(0, shiftHandler.leftToAssign(shift, cashierRole));
        assertFalse(shiftHandler.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that replacement fails when the new employee
     * was added to the system and later terminated.
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeWasTerminated() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();
        EmployeeHandler employeeHandler = EmployeeHandler.getInstance();

        registerEmployee(80, "Olivia", cashierRole);
        registerEmployee(81, "Emma", cashierRole);

        Shift shift = createShift(8);
        DayOfWeek day = shift.getDate().getDayOfWeek();

        preferenceHandler.update(80, day, ShiftType.MORNING);
        preferenceHandler.update(81, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 80);
        employeeHandler.fire(81);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.replaceEmployee(shift, 80, 81)
        );
    }

    /**
     * Verifies that a terminated employee cannot receive a new role.
     */
    @Test
    void addRoleToEmployee_shouldFailWhenEmployeeIsTerminated() {
        EmployeeHandler employeeHandler = EmployeeHandler.getInstance();

        registerEmployee(90, "Sophia", cashierRole);
        employeeHandler.fire(90);

        assertThrows(IllegalArgumentException.class, () ->
                employeeHandler.addRole(90, storekeeperRole)
        );
    }

    /**
     * Verifies that replacing an employee with the same employee ID
     * is not allowed.
     */
    @Test
    void replaceEmployee_shouldFailWhenReplacingWithSameEmployee() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(110, "Lior", cashierRole);
        Shift shift = createShift(10);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(110, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, 110);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.replaceEmployee(shift, 110, 110)
        );
    }

    /**
     * Verifies that publishing a week where at least one shift has no assigned
     * shift manager throws IllegalStateException.
     */
    @Test
    void publishWeekSchedule_shouldFail_WhenShiftHasNoManager() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        LocalDate isolatedDate = LocalDate.of(2027, 6, 1);
        assertThrows(IllegalStateException.class, () ->
                shiftHandler.publishWeekSchedule(dimona, isolatedDate)
        );
    }

    /**
     * Verifies that setting the shift manager requirement to 0 throws
     * IllegalArgumentException, since every shift must have at least one.
     */
    @Test
    void setRequirement_shouldFail_WhenShiftManagerCountIsZero() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        Shift shift = createShift(20);

        /*if (role == Role.shiftManager && count < 1) {
        throw new IllegalArgumentException("Cannot set shift manager requirement below 1...");
        }*/

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.setRequirement(shift, shiftManagerRole, 0)
        );
    }

    /**
     * Verifies that an employee cannot be assigned twice
     * to the same shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeAlreadyAssignedToSameShift() {
        ShiftHandler shiftHandler = ShiftHandler.getInstance();
        PreferenceHandler preferenceHandler = PreferenceHandler.getInstance();

        registerEmployee(100, "Daniel", cashierRole);
        Shift shift = createShift(9);
        DayOfWeek day = shift.getDate().getDayOfWeek();
        preferenceHandler.update(100, day, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 2);
        shiftHandler.assignEmployee(shift, cashierRole, 100);

        assertThrows(IllegalArgumentException.class, () ->
                shiftHandler.assignEmployee(shift, cashierRole, 100)
        );
    }
}