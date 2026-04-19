package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.WeekSchedule;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
        /*if (assignments.containsKey(shift))
            return;*/

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

    public List<String> getEmployeeShiftsWeek(int id, WeekSchedule week) {
        List<String> employeeShifts = new ArrayList<>();
        LocalDate start = week.getStartOfWeek();
        LocalDate end = start.plusDays(6);

        // Filter assignments only within this week's range
        for (Map.Entry<Shift, Map<Role, Set<Integer>>> entry : assignments.entrySet()) {
            Shift shift = entry.getKey();
            LocalDate shiftDate = shift.getShiftDate();

            if (!shiftDate.isBefore(start) && !shiftDate.isAfter(end)) {
                for (Role role : entry.getValue().keySet()) {
                    if (isAssignedToRole(shift, role, id)) {
                        employeeShifts.add(shift.toStringByWeekDay() + " (" + role + ")");
                    }
                }
            }
        }
        return employeeShifts;
    }

    public String getEmployeeWeekDisplay(int id, LocalDate referenceDate) {
        // 1. Get the domain object for this week
        WeekSchedule week = getOrCreateWeek(referenceDate);
        LocalDate startOfWeek = week.getStartOfWeek();
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // 2. Business Rule: Gatekeep based on the Published status
        // (Optional: You might want to allow viewing the CURRENT week even if not published,
        // but restricted for NEXT week).
        if (!week.isViewableByUser()) {
            return String.format("The schedule for the week of %s is not yet published.", startOfWeek);
        }

        // 3. Filter, Sort, and Format (Logic remains similar but uses 'week' metadata)
        String shiftList = assignments.entrySet().stream()
                .filter(entry -> {
                    LocalDate shiftDate = entry.getKey().getShiftDate();
                    return !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek);
                })
                .flatMap(shiftEntry -> shiftEntry.getValue().entrySet().stream()
                        .filter(roleEntry -> roleEntry.getValue().contains(id))
                        .map(roleEntry -> Map.entry(shiftEntry.getKey(), roleEntry.getKey()))
                )
                .sorted(Comparator.comparing((Map.Entry<Shift, Role> e) -> e.getKey().getShiftDate())
                        .thenComparing(e -> e.getKey().getType()))
                .map(e -> "- " + e.getKey().getShiftDate() + " (" + e.getKey().getType() + ") | Role: " + e.getValue())
                .collect(Collectors.joining("\n"));

        if (shiftList.isEmpty()) {
            return String.format("No shifts for ID %d between %s and %s", id, startOfWeek, endOfWeek);
        }

        return String.format("Shifts for Employee ID: %d (Week of %s to %s)\n%s",
                id, startOfWeek, endOfWeek, shiftList);
    }

    /**
     * Returns all employees assigned to a specific role in a shift.
     *
     * @param shift the shift
     * @param role  the role
     * @return set of employee IDs, or empty set if none exist
     */
    public Set<Integer> getEmployees(Shift shift, Role role) {
        //System.out.println(assignments.get(shift));
        //System.out.println(assignments.size());
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
        return getEmployees(shift, role).size();
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
        return getEmployees(shift, role).contains(employeeID);
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