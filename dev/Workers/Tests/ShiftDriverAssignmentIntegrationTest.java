package dev.Workers.Tests;

import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for driver assignment to shifts.
 *
 * These tests verify the interaction between:
 * EmployeeHandler, PreferenceHandler, RoleRegistry,
 * ShiftHandler and AssignmentHandler.
 *
 * The focus is on validating driver licenses and
 * assignment constraints.
 */
class ShiftDriverAssignmentIntegrationTest {
    private EmployeeHandler employeeHandler;
    private ShiftHandler shiftHandler;
    private AssignmentHandler assignmentHandler;
    private PreferenceHandler preferenceHandler;
    private RoleRegistry roleRegistry;
    private Branch branch;

    @BeforeAll
    static void globalSetup() {
        dev.Workers.database.DatabaseManager.eraseDatabase();
        dev.Workers.database.DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute("INSERT OR IGNORE INTO branches (branch_name)" +
                    "VALUES ('Beer-Sheva'), ('Dimona'), ('Ofakim'), ('Rahat');");

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup test database branches", e);
        }
    }

    /**
     * Initializes all handlers before each test.
     */
    @BeforeEach
    void setUp() {
        employeeHandler = EmployeeHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();
        assignmentHandler = shiftHandler.getAssignments();
        preferenceHandler = PreferenceHandler.getInstance();
        roleRegistry = RoleRegistry.getInstance();

        BranchRegistry registry = BranchRegistry.getInstance();
        registry.registerBranch(new Branch("Beer-Sheva"));
        registry.registerBranch(new Branch("Dimona"));
        registry.registerBranch(new Branch("Ofakim"));
        registry.registerBranch(new Branch("Rahat"));
        branch = registry.getBranchByName("Beer-Sheva");
    }

    /**
     * Creates a new employee with default settings and preferences.
     */
    private int createDriver(String name) {

        int id = 900000 + (int) (Math.random() * 100000);

        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.hourly,
                12,
                DayOfWeek.SATURDAY);

        employeeHandler.add(
                name,
                id,
                branch,
                123456,
                50,
                terms,
                LocalDate.now());

        preferenceHandler.initPreferences(id);

        return id;
    }

    /**
     * Verifies that a driver with a valid license
     * can be assigned successfully.
     */
    @Test
    void assignDriverWithValidLicense() {

        int driverId = createDriver("Driver1");

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverC);

        // Find a date on which the employee is available
        LocalDate date = LocalDate.now().plusDays(1);

        while (date.getDayOfWeek() ==
                employeeHandler.getEmployee(driverId)
                        .getTerms()
                        .getDayOff()) {

            date = date.plusDays(1);
        }

        Shift shift = shiftHandler.getShift(
                branch,
                date,
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        shiftHandler.assignEmployee(
                shift,
                driverC,
                driverId);

        assertTrue(
                assignmentHandler.isAssignedToRole(
                        shift,
                        driverC,
                        driverId));
    }

    /**
     * Verifies that a driver with license B
     * cannot be assigned to a role requiring license C.
     */
    @Test
    void assignDriverWithWrongLicenseShouldFail() {

        int driverId = createDriver("Driver2");

        Role driverB = roleRegistry.getRoleByName("Driver (B)");
        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverB);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(2),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                RuntimeException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));
    }

    /**
     * Verifies successful assignment of a driver
     * with license D.
     */
    @Test
    void assignDriverWithLicenseD() {

        int driverId = createDriver("Driver3");

        Role driverD = roleRegistry.getRoleByName("Driver (D)");

        employeeHandler.addRole(driverId, driverD);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(3),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverD, 1);
        shiftHandler.assignEmployee(shift, driverD, driverId);

        assertTrue(
                assignmentHandler.isAssignedToRole(
                        shift,
                        driverD,
                        driverId));
    }

    /**
     * Verifies that an employee without a driver role
     * cannot be assigned to a driver requirement.
     */
    @Test
    void employeeWithoutDriverRoleShouldFail() {

        int employeeId = createDriver("Worker");

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(4),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                RuntimeException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        employeeId));
    }

    /**
     * Verifies that the same employee cannot be assigned
     * more than once to the same shift.
     */
    @Test
    void employeeCanBeAssignedOnlyOnce() {

        int driverId = createDriver("Driver4");

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverC);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(5),
                ShiftType.MORNING);

        shiftHandler.setRequirementManually(shift, driverC, 2);

        shiftHandler.assignEmployee(shift, driverC, driverId);

        assertThrows(
                RuntimeException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));
    }
    /**
     * Verifies that an employee cannot be assigned
     * on his weekly day off.
     */
    @Test
    void employeeDayOffShouldPreventAssignment() {

        int driverId = 900000 + (int) (Math.random() * 100000);

        DayOfWeek dayOff = LocalDate.now().plusDays(6).getDayOfWeek();

        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.hourly,
                12,
                dayOff);

        employeeHandler.add(
                "Driver5",
                driverId,
                branch,
                654321,
                50,
                terms,
                LocalDate.now());

        preferenceHandler.initPreferences(driverId);

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverC);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(6),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                RuntimeException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));
    }
    /**
     * Verifies that assigning a driver with an incompatible license is rejected.
     */
    @Test
    void assignDriverWithIncompatibleLicenseShouldFail() {

        int driverId = createDriver("Driver");

        Role driverB = roleRegistry.getRoleByName("Driver (B)");
        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverB);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(1),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                RuntimeException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));
    }
    /**
     * Edge case test.
     *
     * Verifies that an inconsistency between an employee's actual license
     * and the role under which the employee is assigned can be detected.
     *
     * Scenario:
     * 1. Create an employee.
     * 2. Grant the employee a Driver(B) role only.
     * 3. Create a shift.
     * 4. Bypass the regular validation mechanism by inserting the employee
     *    directly into the assignment structure as Driver(C).
     * 5. Verify that the employee does not actually possess a Driver(C) role.
     *
     * This test simulates corrupted or inconsistent data that could result
     * from manual database modifications or programming errors.
     */
    @Test
    void assignedDriverMustHaveMatchingLicense() {

        int driverId = createDriver("Driver");

        Role driverB = roleRegistry.getRoleByName("Driver (B)");
        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        // Employee possesses only a B license
        employeeHandler.addRole(driverId, driverB);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(2),
                ShiftType.MORNING);

        // Bypass normal validation and create an inconsistent assignment
        assignmentHandler.add(shift, driverC, driverId);

        // Verify that the employee does not actually have a Driver(C) role
        assertFalse(
                employeeHandler.getEmployee(driverId)
                        .getRoles()
                        .contains(driverC));
    }
    /**
     * Verifies that the same employee cannot be assigned
     * twice to the same shift.
     */
    @Test
    void duplicateAssignmentShouldFail() {

        int driverId = createDriver("Driver");

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        employeeHandler.addRole(driverId, driverC);

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(5),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 2);

        // first assignment
        shiftHandler.assignEmployee(
                shift,
                driverC,
                driverId);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));

        assertTrue(
                ex.getMessage().contains("already assigned"));
    }
    private LocalDate getAvailableDate(int employeeId) {

        LocalDate date = LocalDate.now().plusDays(1);

        while (date.getDayOfWeek() ==
                employeeHandler.getEmployee(employeeId)
                        .getTerms()
                        .getDayOff()) {

            date = date.plusDays(1);
        }

        return date;
    }
    /**
     * Verifies that an employee cannot be assigned
     * to a shift belonging to another branch.
     */
    @Test
    void employeeFromDifferentBranchShouldFail() {

        Branch otherBranch =
                BranchRegistry.getInstance()
                        .getBranchByName("Dimona");

        int driverId = 900000 + (int) (Math.random() * 100000);

        EmployeeTerms terms = new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.hourly,
                12,
                DayOfWeek.SATURDAY);

        employeeHandler.add(
                "Driver6",
                driverId,
                otherBranch,
                123456,
                50,
                terms,
                LocalDate.now());

        preferenceHandler.initPreferences(driverId);

        Role driverC = roleRegistry.getRoleByName("Driver (C)");
        employeeHandler.addRole(driverId, driverC);

        Shift shift = shiftHandler.getShift(
                branch,                  // Beer-Sheva
                getAvailableDate(driverId),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        driverId));
    }
    /**
     * Verifies that assigning a non-existing employee
     * to a shift is rejected.
     */
    @Test
    void nonExistingEmployeeShouldFail() {

        Role driverC = roleRegistry.getRoleByName("Driver (C)");

        Shift shift = shiftHandler.getShift(
                branch,
                LocalDate.now().plusDays(1),
                ShiftType.MORNING);

        shiftHandler.setRequirement(shift, driverC, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        driverC,
                        999999999));
    }
}