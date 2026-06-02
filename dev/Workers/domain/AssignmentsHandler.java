package dev.Workers.domain;

import dev.Workers.domain.Actions.RequestAction;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.DriverRole;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static dev.Workers.domain.Enums.ShiftType.MORNING;

/**
 * Manages employee assignments for shifts.
 * This class is responsible for storing and managing which employees
 * are assigned to which role inside each shift.
 */
public class AssignmentsHandler {
    private final Map<Shift, Map<Role, Set<Integer>>> assignments;  // shift to (role to assigned empIDs)
    // only for mornings
    private final Map<Shift, Map<Integer, Integer>> extraHours;     // shift to (assigned empID to hours)
    // for the employees
    private final Map<Integer, Queue<RequestAction>> pendingRequests;             // empID to requests
    // for the HR manager
    private final Map<Branch, Queue<String>> requestAnswers;

    private final RoleRegistry roleRegistry;

    private static AssignmentsHandler instance;

    public static AssignmentsHandler getInstance() {
        if (instance == null) {
            instance = new AssignmentsHandler();
        }
        return instance;
    }

    public AssignmentsHandler() {
        this.assignments = new HashMap<>();
        this.extraHours = new HashMap<>();
        this.pendingRequests = new HashMap<>();
        this.requestAnswers = new HashMap<>();
        this.roleRegistry = RoleRegistry.getInstance();
    }

    public void init(Shift shift) {
        assignments.put(shift, new HashMap<>());

        if (shift.getType() == MORNING) {
            extraHours.put(shift, new HashMap<>());
        }
    }

    /**
     * Assigns an employee to a specific role in a shift.
     * <p>
     * If the shift or role does not exist yet, they are created automatically.
     *
     * @param shift the shift to assign into
     * @param role  the role of the employee
     * @param id    the employee ID
     */
    public void add(Shift shift, Role role, int id) {
        Map<Role, Set<Integer>> shiftMap = assignments.get(shift);
        if (shiftMap == null) {
            shiftMap = new HashMap<>();
            assignments.put(shift, shiftMap);
        }
        Set<Integer> set = shiftMap.get(role);
        if (set == null) {
            set = new HashSet<>();
            shiftMap.put(role, set);
        }
        set.add(id);
    }

    /**
     * Removes an employee from a specific role in a shift.
     * <p>
     * If the employee or role does not exist, nothing happens.
     *
     * @param shift      the shift

     * @param employeeID the employee ID to remove
     */
    public void remove(Shift shift, int employeeID) {
        if (isShiftEmpty(shift))
            throw new IllegalArgumentException("Shift is empty.");

        Role role = getEmployeeRole(shift, employeeID);
        if (role == null)
            throw new IllegalArgumentException("Employee not assigned to shift.");

        Set<Integer> employees = getEmployeesByRole(shift, role);
        employees.remove(employeeID);
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        if (shift.getType() != MORNING)
            throw new IllegalArgumentException("Shift must be morning.");
        if (!isAssignedToShift(shift, empID))
            throw new IllegalArgumentException("Employee not assigned to this shift.");

        Map<Integer, Integer> extraHoursMap = extraHours.get(shift);
        extraHoursMap.put(empID, hours);
    }

    public Map<Integer, Integer> getExtraHours(Shift shift) {
        return extraHours.get(shift);
    }

    public Set<Integer> getAllEmployees(Shift shift) {
        Set<Integer> employeeIds = new HashSet<>();
        Map<Role, Set<Integer>> roleMap = assignments.get(shift);

        if (roleMap != null) {
            for (Set<Integer> roleEmployees : roleMap.values()) {
                employeeIds.addAll(roleEmployees);
            }
        }

        return employeeIds;
    }

    /**
     * Returns all employees assigned to a specific role in a shift.
     *
     * @param shift the shift
     * @param role  the role
     * @return set of employee IDs, or empty set if none exist
     */
    public Set<Integer> getEmployeesByRole(Shift shift, Role role) {
        return assignments
                .getOrDefault(shift, Collections.emptyMap())
                .getOrDefault(role, Collections.emptySet());
    }

    // used by TP module
    public Set<Integer> getAllDrivers(Shift shift) {
        return assignments.getOrDefault(shift, Collections.emptyMap())
                .entrySet().stream()
                .filter(entry -> entry.getKey() instanceof DriverRole)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }

    public boolean isShiftEmpty(Shift shift) {
        Map<Role, Set<Integer>> shiftAssignments = assignments.get(shift);

        if (shiftAssignments == null || shiftAssignments.isEmpty()) {
            return true;
        }

        for (Set<Integer> roleAssignments : shiftAssignments.values()) {
            if (!roleAssignments.isEmpty())
                return false;
        }

        return true;
    }

    /**
     * Returns how many employees are assigned to a role in a shift.
     *
     * @param shift the shift
     * @param role  the role
     * @return number of assigned employees
     */
    public int countAssigned(Shift shift, Role role) {
        return getEmployeesByRole(shift, role).size();
    }

