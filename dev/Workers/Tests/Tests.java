package dev.Workers.Tests;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Shift;
import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrated Unit Tests for Workers Management System.
 * Covers ShiftService, ConstraintManager, AccessService, EmployeeManager, and RoleManager.
 * Includes Null checks and Edge cases.
 */
public class Tests {

    private ShiftService shiftService;
    private RoleService roleService;
    private ConstraintService constraintService;
    private EmployeeService employeeService;
    private AccessService accessService;

    @BeforeEach
    void setUp() {
        // Initialize instances
        shiftService = ShiftService.getInstance();
        roleService = RoleService.getInstance();
        constraintService = ConstraintService.getInstance();
        employeeService = EmployeeService.getInstance();
        accessService = AccessService.getInstance();

    }


    @Test
    @DisplayName("ShiftService: Add and retrieve a shift")
    void testAddShift() {
        LocalDate date = LocalDate.now().plusWeeks(1);
        shiftService.addShift(date, shiftType.morning);

        Shift shift = shiftService.getShift(date, shiftType.morning);
        assertNotNull(shift, "Shift should exist in the system.");
        assertEquals(date, shift.getShiftDate(), "Shift date mismatch.");
    }

  /*  @Test
    @DisplayName("ShiftService: Set and track staffing requirements")
    void testRequirements() {
        LocalDate date = LocalDate.now().plusWeeks(1);
        shiftService.addShift(date, shiftType.evening);
        Shift shift = shiftService.getShift(date, shiftType.evening);

        shiftService.setRequirement(shift, Role.Cashier, 3);
        assertEquals(3, shiftService.leftToAssign(shift, Role.Cashier),
                "The number of employees left to assign should match the requirement.");
    }*/

    @Test
    @DisplayName("ShiftService: Fail assignment for unqualified employee")
    void testUnqualifiedAssignment() {
        LocalDate date = LocalDate.now().plusDays(5);
        shiftService.addShift(date, shiftType.morning);
        Shift shift = shiftService.getShift(date, shiftType.morning);

        // Employee ID 999 is not registered with roles in RoleManager
        assertThrows(RuntimeException.class, () -> {
            shiftService.assignEmployee(shift, Role.shiftManager, 999);
        }, "Assignment should be blocked if the employee is not qualified for the role.");
    }

    // ==========================================
    // --- ORIGINAL CONSTRAINTMANAGER TESTS ---
    // ==========================================

    @Test
    @DisplayName("ConstraintManager: Verify deadline enforcement")
    void testDeadlineLogic() {
        LocalDate futureDate = LocalDate.now().plusDays(5);
        constraintService.setDeadline(futureDate);
        assertTrue(constraintService.isOnTime(LocalDate.now()),
                "Should return true when current date is before deadline.");

        LocalDate pastDate = LocalDate.now().minusDays(1);
        constraintService.setDeadline(pastDate);
        assertFalse(constraintService.isOnTime(LocalDate.now()),
                "Should return false when current date is after deadline.");
    }

    @Test
    @DisplayName("ConstraintManager: Map number to DayOfWeek")
    void testDayFromNumber() {
        assertEquals(DayOfWeek.SUNDAY, ConstraintService.getDayFromNumber(1));
        assertEquals(DayOfWeek.SATURDAY, ConstraintService.getDayFromNumber(7));

        assertThrows(IllegalArgumentException.class, () -> {
            ConstraintService.getDayFromNumber(8);
        }, "Should throw exception for days outside the 1-7 range.");
    }


    // ==========================================
    // --- NEW: ACCESS SERVICE TESTS ---
    // ==========================================

  /*  @Test
    @DisplayName("AccessService: Register and remove user")
    void testRegisterAndRemoveUser() {
        int empId = 101;
        accessService.Register(empId, "password123");

        assertTrue(accessService.isRegisteredUser(empId), "User should be registered");

        accessService.Remove(empId);
        assertFalse(accessService.isRegisteredUser(empId), "User should be removed");
    }*/

   /* @Test
    @DisplayName("AccessService: Update password for existing user")
    void testUpdatePassword() {
        int empId = 102;
        accessService.Register(empId, "oldPass");
        accessService.updatePassword(empId, "newPass");

        // Assuming there is no direct getPassword() exposed for security,
        // we mainly check that the update doesn't throw an error for an existing user.
        assertDoesNotThrow(() -> accessService.updatePassword(empId, "anotherPass"));
    }*/


    // ==========================================
    // --- NEW: EMPLOYEE MANAGER TESTS ---
    // ==========================================

    @Test
    @DisplayName("EmployeeManager: Add employee and verify existence")
    void testAddEmployee() {
        int empId = 500;
        // Adding an employee with basic valid parameters
        employeeService.add("Yossi", empId, 12345, 8000.0, null, LocalDate.now());

        assertTrue(employeeService.isEmployee(empId), "Employee should exist after being added.");
    }

    @Test
    @DisplayName("EmployeeManager: Handle negative salary gracefully")
    void testAddEmployeeNegativeSalary() {
        int empId = 501;
        // Should print an error message and NOT add the employee
        employeeService.add("Dana", empId, 12345, -500.0, null, LocalDate.now());

        assertFalse(employeeService.isEmployee(empId), "Employee with negative salary should not be added.");
    }


    // ==========================================
    // --- NEW: ROLE MANAGER TESTS ---
    // ==========================================

   /* @Test
    @DisplayName("RoleManager: Add and remove roles")
    void testRoleManagement() {
        int empId = 200;

        List<Role> roles = new ArrayList<>(Arrays.asList(Role.Cashier, Role.shiftManager));

        roleService.addFullList(empId, roles);
        assertEquals(2, roleService.getListById(empId).size(), "Employee should have 2 roles.");


        roleService.removeSingleItem(empId, Role.Cashier);
        assertEquals(1, roleService.getListById(empId).size(), "Employee should have 1 role left.");

        roleService.removeAll(empId);
        assertTrue(roleService.getListById(empId).isEmpty(), "Employee should have no roles left.");
    }*/


    // ==========================================
    // --- NEW: NULL CHECKS & EDGE CASES ---
    // ==========================================

  /*  @Test
    @DisplayName("Null Check: AccessService - Register with Null password")
    void testAccessServiceNullPassword() {
        int empId = 999;
        // According to the logic, this should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            accessService.Register(empId, null);
        }, "Registering with a null password should throw IllegalArgumentException.");
    }*/

    @Test
    @DisplayName("Null Check: ConstraintManager - isOnTime with Null date")
    void testConstraintManagerNullDate() {
        constraintService.setDeadline(LocalDate.now());
        // null.isBefore(deadline) will throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            constraintService.isOnTime(null);
        }, "Checking isOnTime with null date should throw NullPointerException.");
    }

    @Test
    @DisplayName("Null Check: ShiftService - Get shift with Null parameters")
    void testShiftServiceNullParameters() {
        // Attempting to get a shift with null parameters should either return null or throw NPE
        // Depending on standard java implementation, checking equals on null throws NPE
        assertThrows(NullPointerException.class, () -> {
            shiftService.getShift(null, null);
        }, "Calling getShift with null should result in NullPointerException.");
    }

  /*  @Test
    @DisplayName("Edge Case: AccessService - Remove non-existent user")
    void testRemoveNonExistentUser() {
        // Trying to remove a user that doesn't exist
        assertThrows(IllegalArgumentException.class, () -> {
            accessService.Remove(8888);
        }, "Removing a non-existent user should throw IllegalArgumentException.");
    }*/
}