package dev.Workers.domain;

import dev.Workers.domain.Objects.Constraint;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDate;
import java.util.*;

public class ShiftManager   {
    private static Set<Shift> shifts;
    private ShiftRequirements shiftRequirements;
    private ConstraintManager constraintManager;
    private static RoleManager roleManager = RoleManager.getInstance();

    private static ShiftManager instance;

    public static ShiftManager getInstance() {
        if (instance == null)
            instance = new ShiftManager();
        return instance;
    }

    private ShiftManager() {
        shifts = new HashSet<>();
        shiftRequirements = new ShiftRequirements();
        constraintManager = ConstraintManager.getInstance();
    }

    /**
     * Assigns an employee to a shift for a specific role,
     * considering constraints and availability.
     * @param shift The shift
     * @param employeeID Employee ID
     * @param role Role to assign
     */
    public void assignEmployeeToShift(Shift shift, Role role, int employeeID) {
        if (isEmployeeApplicable(shift, role, employeeID))
            shift.assignEmployee(role, employeeID);
        addShiftRequirement(shift, role, 1);
    }

    public void removeEmployeeFromShift(Shift shift, Role role, int employeeID) {
        shift.removeEmployee(role, employeeID);
        addShiftRequirement(shift, role, -1);
    }

    public void addShift(LocalDate date, String shiftTypeString) {
        if (date == null)
            System.out.println("Invalid date format.");
        if (shiftTypeString != "morning" && shiftTypeString != "evening")
            System.out.println("Invalid shift type.");
        Shift shift = new Shift(date, shiftTypeString);
        if (!shifts.contains(shift))
            shifts.add(shift);
    }

    public void removeShift(Shift shift) {
        if (!shifts.contains(shift))
            System.out.println("Shift doesn't exist.");
        shifts.remove(shift);
    }

    /**
     * Returns a shift by date and type.
     * @param date The date of the shift
     * @param shiftTypeString "morning" or "evening"
     * @return The Shift object if found, null otherwise
     */
    public Shift getShift(LocalDate date, String shiftTypeString) {
        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getShift().toString().equalsIgnoreCase(shiftTypeString)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Adds shift requirements for a specific role in a shift.
     * @param shift The shift
     * @param role The role
     * @param count Number of employees required
     */
    public void addShiftRequirement(Shift shift, Role role, int count) {
        shiftRequirements.update(shift, role, count);
    }

    /**
     *
     * @return history of shifts
     */
    public String getShiftsHistory() {
        StringBuilder sb = new StringBuilder();
        if (shifts.isEmpty()) {
            sb.append("No shifts recorded.\n");
        } else {
            for (Shift s : shifts) {
                sb.append(s.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public boolean isEmployeeApplicable(Shift shift, Role role, int employeeID) {
        return  (isEmployeeAvailable(employeeID, shift) &&
                isEmployeeQualified(employeeID, role) &&
                isRoleAvailable(shift, role));
    }

    private boolean isEmployeeAvailable(int employeeID, Shift shift) {
        List<Constraint> constraints = constraintManager.getListById(employeeID);
        for (Constraint c : constraints) {
            if (c.getDate().equals(shift.getShiftDate()) && c.getShiftType() == shift.getShift()) {
                return false;
            }
        }
        return true;
    }
    private boolean isEmployeeQualified(int employeeID, Role role) {
        List<Role> roles = roleManager.getListById(employeeID);
        return roles.contains(role);
    }
    private boolean isRoleAvailable(Shift shift, Role role) {
        return shiftRequirements.getCountByRole(shift, role) > 0;
    }

    public void displayAssignmentStatus(Shift shift) {
        String[] existingRoles = roleManager.getExistingRoles();
        // TODO
    }
}
