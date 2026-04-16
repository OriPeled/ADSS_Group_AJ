package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Objects.Shift;

import java.util.*;

/**
 * Manages employee assignments for shifts.
 * <p>
 * This class is responsible for storing and managing which employees
 * are assigned to which role inside each shift.
 */
public class Assignments {
    private final Map<Shift, Map<Role, Set<Integer>>> assignments;

    /**
     * Constructor initializes empty assignment storage.
     */
    public Assignments() {
        this.assignments = new HashMap<>();
    }

    /**
     * public void init(Shift shift) {
     * Map<Role, Set<Integer>> innerMap = new HashMap<>();
     * Set<Integer> innerSet = new HashSet<>();
     * for (Role role : Role.values())
     * innerMap.put(role, innerSet);
     * assignments.put(shift, innerMap);
     * }
     */

    public void init(Shift shift) {
        if (assignments.containsKey(shift))
            return;

        Map<Role, Set<Integer>> innerMap = new HashMap<>();
        for (Role role : Role.values()) {
            innerMap.put(role, new HashSet<>()); // ← new set for each role
        }
        assignments.put(shift, innerMap);
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
     * @param role       the role
     * @param employeeID the employee ID to remove
     */
    public void remove(Shift shift, Role role, int employeeID) {
        Map<Role, Set<Integer>> shiftAssignments = assignments.get(shift);
        if (shiftAssignments == null) return;

        Set<Integer> employees = shiftAssignments.get(role);
        if (employees == null) return;

        employees.remove(employeeID);

        // Clean up empty structures to keep memory clean
        if (employees.isEmpty()) {
            shiftAssignments.remove(role);
        }

        if (shiftAssignments.isEmpty()) {
            assignments.remove(shift);
        }
    }

    public List<String> getShiftWeek(int id) {
        List<String> employeeShifts = new ArrayList<>();
        for (Shift shift : assignments.keySet()) {
            for (Role role : assignments.get(shift).keySet()) {
                if (isAssigned(shift, role, id)) {
                    employeeShifts.add(shift.toStringByWeekDay());
                }
            }
        }
        return employeeShifts;
    }

    /**
     * Returns all employees assigned to a specific role in a shift.
     *
     * @param shift the shift
     * @param role  the role
     * @return set of employee IDs, or empty set if none exist
     */
    public Set<Integer> getEmployees(Shift shift, Role role) {
        return assignments
                .getOrDefault(shift, Collections.emptyMap())
                .getOrDefault(role, Collections.emptySet());
    }

    /**
     * Returns how many employees are assigned to a role in a shift.
     *
     * @param shift the shift
     * @param role  the role
     * @return number of assigned employees
     */
    public int countAssigned(Shift shift, Role role) {
        return getEmployees(shift, role).size();
    }

    public Map<Shift, Map<Role, Set<Integer>>> getAssignments() {
        return assignments;
    }

    /**public int countUnassignedValid(Shift shift, Role role) {
     // TODO
     return 0;
     }
     */

    /**
     * Checks if an employee is already assigned to a shift role.
     *
     * @param shift      the shift
     * @param role       the role
     * @param employeeID the employee ID
     * @return true if already assigned, false otherwise
     */
    public boolean isAssigned(Shift shift, Role role, int employeeID) {
        return getEmployees(shift, role).contains(employeeID);
    }

    /**
     * @param shift
     * @param id
     * @return role of the employee in the shift, else null
     */
    public Role getEmployeeRole(Shift shift, int id) {
        for (Role role : Role.values()) {
            if (isAssigned(shift, role, id))
                return role;
        }
        return null;
    }

    /**
     * Returns all assignments in the system.
     *
     * @return full assignment map
     */
    public Map<Shift, Map<Role, Set<Integer>>> getAllAssignments() {
        return assignments;
    }

    /**
     * Pretty print: one line per shift.
     */
    @Override
    public String toString() {
        if (assignments.isEmpty()) {
            return "No assignments.";
        }
        String result = "Shift Assignments:\n";
        for (Map.Entry<Shift, Map<Role, Set<Integer>>> shiftEntry : assignments.entrySet()) {
            Shift shift = shiftEntry.getKey();
            Map<Role, Set<Integer>> roles = shiftEntry.getValue();
            result += "Shift: " + shift + " | ";
            boolean first = true;
            for (Map.Entry<Role, Set<Integer>> roleEntry : roles.entrySet()) {
                if (!first) {
                    result += ", ";
                }
                result += roleEntry.getKey() + ": " + roleEntry.getValue().size();
                first = false;
            }
            result += "\n";
        }
        return result;
    }
}