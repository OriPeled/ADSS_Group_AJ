package dev.Workers.domain;

import dev.Workers.domain.Actions.RequestAction;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static dev.Workers.domain.Enums.ShiftType.morning;

/**
 * Manages employee assignments for shifts.
 * This class is responsible for storing and managing which employees
 * are assigned to which role inside each shift.
 */
public class Assignments {
    private final Map<Shift, Map<Role, Set<Integer>>> assignments;  // shift to (role to assigned empIDs)
    // only for mornings
    private final Map<Shift, Map<Integer, Integer>> extraHours;     // shift to (assigned empID to hours)
    // for the employees
    private final Map<Integer, Queue<RequestAction>> pendingRequests;             // empID to requests
    // for the HR manager
    private final Queue<String> requestAnswers;

    /**
     * Constructor initializes empty assignment storage.
     */
    public Assignments() {
        this.assignments = new HashMap<>();
        this.extraHours = new HashMap<>();
        this.pendingRequests = new HashMap<>();
        this.requestAnswers = new LinkedList<>();
    }

    public void init(Shift shift) {
        Map<Role, Set<Integer>> roleMap = new HashMap<>();
        for (Role role : Role.values()) {
            roleMap.put(role, new HashSet<>());
        }
        assignments.put(shift, roleMap);

        if (shift.getType() == morning) {
            Map<Integer, Integer> extraHoursMap = new HashMap<>();
            extraHours.put(shift, extraHoursMap);
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
        if (getEmployeeRole(shift, employeeID) == null)
            throw new IllegalArgumentException("Employee not assigned to shift.");

        Set<Integer> employees = getEmployeesByRole(shift, role);
        employees.remove(employeeID);
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        if (shift.getType() != morning)
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

    public boolean isShiftEmpty(Shift shift) {
        Map<Role, Set<Integer>> shiftAssignments = assignments.get(shift);
        if (shiftAssignments.isEmpty()) return false;

        for (Set<Integer> roleAssignments: shiftAssignments.values()) {
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
        for (Role role : Role.values()) {
            if (isAssignedToRole(shift, role, id))
                return role;
        }
        return null;
    }

    public Map<Integer, Queue<RequestAction>> getAllPendingRequests() {
        return pendingRequests;
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
            for (Role role : Role.values()) {
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

    public void addRequestAnswer(String message) {
        requestAnswers.add("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + message);
    }

    public List<String> popRequestAnswers() {
        List<String> current = new ArrayList<>();
        while (!requestAnswers.isEmpty()) {
            current.add(requestAnswers.poll());
        }
        return current;
    }
}