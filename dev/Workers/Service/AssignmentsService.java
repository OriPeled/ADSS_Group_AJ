package dev.Workers.Service;

import dev.Workers.domain.AssignmentsHandler;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.util.*;

public class AssignmentsService {
    private static AssignmentsHandler assignmentsHandler;

    private static AssignmentsService instance;

    public static AssignmentsService getInstance() {
        if (instance == null) {
            instance = new AssignmentsService();
        }
        return instance;
    }

    private AssignmentsService() {
        assignmentsHandler = AssignmentsHandler.getInstance();
    }

    // used by TP module
    public Set<Integer> getAllDrivers(Shift shift) {
        return assignmentsHandler.getAllDrivers(shift);
    }

    public boolean pendingRequestsLeft() {
        return assignmentsHandler.pendingRequestsLeft();
    }

    public static boolean hasRequests() {
        return assignmentsHandler.hasRequests();
    }

    public List<String> popRequestAnswers(Branch branch) {
        return assignmentsHandler.popRequestAnswers(branch);
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        assignmentsHandler.sendRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        assignmentsHandler.sendRequest(shift, curId, newId);
    }

    // called by user
    public boolean assignmentNeedsApproval(int empID) {
        return assignmentsHandler.assignmentNeedsApproval(empID);
    }

    // called by user
    public String displayNextPendingAssignment(int employeeId) {
        return assignmentsHandler.displayNextPendingAssignment(employeeId);
    }
}
