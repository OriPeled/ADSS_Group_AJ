package dev.Workers.Tests;

import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.EmployeeTerms;
import dev.Workers.domain.RoleManager;
import dev.Workers.domain.ShiftManager;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.Shift;

import org.junit.jupiter.api.Test;

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

    /**
     * Creates a valid EmployeeTerms object for test employees.
     *
     * @return a default EmployeeTerms instance
     */
    private EmployeeTerms createTerms() {
        return new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2);
    }
    private Shift createShift(int dayOffset) {
        ShiftManager shiftManager = ShiftManager.getInstance();
        LocalDate date = LocalDate.of(2026, 4, 20).plusDays(dayOffset);
        shiftManager.addShift(date, ShiftType.morning);
        return shiftManager.getShift(date, ShiftType.morning);
    }
    private void registerEmployee(int id, String name, Role role) {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        Employee employee = new Employee(
                name,
                id,
                100000 + id,
                5000,
                new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2),
                LocalDate.of(2026, 4, 1)
        );

        employeeManager.add(id, employee);
        roleManager.addRoleToEmployee(id, role);
        constraintManager.initConstraintForEmployee(id);
    }
    /**
     * Verifies that a qualified and available employee
     * can be assigned successfully to a required role in a shift.
     *
     * Scenario:
     * - The employee exists in the system
     * - The employee is active
     * - The employee has the required role
     * - The employee is available for the shift
     * - The shift still requires one employee for that role
     *
     * Expected result:
     * - The employee is assigned successfully
     * - No employees remain missing for the role
     * - The role is no longer marked as needed
     */
    @Test
    void assignEmployee_shouldSucceedForValidEmployee() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();
        // Register a valid employee with the required role
        registerEmployee(1, "Alice", Role.Cashier);
        // Create a test shift
        Shift shift = createShift(0);
        // Make the employee available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(1, day, ShiftType.morning);
        // Define that the shift needs one cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Perform assignment
        shiftManager.assignEmployee(shift, Role.Cashier, 1);

        // Validate result
        assertEquals(0, shiftManager.leftToAssign(shift, Role.Cashier));
        assertFalse(shiftManager.isNeeded(shift, Role.Cashier));
    }
    /**
     * Verifies that assignment fails when the employee
     * does not have the required role.
     *
     * Case:
     * - The employee exists in the system
     * - The employee is active
     * - The employee is available for the shift
     * - The shift requires a cashier
     * - But the employee has a different role
     *
     * Expected result:
     * - assignEmployee throws IllegalArgumentException
     */
    @Test
    void assignEmployee_should_Fail_When_Employee_Is_Not_Qualified() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        // Register an employee with the wrong role
        registerEmployee(2, "Bob", Role.Storekeeper);

        // Create a test shift
        Shift shift = createShift(1);

        // Make the employee available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(2, day, ShiftType.morning);

        // Define that the shift needs one cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Verify that assignment fails because the employee is not qualified
        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, Role.Cashier, 2)
        );
    }
    /**
     * Verifies that assignment fails when the employee
     * is not available for the requested shift.
     *
     * Case:
     * - The employee exists in the system
     * - The employee is active
     * - The employee has the required role
     * - The shift requires one employee for that role
     * - But the employee is available only for a different shift type
     *
     * Expected result:
     * - assignEmployee throws IllegalArgumentException
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsNotAvailable() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        // Register an employee with the correct role
        registerEmployee(3, "Charlie", Role.Cashier);

        // Create a test shift
        Shift shift = createShift(2);

        // Make the employee unavailable for this shift:
        // the shift is morning, but the employee is available only for evening
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(3, day, ShiftType.evening);

        // Define that the shift needs one Cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Verify that assignment fails because the employee is not available
        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, Role.Cashier, 3)
        );
    }
    /**
     * Verifies that an assigned employee can be replaced
     * by another employee who is qualified and available.
     *
     * Scenario:
     * - The current employee is already assigned to the shift
     * - The new employee exists in the system
     * - The new employee has the required role
     * - The new employee is available for the shift
     * - The new employee is not already assigned to the shift
     *
     * Expected result:
     * - replaceEmployee completes successfully
     * - The role remains fully staffed after the replacement
     * - No employees remain missing for that role
     */
    @Test
    void replaceEmployee_shouldReplaceAssignedEmployeeSuccessfully() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        // Register two valid employees with the same required role
        registerEmployee(10, "David", Role.Cashier);
        registerEmployee(11, "Eve", Role.Cashier);

        // Create a test shift
        Shift shift = createShift(3);

        // Make both employees available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(10, day, ShiftType.morning);
        constraintManager.update(11, day, ShiftType.morning);

        // Define that the shift needs one cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Assign the first employee
        shiftManager.assignEmployee(shift, Role.Cashier, 10);

        // Replace the first employee with the second one
        shiftManager.replaceEmployee(shift, 10, 11);

        // Validate that the role is still fully staffed
        assertEquals(0, shiftManager.leftToAssign(shift, Role.Cashier));
        assertFalse(shiftManager.isNeeded(shift, Role.Cashier));
    }
    /**
     * Verifies that replacing an assigned employee fails
     * when the new employee does not have the required role.
     *
     * Scenario:
     * - The current employee is already assigned to the shift
     * - The new employee exists in the system
     * - The new employee is available for the shift
     * - But the new employee does not have the required role
     *
     * Expected result:
     * - replaceEmployee throws IllegalArgumentException
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeIsNotQualified() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        // Register the currently assigned employee with the correct role
        registerEmployee(20, "Frank", Role.Cashier);

        // Register the replacement employee with the wrong role
        registerEmployee(21, "Grace", Role.Storekeeper);

        // Create a test shift
        Shift shift = createShift(4);

        // Make both employees available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(20, day, ShiftType.morning);
        constraintManager.update(21, day, ShiftType.morning);

        // Define that the shift needs one Cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Assign the first employee
        shiftManager.assignEmployee(shift, Role.Cashier, 20);

        // Verify that replacement fails because the new employee is not qualified
        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.replaceEmployee(shift, 20, 21)
        );
    }
    /**
     * Verifies that ShiftManager correctly calculates
     * how many employees are still missing for a role in a shift.
     *
     * Scenario:
     * - The shift requires two employees for a role
     * - Initially, no employees are assigned
     * - Then, one employee is assigned
     *
     * Expected result:
     * - Before assignment: 2 employees are missing
     * - After assignment: 1 employee is still missing
     */
    @Test
    void leftToAssign_shouldReportMissingEmployeesCorrectly() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();

        // Register a valid employee
        registerEmployee(30, "Hannah", Role.Cashier);

        // Create a test shift
        Shift shift = createShift(5);

        // Make the employee available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(30, day, ShiftType.morning);

        // Define that the shift needs two Cashiers
        shiftManager.setRequirement(shift, Role.Cashier, 2);

        // Before assignment → 2 employees missing
        assertEquals(2, shiftManager.leftToAssign(shift, Role.Cashier));

        // Assign one employee
        shiftManager.assignEmployee(shift, Role.Cashier, 30);

        // After assignment → 1 employee missing
        assertEquals(1, shiftManager.leftToAssign(shift, Role.Cashier));
    }
    /**
     * Verifies that a terminated employee cannot be assigned to a shift.
     *
     * Scenario:
     * - The employee exists in the system
     * - The employee has the required role
     * - The employee is marked as inactive (terminated)
     * - The employee is otherwise available for the shift
     *
     * Expected result:
     * - assignEmployee throws IllegalArgumentException
     */
    @Test
    void assignEmployee_shouldFail_WhenEmployeeIsTerminated() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();
        EmployeeManager employeeManager = EmployeeManager.getInstance();

        // Register a valid employee
        registerEmployee(60, "Liam", Role.Cashier);

        // Terminate the employee
        employeeManager.remove(60);

        // Create a test shift
        Shift shift = createShift(6);

        // Make the employee available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(60, day, ShiftType.morning);

        // Define that the shift needs one Cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Verify that assignment fails because the employee is inactive
        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, Role.Cashier, 60)
        );
    }
    /**
     * Verifies that a terminated employee cannot be assigned to a shift.
     *
     * Scenario:
     * - The employee exists in the system
     * - The employee has the required role
     * - The employee is marked as inactive (terminated)
     * - The employee is otherwise available for the shift
     *
     * Expected result:
     * - assignEmployee throws IllegalArgumentException
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsTerminated() {
        ShiftManager shiftManager = ShiftManager.getInstance();
        ConstraintManager constraintManager = ConstraintManager.getInstance();
        EmployeeManager employeeManager = EmployeeManager.getInstance();

        // Register a valid employee
        registerEmployee(60, "Liam", Role.Cashier);

        // Terminate the employee
        employeeManager.remove(60);

        // Create a test shift
        Shift shift = createShift(6);

        // Make the employee available for that exact day and shift type
        DayOfWeek day = shift.getShiftDate().getDayOfWeek();
        constraintManager.update(60, day, ShiftType.morning);

        // Define that the shift needs one Cashier
        shiftManager.setRequirement(shift, Role.Cashier, 1);

        // Verify that assignment fails because the employee is inactive
        assertThrows(IllegalArgumentException.class, () ->
                shiftManager.assignEmployee(shift, Role.Cashier, 60)
        );
    }
}