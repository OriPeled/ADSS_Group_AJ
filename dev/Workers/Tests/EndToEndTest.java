package dev.Workers.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
import dev.Workers.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for the Workers system.
 *
 * These tests simulate real system flows through the service layer:
 * EmployeeService, PreferenceService, ShiftService, AssignmentService,
 * and RequirementService.
 *
 * The tests verify that the main system layers work together correctly.
 */
class EndToEndTest {

    private EmployeeService employeeService;
    private PreferenceService preferenceService;
    private ShiftService shiftService;
    private AssignmentService assignmentService;

    private RoleRegistry roleRegistry;
    private Branch branch;

    private Role cashierRole;
    private Role storekeeperRole;

    private int nextId;

    /**
     * Initializes a clean database and service layer before every test.
     */
    @BeforeEach
    void setUp() throws Exception {

        DatabaseManager.eraseDatabase();

        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(PreferenceHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
        resetSingleton(AssignmentHandler.class, "instance");
        resetSingleton(RequirementHandler.class, "instance");
        resetSingleton(RoleRegistry.class, "instance");
        resetSingleton(BranchRegistry.class, "instance");

        resetSingleton(EmployeeService.class, "instance");
        resetSingleton(PreferenceService.class, "instance");
        resetSingleton(ShiftService.class, "instance");
        resetSingleton(AssignmentService.class, "instance");
        resetSingleton(RequirementService.class, "instance");

        DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute(
                    "INSERT OR IGNORE INTO branches (branch_name) " +
                            "VALUES ('Beer-Sheva'), ('Dimona'), ('Ofakim'), ('Rahat');");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup branches for E2E tests", e);
        }

        BranchRegistry branchRegistry = BranchRegistry.getInstance();
        branchRegistry.registerBranch(new Branch("Beer-Sheva"));
        branchRegistry.registerBranch(new Branch("Dimona"));
        branchRegistry.registerBranch(new Branch("Ofakim"));
        branchRegistry.registerBranch(new Branch("Rahat"));

        branch = branchRegistry.getBranchByName("Beer-Sheva");

        employeeService = EmployeeService.getInstance();
        preferenceService = PreferenceService.getInstance();
        shiftService = ShiftService.getInstance();
        assignmentService = AssignmentService.getInstance();

        roleRegistry = RoleRegistry.getInstance();

        cashierRole = roleRegistry.getRoleByName("Cashier");
        storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        preferenceService.setDeadline(null);

        nextId = 700000;
    }

    /**
     * Resets singleton instances between tests.
     */
    private void resetSingleton(Class<?> clazz, String fieldName)
            throws Exception {

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }

    /**
     * Creates a valid employee through the service layer.
     */
    private int createEmployee(String name, Role role, boolean manager) {

        int id = nextId++;

        employeeService.add(
                name,
                id,
                branch,
                1000 + id,
                5000,
                new EmployeeTerms(
                        JobStatus.fullTime,
                        SalaryType.hourly,
                        2,
                        DayOfWeek.SATURDAY),
                LocalDate.of(2025, 1, 1));

        employeeService.addRole(id, role);
        employeeService.getEmployee(id).setManager(manager);

        preferenceService.initPreferences(id);

        for (DayOfWeek day : DayOfWeek.values()) {
            if (day != DayOfWeek.SATURDAY) {
                preferenceService.manualUpdate(
                        id,
                        day,
                        ShiftType.ANY);
            }
        }

        return id;
    }

    /**
     * Returns a future date that is not the employee's day off.
     */
    private LocalDate getAvailableDate(int employeeId) {

        LocalDate date = LocalDate.now().plusWeeks(4);

        while (date.getDayOfWeek() ==
                employeeService.getEmployee(employeeId)
                        .getTerms()
                        .getDayOff()) {

            date = date.plusDays(1);
        }

        return date;
    }

    /**
     * Verifies a full employee lifecycle:
     * create employee, update salary, and fire employee.
     */
    @Test
    void employeeLifecycleEndToEnd() {

        int employeeId =
                createEmployee("EmployeeLifecycle", cashierRole, false);

        assertTrue(employeeService.exists(employeeId));

        employeeService.updateSalary(employeeId, 7000);

        assertEquals(
                7000,
                employeeService.getEmployee(employeeId).getSalary(),
                0.001);

        employeeService.fire(employeeId);

        assertTrue(
                employeeService.getEmployee(employeeId).isTerminated());
    }

