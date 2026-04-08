package dev.Workers.domain;

import java.time.LocalDate;
import java.util.*;

import static dev.Workers.domain.shiftType.evening;
import static dev.Workers.domain.shiftType.morning;

public class ShiftManager   {
    private static Set<Shift> shifts;
    private ShiftRequirements shiftRequirements;  // Requirements per role for each shift
    private ConstraintManager constraintManager;
    private RoleManager roleManager;
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
    public void assignEmployeeToShift(Shift shift, int employeeID, Role role) {
        List<Constraint> employeeConstraints = constraintManager.getListById(employeeID);



        // Check if employee meets shift constraints (simplified example)
        if (!employeeConstraints.isEmpty()) {
            if employeeConstraints.
        }

        if (employeeConstraints.contains())

        shift.assignEmployee(role, employeeID);
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


}
