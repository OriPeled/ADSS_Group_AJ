package dev.Tests;

import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.service.EmployeeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EmployeeService.
 */
public class EmployeeServiceTest {
    private EmployeeService employeeService;
    private Branch branch;
    private static int nextId = 100000;

    @BeforeAll
    static void globalSetup() {
        dev.Workers.database.DatabaseManager.eraseDatabase();
        dev.Workers.database.DatabaseInitializer.initializeDatabase();

        try (java.sql.Connection connection = dev.Workers.database.DatabaseManager.getConnection();
             java.sql.Statement statement = connection.createStatement()) {
            statement.execute("INSERT OR IGNORE INTO branches (branch_name) VALUES ('Beer-Sheva');");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to setup test database branches", e);
        }
    }

    @BeforeEach
    void setUp() {
        employeeService = EmployeeService.getInstance();

        BranchRegistry registry = BranchRegistry.getInstance();
        registry.registerBranch(new Branch("Beer-Sheva"));
        branch = registry.getBranchByName("Beer-Sheva");
    }

    /**
     * Creates a valid employee and returns its id.
     */
    private int createEmployee() {
        int id = nextId++;
        employeeService.add(
                "Employee" + id,
                id,
                branch,
                1000 + id,
                5000,
                new EmployeeTerms(
                        JobStatus.fullTime,
                        SalaryType.global,
                        2,
                        DayOfWeek.WEDNESDAY),
                LocalDate.of(2025, 1, 1));
        return id;
    }

    /**
     * Verifies that adding an employee succeeds.
     */
    @Test
    void addEmployee_shouldSucceed() {
        int id = createEmployee();
        assertTrue(employeeService.exists(id));
    }

    /**
     * Verifies that updating employee name succeeds.
     */
    @Test
    void updateName_shouldSucceed() {
        int id = createEmployee();
        employeeService.updateName(id, "David");

        assertEquals("David", employeeService.getEmployee(id).getName());
    }

    /**
     * Verifies that updating bank account succeeds.
     */
    @Test
    void updateBankAccount_shouldSucceed() {
        int id = createEmployee();
        employeeService.updateBankAccount(id, 55555);

        assertEquals(55555, employeeService.getEmployee(id).getBankAccount());
    }

    /**
     * Verifies that updating salary succeeds.
     */
    @Test
    void updateSalary_shouldSucceed() {
        int id = createEmployee();
        employeeService.updateSalary(id, 9000);

        assertEquals(9000, employeeService.getEmployee(id).getSalary(), 0.001);
    }

    /**
     * Verifies that updating rest days succeeds.
     */
    @Test
    void updateRestDays_shouldSucceed() {
        int id = createEmployee();
        employeeService.updateRestDays(id, 4);

        assertEquals(4, employeeService.getEmployee(id).getTerms().getRestDays());
    }

    /**
     * Verifies that updating day off succeeds.
     */
    @Test
    void updateDayOff_shouldSucceed() {
        int id = createEmployee();
        employeeService.updateDayOff(id, DayOfWeek.FRIDAY);

        assertEquals(DayOfWeek.FRIDAY, employeeService.getEmployee(id).getTerms().getDayOff());
    }

    /**
     * Verifies that promoting an employee makes him a manager.
     */
    @Test
    void promoteEmployee_shouldBecomeManager() {
        int id = createEmployee();
        employeeService.promoteDemote(id);

        assertTrue(employeeService.getEmployee(id).isManager());
    }

    /**
     * Verifies that firing an employee terminates him.
     */
    @Test
    void fireEmployee_shouldTerminateEmployee() {
        int id = createEmployee();
        employeeService.fire(id);

        assertTrue(employeeService.getEmployee(id).isTerminated());
    }

    /**
     * Verifies that rehiring a terminated employee activates him.
     */
    @Test
    void rehireEmployee_shouldReactivateEmployee() {
        int id = createEmployee();
        employeeService.fire(id);
        employeeService.rehire(id);

        assertFalse(employeeService.getEmployee(id).isTerminated());
    }

    /**
     * Verifies that updating salary with a negative value fails.
     */
    @Test
    void updateSalary_negativeSalary_shouldFail() {
        int id = createEmployee();

        assertThrows(IllegalArgumentException.class, () -> employeeService.updateSalary(id, -100));
    }
}