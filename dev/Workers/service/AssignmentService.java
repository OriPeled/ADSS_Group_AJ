package dev.Workers.service;

import dev.Workers.domain.AssignmentHandler;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.util.*;

public class AssignmentService {
    private AssignmentHandler assignmentHandler;

    private static AssignmentService instance;

    public static AssignmentService getInstance() {
        if (instance == null) {
            instance = new AssignmentService();
        }
        return instance;
    }

    private AssignmentService() {
        assignmentHandler = AssignmentHandler.getInstance();
    }

    public boolean pendingRequestsLeft() {
        return assignmentHandler.pendingRequestsLeft();
    }

    public List<String> popRequestAnswers(Branch branch) {
        return assignmentHandler.popRequestAnswers(branch);
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        assignmentHandler.sendRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        assignmentHandler.sendRequest(shift, curId, newId);
    }

    // called by user
    public boolean assignmentNeedsApproval(int empID) {
        return assignmentHandler.assignmentNeedsApproval(empID);
    }

    // called by user
    public String displayNextPendingAssignment(int employeeId) {
        return assignmentHandler.displayNextPendingAssignment(employeeId);
    }
}
