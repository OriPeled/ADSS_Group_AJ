package dev.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
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
    private EmployeeHandler employeeHandler;
    private AccessHandler accessHandler;
    private PreferenceHandler preferenceHandler;
    private ShiftHandler shiftHandler;
    private RoleRegistry roleRegistry;
    private BranchRegistry branchRegistry;
    private Role cashier;
    private Role storekeeper;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.eraseDatabase();

        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(AccessHandler.class, "instance");
        resetSingleton(PreferenceHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
        resetSingleton(RoleRegistry.class, "instance");
        resetSingleton(BranchRegistry.class, "instance");
        resetSingleton(RequirementHandler.class, "instance");
        resetSingleton(dev.Workers.service.EmployeeService.class, "instance");
        resetSingleton(dev.Workers.service.AccessService.class, "instance");
        resetSingleton(dev.Workers.service.ShiftService.class, "instance");
        resetSingleton(dev.Workers.service.RequirementService.class, "instance");
        resetSingleton(dev.Workers.service.PreferenceService.class, "instance");

        DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR IGNORE INTO branches (branch_name) VALUES ('Beer-Sheva'), ('Dimona'), ('Ofakim'), ('Rahat');");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup test database branches", e);
        }
        branchRegistry = BranchRegistry.getInstance();
        branchRegistry.registerBranch(new Branch("Beer-Sheva"));
        branchRegistry.registerBranch(new Branch("Dimona"));
        branchRegistry.registerBranch(new Branch("Ofakim"));
        branchRegistry.registerBranch(new Branch("Rahat"));

        employeeHandler = EmployeeHandler.getInstance();
        Field accessEmployeeHandlerField = AccessHandler.class.getDeclaredField("employeeHandler");
        accessEmployeeHandlerField.setAccessible(true);
        accessEmployeeHandlerField.set(null, employeeHandler);

        roleRegistry = RoleRegistry.getInstance();
        preferenceHandler = PreferenceHandler.getInstance();
        accessHandler = AccessHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();
        cashier = roleRegistry.getRoleByName("Cashier");
        storekeeper = roleRegistry.getRoleByName("Storekeeper");
        preferenceHandler.setDeadline(null);
    }

    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field instanceField = clazz.getDeclaredField(fieldName);
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    /**
     * Adds a valid active employee to the system.
     */
    private void addEmployee(int id) {
        employeeHandler.add(
                "Employee" + id,
                id,
                branchRegistry.getBranchByName("Ofakim"),
                1000 + id,
                5000,
                new EmployeeTerms(
                        JobStatus.fullTime,
                        SalaryType.global,
                        2,
                        DayOfWeek.WEDNESDAY),
                LocalDate.now().minusYears(1)
        );
    }

    /**
     * Initializes preferences and makes the employee available all week.
     */
    private void initConstraints(int id) {
        preferenceHandler.initPreferences(id);
        for (DayOfWeek day : DayOfWeek.values()) {
            preferenceHandler.update(id, day, ShiftType.ANY);
        }
    }

    private LocalDate getAvailableDate(int employeeId) {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == employeeHandler.getEmployee(employeeId).getTerms().getDayOff()) {
            date = date.plusDays(1);
        }
        return date;
    }

    // =========================================================
    // EmployeeManager tests
    // =========================================================

    @Test
    void employee_add_shouldSucceed() {
        addEmployee(1);
        assertTrue(employeeHandler.isEmployee(1));
        assertNotNull(employeeHandler.getEmployee(1));
    }

    @Test
    void employee_addDuplicateId_shouldFail() {
        addEmployee(1);
        assertThrows(IllegalArgumentException.class, () ->
                employeeHandler.add(
                        "AnotherEmployee",
                        1,
                        branchRegistry.getBranchByName("Ofakim"),
                        2222,
                        6000,
                        new EmployeeTerms(
                                JobStatus.fullTime,
                                SalaryType.global,
                                2,
                                DayOfWeek.WEDNESDAY),
                        LocalDate.now()
                ));
    }

    // =========================================================
    // AccessManager tests
    // =========================================================

    @Test
    void access_registerAndLogin_shouldSucceed() {
        addEmployee(1);
        accessHandler.register(1, "1234");

        assertEquals(UserResponse.success, accessHandler.login(1, "1234"));
    }

    @Test
    void access_loginWithoutRegistration_shouldReturnNotRegistered() {
        addEmployee(1);
        assertEquals(UserResponse.notRegistered, accessHandler.login(1, "1234"));
    }

    @Test
    void access_loginWrongPassword_shouldFail() {
        addEmployee(1);
        accessHandler.register(1, "1234");
        assertThrows(IllegalArgumentException.class, () -> accessHandler.login(1, "9999"));
    }

    @Test
    void access_registerShortPassword_shouldFail() {
        addEmployee(1);
        assertThrows(IllegalArgumentException.class, () -> accessHandler.register(1, "123"));
    }

    // =========================================================
    // PreferenceManager tests
    // =========================================================

    @Test
    void constraint_updateBeforeDeadline_shouldSucceed() {
        addEmployee(1);
        preferenceHandler.initPreferences(1);
        preferenceHandler.setDeadline(DayOfWeek.THURSDAY);
        assertDoesNotThrow(() ->
                preferenceHandler.update(
                        1,
                        DayOfWeek.MONDAY,
                        ShiftType.MORNING,
                        LocalDate.of(2026, 4, 20)));
    }

    @Test
    void constraint_updateAfterDeadline_shouldFail() {
        addEmployee(1);
        preferenceHandler.initPreferences(1);
        preferenceHandler.setDeadline(DayOfWeek.MONDAY);
        assertThrows(RuntimeException.class, () ->
                preferenceHandler.update(
                        1,
                        DayOfWeek.TUESDAY,
                        ShiftType.MORNING,
                        LocalDate.of(2026, 4, 21)));
    }

    @Test
    void constraint_isEmployeeAvailable_shouldReturnTrue() {
        addEmployee(1);
        preferenceHandler.initPreferences(1);
        preferenceHandler.setDeadline(null);
        preferenceHandler.update(1, DayOfWeek.MONDAY, ShiftType.MORNING);

        assertTrue(preferenceHandler.isEmployeeAvailable(1, DayOfWeek.MONDAY, ShiftType.MORNING));
    }

    // =========================================================
    // RoleManager tests
    // =========================================================

    @Test
    void role_addRole_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        assertTrue(cashier.isQualified(1));
    }

    @Test
    void role_addDuplicateRole_shouldFail() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        assertThrows(IllegalArgumentException.class, () -> employeeHandler.addRole(1, cashier));
    }

    @Test
    void role_removeRole_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        employeeHandler.removeRole(1, cashier);
        assertFalse(cashier.isQualified(1));
    }

    // =========================================================
    // ShiftManager tests
    // =========================================================

    @Test
    void shift_assignEmployee_shouldSucceed() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        initConstraints(1);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 1);
        shiftHandler.assignEmployee(shift, cashier, 1);

        assertFalse(shiftHandler.isNeeded(shift, cashier));
    }

    @Test
    void shift_assignEmployeeWithoutRole_shouldFail() {
        addEmployee(1);
        initConstraints(1);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 1);

        assertThrows(IllegalArgumentException.class, () -> shiftHandler.assignEmployee(shift, cashier, 1));
    }

    @Test
    void shift_replaceEmployee_shouldSucceed() {
        addEmployee(1);
        addEmployee(2);
        employeeHandler.addRole(1, cashier);
        employeeHandler.addRole(2, cashier);
        initConstraints(1);
        initConstraints(2);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 1);
        shiftHandler.assignEmployee(shift, cashier, 1);
        shiftHandler.replaceEmployee(shift, 1, 2);

        assertFalse(shiftHandler.isNeeded(shift, cashier));
    }

    @Test
    void shift_assigningManagerShouldMarkShiftAsManaged() {
        addEmployee(1);
        employeeHandler.getEmployee(1).setManager(true);
        employeeHandler.addRole(1, cashier);
        initConstraints(1);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 1);
        shiftHandler.assignEmployee(shift, cashier, 1);

        assertTrue(shiftHandler.hasManager(shift));
    }

    @Test
    void shiftWithoutManagerShouldNotBeComplete() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        initConstraints(1);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 1);
        shiftHandler.assignEmployee(shift, cashier, 1);

        assertFalse(shiftHandler.hasManager(shift));
        assertEquals("INCOMPLETE", shiftHandler.getShiftStatus(shift));
    }

    @Test
    void shift_duplicateAssignmentShouldFail() {
        addEmployee(1);
        employeeHandler.addRole(1, cashier);
        initConstraints(1);
        LocalDate date = getAvailableDate(1);

        Shift shift = shiftHandler.getShift(
                employeeHandler.getEmployee(1).getBranch(),
                date,
                ShiftType.MORNING);
        shiftHandler.setRequirement(shift, cashier, 2);
        shiftHandler.assignEmployee(shift, cashier, 1);

        assertThrows(IllegalArgumentException.class, () -> shiftHandler.assignEmployee(shift, cashier, 1));
    }

    @Test
    void shift_publishWeekSchedule_shouldSucceedWhenWeekIsFullyAssigned() {
        addEmployee(1);
        addEmployee(2);
        employeeHandler.getEmployee(1).setManager(true);
        employeeHandler.addRole(1, cashier);
        employeeHandler.addRole(2, storekeeper);
        initConstraints(1);
        initConstraints(2);
        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                Shift shift = shiftHandler.getShift(
                        employeeHandler.getEmployee(1).getBranch(),
                        date,
                        type);
                shiftHandler.setRequirement(shift, cashier, 1);
                shiftHandler.setRequirement(shift, storekeeper, 1);
                shiftHandler.assignEmployee(shift, cashier, 1);
                shiftHandler.assignEmployee(shift, storekeeper, 2);
            }
        }
        shiftHandler.publishWeekSchedule(employeeHandler.getEmployee(1).getBranch(), sunday);

        assertEquals(
                WeekStatus.PUBLISHED,
                shiftHandler.getWeekStatus(employeeHandler.getEmployee(1).getBranch(), sunday));
    }

    @Test
    void shift_publishWeekScheduleWithoutManager_shouldFail() {
        addEmployee(1);
        addEmployee(2);
        employeeHandler.addRole(1, cashier);
        employeeHandler.addRole(2, storekeeper);
        initConstraints(1);
        initConstraints(2);
        LocalDate sunday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                Shift shift = shiftHandler.getShift(
                        employeeHandler.getEmployee(1).getBranch(),
                        date,
                        type);
                shiftHandler.setRequirement(shift, cashier, 1);
                shiftHandler.setRequirement(shift, storekeeper, 1);
                shiftHandler.assignEmployee(shift, cashier, 1);
                shiftHandler.assignEmployee(shift, storekeeper, 2);
            }
        }

        assertThrows(IllegalStateException.class, () -> shiftHandler.publishWeekSchedule(employeeHandler.getEmployee(1).getBranch(), sunday));
    }
}