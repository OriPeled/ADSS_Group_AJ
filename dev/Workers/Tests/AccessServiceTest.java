package dev.Workers.Tests;

import dev.Workers.domain.AccessHandler;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.service.AccessService;
import dev.Workers.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AccessService.
 */
public class AccessServiceTest {

    private AccessService accessService;
    private EmployeeService employeeService;
    private AccessHandler accessHandler;

    private Branch branch;

    private static int nextId = 200000;

    @BeforeEach
    void setUp() {

        accessService = AccessService.getInstance();
        employeeService = EmployeeService.getInstance();
        accessHandler = AccessHandler.getInstance();

        branch = BranchRegistry.getInstance()
                .getBranchByName("Beer-Sheva");
    }

    /**
     * Creates a valid employee.
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
                LocalDate.of(2025,1,1));

        return id;
    }

    /**
     * Verifies that registering succeeds.
     */
    @Test
    void register_shouldSucceed() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        assertTrue(
                accessHandler.isRegisteredUser(id));
    }

    /**
     * Verifies that short passwords are rejected.
     */
    @Test
    void register_shortPassword_shouldFail() {

        int id = createEmployee();

        assertThrows(
                IllegalArgumentException.class,
                () -> accessService.Register(id, "12"));
    }

    /**
     * Verifies that login succeeds with the correct password.
     */
    @Test
    void login_correctPassword_shouldSucceed() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        assertEquals(
                UserResponse.success,
                accessService.login(id, "1234"));
    }

    /**
     * Verifies that wrong passwords are rejected.
     */
    @Test
    void login_wrongPassword_shouldFail() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        assertThrows(
                IllegalArgumentException.class,
                () -> accessService.login(id, "9999"));
    }

    /**
     * Verifies that non-registered users return notRegistered.
     */
    @Test
    void login_notRegistered_shouldReturnNotRegistered() {

        int id = createEmployee();

        assertEquals(
                UserResponse.notRegistered,
                accessService.login(id, "1234"));
    }

    /**
     * Verifies that updating password succeeds.
     */
    @Test
    void updatePassword_shouldSucceed() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        accessService.updatePassword(id, "5678");

        assertFalse(
                accessHandler.wrongPassword(id, "5678"));
    }

    /**
     * Verifies that invalid new passwords are rejected.
     */
    @Test
    void updatePassword_shortPassword_shouldFail() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        assertThrows(
                IllegalArgumentException.class,
                () -> accessService.updatePassword(id, "12"));
    }

    /**
     * Verifies that removing a user succeeds.
     */
    @Test
    void removeUser_shouldSucceed() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        accessService.removeUser(id);

        assertFalse(
                accessHandler.isRegisteredUser(id));
    }

    /**
     * Verifies that login after removal returns notRegistered.
     */
    @Test
    void login_afterRemoval_shouldReturnNotRegistered() {

        int id = createEmployee();

        accessService.Register(id, "1234");

        accessService.removeUser(id);

        assertEquals(
                UserResponse.notRegistered,
                accessService.login(id, "1234"));
    }

    /**
     * Verifies that removing an unregistered user does not throw.
     */
    @Test
    void removeUnregisteredUser_shouldNotThrow() {

        int id = createEmployee();

        assertDoesNotThrow(
                () -> accessService.removeUser(id));
    }
}