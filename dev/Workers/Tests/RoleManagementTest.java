package dev.Workers.Tests;

import dev.Workers.database.DatabaseInitializer;
import dev.Workers.database.DatabaseManager;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;
import dev.Workers.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for employee role management.
 */
class RoleManagementTest {

    private EmployeeService employeeService;
    private RoleRegistry roleRegistry;
    private Branch branch;

    private Role cashierRole;
    private Role storekeeperRole;

    @BeforeEach
    void setUp() {

        DatabaseManager.eraseDatabase();
        DatabaseInitializer.initializeDatabase();

        employeeService = EmployeeService.getInstance();
        roleRegistry = RoleRegistry.getInstance();

        EmployeeHandler.getInstance()
                .getEmployees()
                .clear();
        branch = BranchRegistry.getInstance()
                .getBranchByName("Beer-Sheva");

        cashierRole = roleRegistry.getRoleByName("Cashier");
        storekeeperRole = roleRegistry.getRoleByName("Storekeeper");
    }

    /**
     * Creates a valid employee and returns his id.
     */
    private int addEmployee() {

        int id;

        do {
            id = (int) (Math.random() * 1_000_000_000);
        }
        while (employeeService.exists(id));

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

    @Test
    void addRole_shouldSucceed() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        assertTrue(employeeService.getRoles(empId).contains(cashierRole));
    }

    @Test
    void addRoleTwice_shouldFail() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.addRole(empId, cashierRole));
    }

    @Test
    void removeRole_shouldSucceed() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        employeeService.removeRole(empId, cashierRole);

        assertFalse(employeeService.getRoles(empId).contains(cashierRole));
    }

    @Test
    void removeNonExistingRole_shouldFail() {

        int empId = addEmployee();

        assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.removeRole(empId, cashierRole));
    }

    @Test
    void availableRoles_shouldNotContainAssignedRole() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        List<Role> available =
                employeeService.availableToAddRoles(empId);

        assertFalse(available.contains(cashierRole));
    }

    @Test
    void availableRoles_shouldContainOtherRole() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        List<Role> available =
                employeeService.availableToAddRoles(empId);

        assertTrue(available.contains(storekeeperRole));
    }

    @Test
    void getRoles_shouldReturnAllRoles() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);
        employeeService.addRole(empId, storekeeperRole);

        assertTrue(employeeService.getRoles(empId).contains(cashierRole));
        assertTrue(employeeService.getRoles(empId).contains(storekeeperRole));
    }

    @Test
    void getFormattedRolesList_shouldContainRoleName() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);

        String roles =
                employeeService.getFormattedRolesList(empId);

        assertNotNull(roles);
        assertTrue(roles.contains(cashierRole.getName()));
    }

    @Test
    void getFormattedAvailableRoles_shouldReturnString() {

        int empId = addEmployee();

        String roles =
                employeeService.getFormattedAvailableRoles(empId);

        assertNotNull(roles);
        assertFalse(roles.isBlank());
    }

    @Test
    void removeRole_shouldKeepOtherRoles() {

        int empId = addEmployee();

        employeeService.addRole(empId, cashierRole);
        employeeService.addRole(empId, storekeeperRole);

        employeeService.removeRole(empId, cashierRole);

        assertFalse(employeeService.getRoles(empId).contains(cashierRole));
        assertTrue(employeeService.getRoles(empId).contains(storekeeperRole));
    }
}