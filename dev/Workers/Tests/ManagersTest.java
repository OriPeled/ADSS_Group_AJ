package dev.Workers.Tests;

import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified unit tests for all manager classes.
 *
 * Tested classes:
 * - EmployeeManager
 * - AccessManager
 * - RoleManager
 * - ConstraintManager
 * - Requirements
 * - Assignments
 *
 * These tests validate CRUD operations and basic business rules.
 */
public class ManagersTest {

    // =========================================================
    // Helper methods
    // =========================================================

    private Employee createEmployee(int id) {
        EmployeeTerms terms = new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2);
        return new Employee("Emp" + id, id, 111 + id, 5000, terms, LocalDate.now());
    }

    private Shift createShift() {
        return new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);
    }

    // =========================================================
    // EmployeeManager Tests
    // =========================================================

    @Test
    void employeeManager_addAndGet_shouldWork() {
        EmployeeManager manager = EmployeeManager.getInstance();
        Employee emp = createEmployee(1);

        manager.add(emp.getId(), emp);

        assertEquals(emp, manager.getById(1));
        assertTrue(manager.isEmployee(1));
    }

    @Test
    void employeeManager_remove_shouldDeactivateEmployee() {
        EmployeeManager manager = EmployeeManager.getInstance();
        Employee emp = createEmployee(2);

        manager.add(emp.getId(), emp);
        manager.remove(2);

        assertFalse(manager.getById(2).isActive());
    }

    // =========================================================
    // AccessManager Tests
    // =========================================================

    @Test
    void accessManager_registerAndLogin_shouldSucceed() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        AccessManager accessManager = AccessManager.getInstance();

        Employee emp = createEmployee(10);
        empManager.add(emp.getId(), emp);

        assertEquals(UserResponse.success, accessManager.register(10, "1234"));
        assertEquals(UserResponse.success, accessManager.login(10, "1234"));
    }

    @Test
    void accessManager_wrongPassword_shouldFail() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        AccessManager accessManager = AccessManager.getInstance();

        Employee emp = createEmployee(11);
        empManager.add(emp.getId(), emp);

        accessManager.register(11, "1234");

        assertEquals(UserResponse.wrongPassword, accessManager.login(11, "0000"));
    }

    // =========================================================
    // RoleManager Tests
    // =========================================================

    @Test
    void roleManager_addRole_shouldAssignRole() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();

        Employee emp = createEmployee(20);
        empManager.add(emp.getId(), emp);

        roleManager.addRoleToEmployee(20, Role.Cashier);

        assertTrue(roleManager.hasRole(20, Role.Cashier));
    }

    @Test
    void roleManager_getListByRole_shouldReturnCorrectEmployees() {
        EmployeeManager empManager = EmployeeManager.getInstance();
        RoleManager roleManager = RoleManager.getInstance();

        Employee emp = createEmployee(21);
        empManager.add(emp.getId(), emp);

        roleManager.addRoleToEmployee(21, Role.Cashier);

        List<Integer> result = roleManager.getListByRole(Role.Cashier);

        assertTrue(result.contains(21));
    }

    // =========================================================
    // ConstraintManager Tests
    // =========================================================

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

    @Test
    void constraintManager_isEmployeeAvailable_shouldRespectConstraint() {
        ConstraintManager cm = ConstraintManager.getInstance();

        cm.initConstraintForEmployee(31);
        cm.update(31, DayOfWeek.MONDAY, ShiftType.morning);

        assertTrue(cm.isEmployeeAvailable(31, DayOfWeek.MONDAY, ShiftType.morning));
        assertFalse(cm.isEmployeeAvailable(31, DayOfWeek.MONDAY, ShiftType.evening));
    }

    // =========================================================
    // Requirements Tests
    // =========================================================

    @Test
    void requirements_setAndCount_shouldWork() {
        Requirements req = new Requirements();
        Shift shift = createShift();

        req.set(shift, Role.Cashier, 3);

        assertEquals(3, req.countRequired(shift, Role.Cashier));
    }

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

    @Test
    void assignments_add_shouldAssignEmployee() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 100);

        assertTrue(assignments.isAssignedToRole(shift, Role.Cashier, 100));
    }

    @Test
    void assignments_remove_shouldUnassignEmployee() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 101);
        assignments.remove(shift, Role.Cashier, 101);

        assertFalse(assignments.isAssignedToRole(shift, Role.Cashier, 101));
    }

    @Test
    void assignments_countAssigned_shouldReturnCorrectCount() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 1);
        assignments.add(shift, Role.Cashier, 2);

        assertEquals(2, assignments.countAssigned(shift, Role.Cashier));
    }

    @Test
    void assignments_getEmployeeRole_shouldReturnCorrectRole() {
        Assignments assignments = new Assignments();
        Shift shift = createShift();

        assignments.init(shift);
        assignments.add(shift, Role.Cashier, 200);

        assertEquals(Role.Cashier, assignments.getEmployeeRole(shift, 200));
    }
}