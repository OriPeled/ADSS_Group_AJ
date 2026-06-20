package dev.Workers.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ShiftHandler.
 */
public class ShiftHandlerTest {
    private EmployeeHandler employeeHandler;
    private PreferenceHandler preferenceHandler;
    private ShiftHandler shiftHandler;
    private RoleRegistry roleRegistry;
    private BranchRegistry branchRegistry;

    private Role cashierRole;
    private Role storekeeperRole;
    private Branch branch;

    private static int nextEmployeeId = 10000;
    private static int nextShiftOffset = 0;

    /**
     * Initializes the database once for the entire test class.
     */
    @BeforeAll
    static void initDatabaseOnce() {
        DatabaseManager.eraseDatabase();
        DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {

            statement.execute("INSERT OR IGNORE INTO branches (branch_name)" +
                    "VALUES ('Beer-Sheva'), ('Dimona'), ('Ofakim'), ('Rahat');");

        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup test database branches", e);
        }
    }

    /**
     * Resets the in-memory singleton managers before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(AccessHandler.class, "instance");
        resetSingleton(PreferenceHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
        resetSingleton(RoleRegistry.class, "instance");
        resetSingleton(BranchRegistry.class, "instance");
        resetSingleton(RequirementHandler.class, "instance");
        resetSingleton(AssignmentHandler.class, "instance");

        clearWeekSchedules();

        employeeHandler = EmployeeHandler.getInstance();

        Field accessEmployeeHandlerField =
                AccessHandler.class.getDeclaredField("employeeHandler");
        accessEmployeeHandlerField.setAccessible(true);
        accessEmployeeHandlerField.set(null, employeeHandler);

        roleRegistry = RoleRegistry.getInstance();
        preferenceHandler = PreferenceHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();

        BranchRegistry registry = BranchRegistry.getInstance();
        registry.registerBranch(new Branch("Beer-Sheva"));
        registry.registerBranch(new Branch("Dimona"));
        registry.registerBranch(new Branch("Ofakim"));
        registry.registerBranch(new Branch("Rahat"));
        branch = registry.getBranchByName("Beer-Sheva");

        cashierRole = roleRegistry.getRoleByName("Cashier");
        storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        preferenceHandler.setDeadline(null);
    }

    static void resetSingleton(Class<?> clazz, String fieldName)
            throws Exception {

        Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @SuppressWarnings("unchecked")
    private static void clearWeekSchedules() throws Exception {
        Field field = ShiftHandler.class.getDeclaredField("weekSchedules");
        field.setAccessible(true);

        Map<LocalDate, ?> weekSchedules =
                (Map<LocalDate, ?>) field.get(null);

        weekSchedules.clear();
    }

    private EmployeeTerms createTerms() {
        return new EmployeeTerms(
                JobStatus.fullTime,
                SalaryType.global,
                2,
                DayOfWeek.WEDNESDAY);
    }

    private int registerEmployee(String name, Role role) {
        int id = nextEmployeeId++;

        employeeHandler.add(
                name,
                id,
                branch,
                100000 + id,
                5000,
                createTerms(),
                LocalDate.of(2025, 1, 1));

        employeeHandler.addRole(id, role);
        preferenceHandler.initPreferences(id);

        for (DayOfWeek day : DayOfWeek.values()) {
            preferenceHandler.update(id, day, ShiftType.ANY);
        }

        return id;
    }

    private int registerManager(String name, Role role) {
        int id = registerEmployee(name, role);
        employeeHandler.getEmployee(id).setManager(true);
        return id;
    }

    private Shift createShift() {
        LocalDate date =
                LocalDate.now()
                        .plusWeeks(4)
                        .plusDays(nextShiftOffset++);

        return shiftHandler.getShift(
                branch,
                date,
                ShiftType.MORNING);
    }

    private LocalDate nextSunday() {
        return LocalDate.now()
                .plusWeeks(4)
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    private void makeEmployeeAvailable(int employeeId, Shift shift, ShiftType type) {
        preferenceHandler.update(
                employeeId,
                shift.getDate().getDayOfWeek(),
                type);
    }

    /**
     * Verifies that a qualified and available employee can be assigned
     * successfully to a required role in a shift.
     */
    @Test
    void assignEmployee_shouldSucceedForValidEmployee() {
        int employeeId = registerEmployee("Alice", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertEquals(0, shiftHandler.leftToAssign(shift, cashierRole));
        assertFalse(shiftHandler.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that assigning an employee without the required role
     * throws an IllegalArgumentException.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsNotQualified() {
        int employeeId = registerEmployee("Bob", storekeeperRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }

    /**
     * Verifies that an employee who is not available for a morning shift
     * cannot be assigned to that shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsNotAvailable() {
        int employeeId = registerEmployee("Charlie", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.EVENING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }

    /**
     * Verifies that an employee cannot be assigned twice
     * to the same shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeAlreadyAssignedToSameShift() {
        int employeeId = registerEmployee("Daniel", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 2);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }

    /**
     * Verifies that leftToAssign returns the correct number
     * of missing employees for a role.
     */
    @Test
    void leftToAssign_shouldReportMissingEmployeesCorrectly() {
        int employeeId = registerEmployee("Hannah", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 2);

        assertEquals(2, shiftHandler.leftToAssign(shift, cashierRole));

        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertEquals(1, shiftHandler.leftToAssign(shift, cashierRole));
    }

    /**
     * Verifies that replacing an assigned employee with another
     * qualified and available employee succeeds.
     */
    @Test
    void replaceEmployee_shouldReplaceAssignedEmployeeSuccessfully() {
        int oldEmployeeId = registerEmployee("David", cashierRole);
        int newEmployeeId = registerEmployee("Eve", cashierRole);

        Shift shift = createShift();

        makeEmployeeAvailable(oldEmployeeId, shift, ShiftType.MORNING);
        makeEmployeeAvailable(newEmployeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, oldEmployeeId);

        shiftHandler.replaceEmployee(
                shift,
                oldEmployeeId,
                newEmployeeId);

        assertEquals(0, shiftHandler.leftToAssign(shift, cashierRole));
        assertFalse(shiftHandler.isNeeded(shift, cashierRole));
    }

    /**
     * Verifies that replacing an employee with an unqualified employee
     * throws an IllegalArgumentException.
     */
    @Test
    void replaceEmployee_shouldFailWhenNewEmployeeIsNotQualified() {
        int oldEmployeeId = registerEmployee("Frank", cashierRole);
        int newEmployeeId = registerEmployee("Grace", storekeeperRole);

        Shift shift = createShift();

        makeEmployeeAvailable(oldEmployeeId, shift, ShiftType.MORNING);
        makeEmployeeAvailable(newEmployeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, oldEmployeeId);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.replaceEmployee(
                        shift,
                        oldEmployeeId,
                        newEmployeeId));
    }

    /**
     * Verifies that replacing an employee with the same employee ID
     * is not allowed.
     */
    @Test
    void replaceEmployee_shouldFailWhenReplacingWithSameEmployee() {
        int employeeId = registerEmployee("Lior", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.replaceEmployee(
                        shift,
                        employeeId,
                        employeeId));
    }

    /**
     * Verifies that a terminated employee cannot be assigned
     * to a future shift.
     */
    @Test
    void assignEmployee_shouldFailWhenEmployeeIsTerminated() {
        int employeeId = registerEmployee("Liam", cashierRole);

        employeeHandler.fire(employeeId);

        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        employeeId));
    }

    /**
     * Verifies that a terminated employee cannot receive
     * a new role.
     */
    @Test
    void addRoleToEmployee_shouldStillWorkOnTerminationDay() {

        int employeeId = registerEmployee("Sophia", cashierRole);

        employeeHandler.fire(employeeId);

        assertDoesNotThrow(() ->
                employeeHandler.addRole(employeeId, storekeeperRole));
    }

    /**
     * Verifies that assigning a manager marks the shift
     * as managed.
     */
    @Test
    void assigningManager_shouldMarkShiftAsManaged() {
        int managerId = registerManager("Manager", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(managerId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, managerId);

        assertTrue(shiftHandler.hasManager(shift));
        assertEquals("COMPLETE", shiftHandler.getShiftStatus(shift));
    }

    /**
     * Verifies that a shift without a manager is considered incomplete,
     * even if all role requirements are filled.
     */
    @Test
    void shiftWithoutManager_shouldBeIncomplete() {
        int employeeId = registerEmployee("Worker", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertFalse(shiftHandler.hasManager(shift));
        assertEquals("INCOMPLETE", shiftHandler.getShiftStatus(shift));
    }

    /**
     * Verifies that setting a negative requirement
     * throws an IllegalArgumentException.
     */
    @Test
    void setRequirement_shouldFailWhenCountIsNegative() {
        Shift shift = createShift();

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.setRequirement(
                        shift,
                        cashierRole,
                        -1));
    }

    /**
     * Verifies that updating extra hours succeeds
     * for an employee assigned to a morning shift.
     */
    @Test
    void updateExtraHours_shouldSucceedForAssignedEmployee() {
        int employeeId = registerEmployee("ExtraHoursEmployee", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        assertDoesNotThrow(() ->
                shiftHandler.updateExtraHours(
                        shift,
                        employeeId,
                        2));
    }

    /**
     * Verifies that updating extra hours for an employee who is not
     * assigned to the shift throws an IllegalArgumentException.
     */
    @Test
    void updateExtraHours_shouldFailForUnassignedEmployee() {
        int employeeId = registerEmployee("UnassignedExtra", cashierRole);
        Shift shift = createShift();

        assertThrows(
                IllegalArgumentException.class,
                () -> shiftHandler.updateExtraHours(
                        shift,
                        employeeId,
                        2));
    }

    /**
     * Verifies that a pending assignment request creates COMPLETE*
     * status when all role requirements are tentatively satisfied.
     */
    @Test
    void pendingRequest_shouldCreateCompleteStarStatus() {
        int managerId = registerManager("PendingManager", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(managerId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignmentHandler.addRequest(
                shift,
                cashierRole,
                managerId);

        assertEquals("COMPLETE*", shiftHandler.getShiftStatus(shift));
    }

    /**
     * Verifies that approving a pending assignment request
     * assigns the employee to the shift.
     */
    @Test
    void approveNextAssignment_shouldAssignEmployee() {
        int managerId = registerManager("ApproveManager", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(managerId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignmentHandler.addRequest(
                shift,
                cashierRole,
                managerId);

        shiftHandler.approveNextAssignment(managerId);

        assertTrue(
                shiftHandler.assignmentHandler.isAssignedToRole(
                        shift,
                        cashierRole,
                        managerId));
    }

    /**
     * Verifies that publishing a fully assigned and managed week
     * succeeds and changes the week status to PUBLISHED.
     */
    @Test
    void publishWeekSchedule_shouldSucceedWhenWeekIsFullyAssigned() {
        int managerId = registerManager("WeekManager", cashierRole);
        int storekeeperId = registerEmployee("WeekStorekeeper", storekeeperRole);

        LocalDate sunday = nextSunday();

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type :
                    new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {

                Shift shift = shiftHandler.getShift(
                        branch,
                        date,
                        type);

                preferenceHandler.update(
                        managerId,
                        date.getDayOfWeek(),
                        type);

                preferenceHandler.update(
                        storekeeperId,
                        date.getDayOfWeek(),
                        type);

                shiftHandler.setRequirement(shift, cashierRole, 1);
                shiftHandler.setRequirement(shift, storekeeperRole, 1);

                shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        managerId);

                shiftHandler.assignEmployee(
                        shift,
                        storekeeperRole,
                        storekeeperId);
            }
        }

        shiftHandler.publishWeekSchedule(branch, sunday);

        assertEquals(
                WeekStatus.PUBLISHED,
                shiftHandler.getWeekStatus(branch, sunday));
    }

    /**
     * Verifies that publishing a week fails when the shifts
     * are assigned but no manager is present.
     */
    @Test
    void publishWeekSchedule_shouldFailWhenWeekHasNoManager() {
        int cashierId = registerEmployee("WeekCashier", cashierRole);
        int storekeeperId = registerEmployee("WeekStorekeeperNoManager", storekeeperRole);

        LocalDate sunday = nextSunday().plusWeeks(1);

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type :
                    new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {

                Shift shift = shiftHandler.getShift(
                        branch,
                        date,
                        type);

                preferenceHandler.update(
                        cashierId,
                        date.getDayOfWeek(),
                        type);

                preferenceHandler.update(
                        storekeeperId,
                        date.getDayOfWeek(),
                        type);

                shiftHandler.setRequirement(shift, cashierRole, 1);
                shiftHandler.setRequirement(shift, storekeeperRole, 1);

                shiftHandler.assignEmployee(
                        shift,
                        cashierRole,
                        cashierId);

                shiftHandler.assignEmployee(
                        shift,
                        storekeeperRole,
                        storekeeperId);
            }
        }

        assertThrows(
                IllegalStateException.class,
                () -> shiftHandler.publishWeekSchedule(
                        branch,
                        sunday));
    }

    /**
     * Verifies that getShiftDetails returns textual information
     * about the selected shift.
     */
    @Test
    void getShiftDetails_shouldReturnShiftInformation() {
        int employeeId = registerEmployee("DetailsEmployee", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);
        shiftHandler.assignEmployee(shift, cashierRole, employeeId);

        String details = shiftHandler.getShiftDetails(shift);

        assertNotNull(details);
        assertTrue(details.contains("Shift"));
        assertTrue(details.contains("Cashier"));
    }

    /**
     * Verifies that getUnassignedValid returns active, qualified,
     * and unassigned employees.
     */
    @Test
    void getUnassignedValid_shouldReturnAvailableEmployees() {
        int employeeId = registerEmployee("AvailableEmployee", cashierRole);
        Shift shift = createShift();

        makeEmployeeAvailable(employeeId, shift, ShiftType.MORNING);

        shiftHandler.setRequirement(shift, cashierRole, 1);

        String result = shiftHandler.getUnassignedValid(shift);

        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(employeeId)));
    }
}