    /**
     * Verifies that employee preferences can be initialized
     * and updated through the service layer.
     */
    @Test
    void employeePreferencesEndToEnd() {

        int employeeId =
                createEmployee("PreferenceEmployee", cashierRole, false);

        preferenceService.manualUpdate(
                employeeId,
                DayOfWeek.MONDAY,
                ShiftType.MORNING);

        assertEquals(
                ShiftType.MORNING,
                preferenceService.display(employeeId)
                        .getShiftType(DayOfWeek.MONDAY));
    }

    /**
     * Verifies that an employee role can be added
     * and retrieved through the service layer.
     */
    @Test
    void addRoleEndToEnd() {

        int employeeId =
                createEmployee("RoleEmployee", cashierRole, false);

        employeeService.addRole(employeeId, storekeeperRole);

        assertTrue(
                employeeService.getRoles(employeeId)
                        .contains(storekeeperRole));
    }

    /**
     * Verifies that a shift can be created and retrieved.
     */
    @Test
    void createShiftEndToEnd() {

        LocalDate date = LocalDate.now().plusWeeks(4);

        Shift shift =
                shiftService.getShift(
                        branch,
                        date,
                        ShiftType.MORNING);

        assertNotNull(shift);
        assertEquals(date, shift.getDate());
        assertEquals(ShiftType.MORNING, shift.getType());
    }

    /**
     * Verifies that a requirement can be set manually
     * and then used for assigning an employee.
     */
    @Test
    void assignEmployeeEndToEnd() {

        int managerId =
                createEmployee("ManagerEmployee", cashierRole, true);

        LocalDate date = getAvailableDate(managerId);

        Shift shift =
                shiftService.getShift(
                        branch,
                        date,
                        ShiftType.MORNING);

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                managerId);

        assertTrue(
                shiftService.isShiftAssigned(shift));
    }

    /**
     * Verifies that assigning the same employee twice
     * to the same shift is rejected.
     */
    @Test
    void duplicateAssignmentEndToEnd_shouldFail() {

        int employeeId =
                createEmployee("DuplicateEmployee", cashierRole, false);

        Shift shift =
                shiftService.getShift(
                        branch,
                        getAvailableDate(employeeId),
                        ShiftType.MORNING);

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                2);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                employeeId);

        assertThrows(
                RuntimeException.class,
                () -> shiftService.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }


    /**
     * Verifies that an assigned employee
     * can be replaced by another employee.
     */
    @Test
    void replaceEmployeeEndToEnd() {

        int oldEmployeeId =
                createEmployee(
                        "OldEmployee",
                        cashierRole,
                        false);

        int newEmployeeId =
                createEmployee(
                        "NewEmployee",
                        cashierRole,
                        false);

        Shift shift =
                shiftService.getShift(
                        branch,
                        getAvailableDate(oldEmployeeId),
                        ShiftType.MORNING);

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        shiftService.assignEmployee(
                shift,
                cashierRole,
                oldEmployeeId);

        shiftService.replaceEmployee(
                shift,
                oldEmployeeId,
                newEmployeeId);

        String details =
                shiftService.getShiftDetails(shift);

        assertFalse(
                details.contains(
                        String.valueOf(oldEmployeeId)));

        assertTrue(
                details.contains(
                        String.valueOf(newEmployeeId)));
    }

    /**
     * Verifies that sending an assignment request
     * creates a pending approval for the employee.
     */
    @Test
    void assignmentRequestEndToEnd() {

        int employeeId =
                createEmployee("RequestEmployee", cashierRole, false);

        Shift shift =
                shiftService.getShift(
                        branch,
                        getAvailableDate(employeeId),
                        ShiftType.MORNING);

        shiftService.sendRequest(
                shift,
                cashierRole,
                employeeId);

        assertTrue(
                assignmentService.assignmentNeedsApproval(employeeId));

        assertTrue(
                shiftService.displayNextPendingAssignment(employeeId)
                        .contains("PENDING"));
    }

    /**
     * Verifies that a fired employee cannot be assigned
     * to a future shift.
     */
    @Test
    void firedEmployeeCannotBeAssignedEndToEnd() {

        int employeeId =
                createEmployee("FiredEmployee", cashierRole, false);

        employeeService.fire(employeeId);

        Shift shift =
                shiftService.getShift(
                        branch,
                        LocalDate.now().plusWeeks(4),
                        ShiftType.MORNING);

        shiftService.setRequirementManually(
                shift,
                cashierRole,
                1);

        assertThrows(
                RuntimeException.class,
                () -> shiftService.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }
}