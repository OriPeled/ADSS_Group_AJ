package dev.Workers.Tests;

import dev.Workers.Service.AccessService;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified unit tests for manager classes in the workers system.
 *
 * Tested classes:
 * - EmployeeManager
 * - AccessManager
 * - RoleManager
 * - ConstraintManager
 * - Requirements
 * - Assignments
 */
public class ManagersTest {

    // =========================================================
    // Helper methods
    // =========================================================

    /**
     * Creates default employment terms for test employees.
     *
     * @return default employee terms
     */
    private EmployeeTerms createTerms() {
        return new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2);
    }

    /**
     * Adds a test employee to the employee manager using the current API.
     *
     * @param manager employee manager instance
     * @param id      employee id
     */
    private void addEmployee(EmployeeManager manager, int id) {
        manager.add(
                "Emp" + id,
                id,
                111 + id,
                5000,
                createTerms(),
                LocalDate.now()
        );
    }

    /**
     * Creates a fixed test shift.
     *
     * @return test shift
     */
    private Shift createShift() {
        return new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);
    }

    // =========================================================
    // EmployeeManager Tests
    // =========================================================

    /**
     * Verifies that adding an employee stores the employee in the system
     * and allows retrieving it by id.
     *
     * Scenario:
     * - A new employee is added to the employee manager
     *
     * Expected result:
     * - getById returns the employee
     * - isEmployee returns true
     */
    @Test
    void employeeManager_addAndGet_shouldWork() {
        EmployeeManager manager = EmployeeManager.getInstance();

        addEmployee(manager, 1);

        assertNotNull(manager.getById(1));
        assertEquals(1, manager.getById(1).getId());
        assertTrue(manager.isEmployee(1));
    }

    /**
     * Verifies that removing an employee does not delete the employee object,
     * but marks the employee as inactive.
     *
     * Scenario:
     * - A new employee is added
     * - The employee is removed
     *
     * Expected result:
     * - The employee remains in the system
     * - The employee is no longer active
     */
    @Test
    void employeeManager_remove_shouldDeactivateEmployee() {
        EmployeeManager manager = EmployeeManager.getInstance();

        addEmployee(manager, 2);
        manager.remove(2);

        assertFalse(manager.getById(2).isActive());
    }

    // =========================================================
    // AccessManager Tests
    // =========================================================

    /**
     * Verifies that registration fails when the password is shorter than 4 characters.
     *
     * Scenario:
     * - A registration attempt is made with a short password
     *
     * Expected result:
     * - IllegalArgumentException is thrown
     * - The exception message explains the password length rule
     */
    @Test
    void register_shortPassword_shouldThrowException() {
        AccessService accessService = AccessService.getInstance();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accessService.Register(1, "123")
        );

        assertEquals("Password must be at least 4 characters long.", exception.getMessage());
    }

    /**
     * Verifies that login fails when the user enters an incorrect password.
     *
     * Scenario:
     * - An employee exists in the system
     * - The employee is registered with a valid password
     * - A login attempt is made with the wrong password
     *
     * Expected result:
     * - IllegalArgumentException is thrown
     * - The exception message indicates a wrong password
     */
    @Test
    void accessManager_wrongPassword_shouldFail() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        AccessManager accessManager = AccessManager.getInstance();

        addEmployee(empManager, 11);
        accessManager.register(11, "1234");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accessManager.login(11, "0000")
        );

        assertEquals("Wrong password.", exception.getMessage());
    }

    // =========================================================
    // RoleManager Tests
    // =========================================================

    /**
     * Verifies that adding a role to an employee updates the employee's qualifications.
     *
     * Scenario:
     * - A new employee is added
     * - A role is assigned to the employee
     *
     * Expected result:
     * - hasRole returns true for the assigned role
     */
    @Test
    void roleManager_addRole_shouldAssignRole() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();

        addEmployee(empManager, 20);
        roleManager.addRoleToEmployee(20, Role.Cashier);

        assertTrue(roleManager.hasRole(20, Role.Cashier));
    }

    /**
     * Verifies that querying employees by role returns employees
     * who were assigned that role.
     *
     * Scenario:
     * - A new employee is added
     * - The employee is assigned a role
     * - The list of employees for that role is requested
     *
     * Expected result:
     * - The employee id appears in the returned list
     */
    @Test
    void roleManager_getListByRole_shouldReturnCorrectEmployees() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();

        addEmployee(empManager, 21);
        roleManager.addRoleToEmployee(21, Role.Cashier);

        List<Integer> result = roleManager.getListByRole(Role.Cashier);

        assertTrue(result.contains(21));
    }

    // =========================================================
    // ConstraintManager Tests
    // =========================================================

    /**
     * Verifies that updating an employee constraint changes
     * the employee's availability for the specified day.
     *
     * Scenario:
     * - Constraint storage is initialized for an employee
     * - The employee's Monday constraint is updated to morning
     *
     * Expected result:
     * - The stored constraint for Monday is morning
     */
    @Test
    void constraintManager_update_shouldChangeConstraint() {
        ConstraintManager cm = ConstraintManager.getInstance();

        cm.initConstraintForEmployee(30);
        cm.update(30, DayOfWeek.MONDAY, ShiftType.morning);

        assertEquals(
                ShiftType.morning,
                cm.getConstraints(30).getShiftType(DayOfWeek.MONDAY)
        );
    }

    /**
     * Verifies that employee availability reflects the stored constraint.
     *
     * Scenario:
     * - Constraint storage is initialized for an employee
     * - Monday is set to morning availability
     *
     * Expected result:
     * - The employee is available for Monday morning
     * - The employee is not available for Monday evening
     */
    @Test
    void constraintManager_isEmployeeAvailable_shouldRespectConstraint() {
        ConstraintManager cm = ConstraintManager.getInstance();

        cm.initConstraintForEmployee(31);
        cm.update(31, DayOfWeek.MONDAY, ShiftType.morning);

        assertTrue(cm.isEmployeeAvailable(31, DayOfWeek.MONDAY, ShiftType.morning));
        assertFalse(cm.isEmployeeAvailable(31, DayOfWeek.MONDAY, ShiftType.evening));
    }

    /**
     * Verifies that updating constraints after the deadline is not allowed.
     *
     * Scenario:
     * - Constraint storage is initialized for an employee
     * - The deadline is set to a day before today
     * - An update attempt is made after the deadline
     *
     * Expected result:
     * - RuntimeException is thrown
     */
    @Test
    void constraintManager_update_shouldFailAfterDeadline() {
        ConstraintManager cm = ConstraintManager.getInstance();
        cm.initConstraintForEmployee(32);

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        DayOfWeek deadline = today.minus(1);
        cm.setDeadline(deadline);

        assertThrows(RuntimeException.class, () ->
                cm.update(32, DayOfWeek.MONDAY, ShiftType.morning)
        );
    }

    // =========================================================
    // Requirements Tests
    // =========================================================

    /**
     * Verifies that setting a staffing requirement for a role
     * stores the correct required amount.
     *
     * Scenario:
     * - A requirement is set for Cashier on a shift
     *
     * Expected result:
     * - countRequired returns the configured amount
     */
    @Test
    void requirements_setAndCount_shouldWork() {
        Requirements req = new Requirements();
        Shift shift = createShift();

        req.set(shift, Role.Cashier, 3);

        assertEquals(3, req.countRequired(shift, Role.Cashier));
    }

    /**
     * Verifies that removing a staffing requirement deletes
     * the requirement for that role.
     *
     * Scenario:
     * - A requirement is set for Cashier on a shift
     * - The requirement is removed
     *
     * Expected result:
     * - countRequired returns 0
     */
    @Test
    void requirements_remove_shouldDeleteRole() {
        Requirements req = new Requirements();
        Shift shift = createShift();

        req.set(shift, Role.Cashier, 3);
        req.remove(shift, Role.Cashier);

        assertEquals(0, req.countRequired(shift, Role.Cashier));
    }

    // =========================================================
    // Assignments Tests
    // =========================================================

    /**
     * Verifies that assigning an employee to a role in a shift
     * stores the assignment correctly.
     *
     * Scenario:
     * - Assignment storage is initialized for a shift
     * - An employee is assigned to the Cashier role
     *
     * Expected result:
     * - isAssignedToRole returns true
     */
    @Test
    void assignments_add_shouldAssignEmployee() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 100);

        assertTrue(assignments.isAssignedToRole(shift, Role.Cashier, 100));
    }

    /**
     * Verifies that removing an employee from a shift role
     * clears the stored assignment.
     *
     * Scenario:
     * - Assignment storage is initialized for a shift
     * - An employee is assigned to a role
     * - The employee is removed from that role
     *
     * Expected result:
     * - isAssignedToRole returns false
     */
    @Test
    void assignments_remove_shouldUnassignEmployee() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 101);
        assignments.remove(shift, Role.Cashier, 101);

        assertFalse(assignments.isAssignedToRole(shift, Role.Cashier, 101));
    }

    /**
     * Verifies that counting assigned employees for a role
     * returns the correct number.
     *
     * Scenario:
     * - Assignment storage is initialized for a shift
     * - Two employees are assigned to the same role
     *
     * Expected result:
     * - countAssigned returns 2
     */
    @Test
    void assignments_countAssigned_shouldReturnCorrectCount() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 1);
        assignments.add(shift, Role.Cashier, 2);

        assertEquals(2, assignments.countAssigned(shift, Role.Cashier));
    }

    /**
     * Verifies that querying an employee's role in a shift
     * returns the correct assigned role.
     *
     * Scenario:
     * - Assignment storage is initialized for a shift
     * - An employee is assigned to the Cashier role
     *
     * Expected result:
     * - getEmployeeRole returns Cashier
     */
    @Test
    void assignments_getEmployeeRole_shouldReturnCorrectRole() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 200);

        assertEquals(Role.Cashier, assignments.getEmployeeRole(shift, 200));
    }
}