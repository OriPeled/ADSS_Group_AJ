package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;

import java.time.LocalDate;
import java.util.*;
    /**
     * Represents a work shift on a specific date.
     *
     * Each shift has:
     * - A date
     * - A shift type (morning/evening)
     * - A mapping between roles and assigned employees (by their IDs)
     *
     * The system allows assigning and removing employees to/from roles,
     * and checking if an employee is already assigned in the shift.
     */
public class Shift {
    // Date of the shift
    private LocalDate shiftDate;
    // type of the shift
    private shiftType shift;

    /**
     * Mapping between roles and employee IDs assigned to each role.
     * Each role can have multiple employees.
    */
    private Map<Role, Set<Integer>> employeeIdsByRole;

    /**
     *
     * Constructor using enum shift type.
     * @param shiftDate date of the shift
     * @param shift shift type
     */
    public Shift(LocalDate shiftDate, shiftType shift) {
        this.shiftDate = shiftDate;
        this.shift = shift;

//        Map<Role, Integer> requirements = ShiftRequirements.requirementsByDate()
//        for ()
        employeeIdsByRole = new HashMap<>();
    }

    public Shift(LocalDate shiftDate, String shiftString) {
        this.shiftDate = shiftDate;

        shiftType shift = shiftType.valueOf(shiftString);
        this.shift = shift;
    }
    /**
     * @return shift date
     */
    public LocalDate getShiftDate() {
        return shiftDate;
    }
    /**
     * Updates shift date
     * @param shiftDate new date
     */
    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    /**
     * @return shift type
     */
    public shiftType getShift() {
        return shift;
    }

    /**
     * Updates shift type
     * @param shift new shift type
     */
    public void setShift(shiftType shift) {
        this.shift = shift;
    }

    public Map<Role, Set<Integer>> getEmployeeIdsByRole() {
        return employeeIdsByRole;
    }

    /**
     *
     * @param role
     * @return list of employees on role on a shift.
     */
    public Set<Integer> getEmployeeIDs(Role role) {
        return employeeIdsByRole.get(role);
    }

    /**
     *
     * @param role
     * @param employeeID
     * add employee by id to shift
     */
    public void assignEmployee(Role role, int employeeID) {
        Set<Integer> employeeIDs = getEmployeeIDs(role);
        if (employeeIDs == null) {
            employeeIDs = new HashSet<>();
            employeeIdsByRole.put(role, employeeIDs);
        }
        employeeIDs.add(employeeID);
        employeeIdsByRole.put(role, employeeIDs);
    }

    /**
     *
     * @param role
     * @param employeeID
     * remove employee from role in shift
     */
    public void removeEmployee(Role role, int employeeID) {
        Set<Integer> employeeIDs = getEmployeeIDs(role);
        if (employeeIDs == null) {
            return;
        }
        employeeIDs.remove(employeeID);

    }

    /**
     *
     * @param employeeID
     * @return true if employee already work at any role , else false
     */
    public boolean isEmployeeAssignedToAnyRole(int employeeID) {
        for (Set<Integer> employeeIDs : employeeIdsByRole.values()) {
            if (employeeIDs.contains(employeeID)) {
                return true;
            }
        }
        return false;
    }

    public int length(Role role) {
        return employeeIdsByRole.get(role).size();
    }

    /**
     * @return string representation of the shift and its assignments
     */
    @Override
    public String toString() {
        return "Shift" +
                "\nDate: " + shiftDate +
                "\nType: " + shift +
                "\nAssignments: " + employeeIdsByRole;
    }
}