    public boolean isRoleEmpty(Shift shift, Role role) {
        return countAssigned(shift, role) == 0;
    }

    public Map<Shift, Map<Role, Set<Integer>>> getAssignments() {
        return assignments;
    }

    /**
     * Checks if an employee is already assigned to a shift role.
     *
     * @param shift      the shift
     * @param role       the role
     * @param employeeID the employee ID
     * @return true if already assigned, false otherwise
     */
    public boolean isAssignedToRole(Shift shift, Role role, int employeeID) {
        return getEmployeesByRole(shift, role).contains(employeeID);
    }

    public boolean isAssignedToShift(Shift shift, int employeeID) {
        return getEmployeeRole(shift, employeeID) != null;
    }

    /**
     * @param shift
     * @param id
     * @return role of the employee in the shift, else null
     */
    public Role getEmployeeRole(Shift shift, int id) {
        for (Role role : roleRegistry.getAllRoles()) {
            if (isAssignedToRole(shift, role, id))
                return role;
        }
        return null;
    }

    public Map<Integer, Queue<RequestAction>> getAllPendingRequests() {
        return pendingRequests;
    }

    public boolean pendingRequestsLeft() {
        return !pendingRequests.isEmpty();
    }

    /**
     * Pretty print: one line per shift.
     */
    @Override
    public String toString() {
        if (assignments.isEmpty()) {
            return "No assignments recorded.";
        }

        StringBuilder sb = new StringBuilder();

        for (Shift shift : assignments.keySet()) {
            sb.append("=== ").append(shift).append(" ===\n");

            // 1. Roles & Assigned Employees
            for (Role role : roleRegistry.getAllRoles()) {
                Set<Integer> employees = getEmployeesByRole(shift, role);
                if (!employees.isEmpty()) {
                    sb.append(String.format("  %-12s: %d assigned | Employees: %s\n",
                            role, employees.size(), employees));
                }
            }

            // 2. Extra Hours
            Map<Integer, Integer> shiftExtra = extraHours.get(shift);
            if (shiftExtra != null && !shiftExtra.isEmpty()) {
                StringJoiner extraJoiner = new StringJoiner(", ");
                shiftExtra.forEach((id, hours) -> {
                    if (hours > 0) {
                        extraJoiner.add("ID " + id + " (+" + hours + "h)");
                    }
                });

                if (extraJoiner.length() > 0) {
                    sb.append("  Extra Hours : ").append(extraJoiner).append("\n");
                }
            }
            sb.append("\n"); // Breathability between shifts
        }

        return sb.toString().trim();
    }

    /**
     * Overload for Assignment requests
     */
    public void addRequest(Shift shift, Role role, int empId) {
        pendingRequests.computeIfAbsent(empId, k -> new LinkedList<>())
                .add(new RequestAction.AssignAction(shift, role, empId));
    }

    /**
     * Overload for Replacement requests
     */
    public void addRequest(Shift shift, int curId, int newId) {
        pendingRequests.computeIfAbsent(newId, k -> new LinkedList<>())
                .add(new RequestAction.ReplaceAction(shift, curId, newId));
    }

    public Queue<RequestAction> getRequests(int empID) {
        return pendingRequests.get(empID);
    }

    public void resetRequests() {
        pendingRequests.clear();
    }

    public boolean hasRequests(int empID) {
        Queue<RequestAction> queue = getRequests(empID);
        return queue != null && !queue.isEmpty();
    }

    public boolean hasRequests() {
        return !pendingRequests.isEmpty();
    }

    public boolean isRequestedToShift(Shift shift, int employeeId) {
        Queue<RequestAction> queue = pendingRequests.get(employeeId);
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        return queue.stream()
                .anyMatch(action -> action.shift().equals(shift));
    }

    public void addRequestAnswer(Branch branch, String message) {
        requestAnswers.get(branch).add("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + message);
    }

    public List<String> popRequestAnswers(Branch branch) {
        List<String> current = new ArrayList<>();

        Queue<String> branchQueue = requestAnswers.get(branch);
        if (branchQueue == null) return current;

        while (!branchQueue.isEmpty()) {
            current.add(branchQueue.poll());
        }

        return current;
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        addRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        addRequest(shift, curId, newId);
    }

    // called by user
    public boolean assignmentNeedsApproval(int empID) {
        return hasRequests(empID);
    }

    // called by user
    public String displayNextPendingAssignment(int employeeId) {
        Queue<RequestAction> queue = getRequests(employeeId);

        if (queue == null || queue.isEmpty()) {
            return "No pending requests for Employee ID: " + employeeId;
        }

        // Look at the head of the FIFO queue without removing it
        RequestAction nextAction = queue.peek();

        StringBuilder sb = new StringBuilder("=== NEXT PENDING REQUEST ===\n");
        sb.append("Employee ID: ").append(employeeId).append("\n");
        sb.append("Details    : ").append(nextAction.getDescription()).append("\n");
        sb.append("----------------------------\n");
        sb.append("Enter 1 to Approve, 0 to Skip/Stay in queue.");

        return sb.toString();
    }
}