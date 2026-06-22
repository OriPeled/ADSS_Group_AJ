package dev.Workers.Tests;

import dev.Workers.domain.AssignmentHandler;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.RoleRegistry;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.service.AssignmentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AssignmentService.
 */
class AssignmentServiceTest {
    private AssignmentService assignmentService;
    private AssignmentHandler assignmentHandler;
    private Branch branch;
    private Role cashierRole;
    private int nextId = 10000;

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
        assignmentService = AssignmentService.getInstance();
        assignmentHandler = AssignmentHandler.getInstance();
        BranchRegistry registry = BranchRegistry.getInstance();

        registry.registerBranch(new Branch("Beer-Sheva"));
        branch = registry.getBranchByName("Beer-Sheva");
        cashierRole = RoleRegistry.getInstance().getRoleByName("Cashier");
    }

    /**
     * Creates a shift and initializes assignment structures.
     */
    private Shift createShift() {
        Shift shift = new Shift(
                branch,
                LocalDate.now().plusDays(100),
                ShiftType.MORNING);
        assignmentHandler.init(shift);
        return shift;
    }

    /**
     * Verifies that sending an assignment request creates a pending request.
     */
    @Test
    void sendRequest_shouldCreatePendingRequest() {
        int employeeId = nextId++;
        Shift shift = createShift();

        assignmentService.sendRequest(
                shift,
                cashierRole,
                employeeId);

        assertTrue(assignmentService.assignmentNeedsApproval(employeeId));
    }

    /**
     * Verifies that pendingRequestsLeft becomes true after sending a request.
     */
    @Test
    void pendingRequestsLeft_shouldReturnTrue() {
        int employeeId = nextId++;
        Shift shift = createShift();

        assignmentService.sendRequest(
                shift,
                cashierRole,
                employeeId);

        assertTrue(assignmentService.pendingRequestsLeft());
    }

    /**
     * Verifies that employee without requests does not need approval.
     */
    @Test
    void assignmentNeedsApproval_withoutRequest_shouldReturnFalse() {
        assertFalse(assignmentService.assignmentNeedsApproval(99999));
    }

    /**
     * Verifies that displayNextPendingAssignment returns a message.
     */
    @Test
    void displayNextPendingAssignment_shouldReturnRequestDescription() {
        int employeeId = nextId++;
        Shift shift = createShift();

        assignmentService.sendRequest(
                shift,
                cashierRole,
                employeeId);
        String result = assignmentService.displayNextPendingAssignment(employeeId);

        assertNotNull(result);
        assertTrue(result.contains("PENDING"));
    }

    /**
     * Verifies that employee without requests receives a proper message.
     */
    @Test
    void displayNextPendingAssignment_withoutRequest_shouldReturnNoPendingMessage() {
        String result = assignmentService.displayNextPendingAssignment(88888);
        assertTrue(result.contains("No pending requests"));
    }

    /**
     * Verifies that replacement requests create pending approval.
     */
    @Test
    void replacementRequest_shouldCreatePendingRequest() {
        int currentId = nextId++;
        int newId = nextId++;
        Shift shift = createShift();

        assignmentService.sendRequest(
                shift,
                currentId,
                newId);

        assertTrue(assignmentService.assignmentNeedsApproval(newId));
    }

    /**
     * Verifies that multiple requests remain pending.
     */
    @Test
    void multipleRequests_shouldRemainPending() {
        int employeeId = nextId++;
        Shift shift1 = createShift();
        Shift shift2 = createShift();

        assignmentService.sendRequest(
                shift1,
                cashierRole,
                employeeId);
        assignmentService.sendRequest(
                shift2,
                cashierRole,
                employeeId);

        assertTrue(assignmentService.assignmentNeedsApproval(employeeId));
    }

    /**
     * Verifies that requests are processed in FIFO order.
     */
    @Test
    void requestQueue_shouldBeFIFO() {
        int employeeId = nextId++;
        Shift shift1 = createShift();
        Shift shift2 = createShift();

        assignmentService.sendRequest(
                shift1,
                cashierRole,
                employeeId);
        assignmentService.sendRequest(
                shift2,
                cashierRole,
                employeeId);

        String description = assignmentService.displayNextPendingAssignment(employeeId);

        assertTrue(description.contains(shift1.getDate().toString()));
    }

    /**
     * Verifies that no request answers exist for a new branch.
     */
    @Test
    void popRequestAnswers_emptyBranch_shouldReturnEmptyList() {
        assertTrue(assignmentService.popRequestAnswers(branch).isEmpty());
    }

    /**
     * Verifies that adding multiple requests keeps the system in pending state.
     */
    @Test
    void hasRequests_shouldReturnTrueAfterSecondRequest() {
        int employeeId = nextId++;
        Shift shift1 = createShift();
        Shift shift2 = createShift();

        assignmentService.sendRequest(
                shift1,
                cashierRole,
                employeeId);
        assignmentService.sendRequest(
                shift2,
                cashierRole,
                employeeId);

        assertTrue(assignmentService.pendingRequestsLeft());
    }
}