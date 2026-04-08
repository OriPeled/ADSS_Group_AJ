package dev.Workers.domain;

import java.time.LocalDate;
import java.util.*;

public class Shift  {
    private LocalDate shiftDate;
    private shiftType shift;
    private Map<Role, Set<Integer>> rolesByEmployeeID;

    public Shift(LocalDate shiftDate, shiftType shift) {
        this.shiftDate = shiftDate;
        this.shift = shift;

//        Map<Role, Integer> requirements = ShiftRequirements.requirementsByDate()
//        for ()
        rolesByEmployeeID = new HashMap<>();
    }

    public Shift(LocalDate shiftDate, String shiftString) {
        this.shiftDate = shiftDate;

        shiftType shift = shiftType.valueOf(shiftString);
        this.shift = shift;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public shiftType getShift() {
        return shift;
    }

    public void setShift(shiftType shift) {
        this.shift = shift;
    }

    public Map<Role, Set<Integer>> getRolesByEmployeeID() {
        return rolesByEmployeeID;
    }

    /**
     *
     * @param role
     * @return list of employees on role on a shift.
     */
    public Set<Integer> getEmployeeIDs(Role role) {
        return rolesByEmployeeID.get(role);
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
            rolesByEmployeeID.put(role, employeeIDs);
        }
        employeeIDs.add(employeeID);
        rolesByEmployeeID.put(role, employeeIDs);
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
        for (Set<Integer> employeeIDs : rolesByEmployeeID.values()) {
            if (employeeIDs.contains(employeeID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "shiftDate=" + shiftDate +
                ", shift=" + shift +
                ", rolesByEmployeeID=" + rolesByEmployeeID +
                '}';
    }
}
