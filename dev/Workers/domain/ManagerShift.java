package dev.Workers.domain;

import java.time.LocalDate;
import java.util.*;

/**
 * ManagerShift – Mediator class that centralizes shift management:
 * - Maintains all shifts and their history
 * - Maintains shift requirements for each role
 * - Maintains employee constraints
 * - Assigns employees to shifts according to roles and constraints
 *
 * Principle: This class allows interaction between Employee, Shift, ShiftRequirements,
 * and ConstraintManager without direct coupling between them (Mediator Pattern).
 */
public class ManagerShift {

    private static ManagerShift instance;

    private Set<Shift> shifts;                 // All existing shifts
    private ShiftRequirements shiftRequirements;  // Requirements per role for each shift
    private ConstraintManager constraintManager;   // Constraints for each employee

    private ManagerShift() {
        shifts = new HashSet<>();
        shiftRequirements = new ShiftRequirements();
        constraintManager = ConstraintManager.getInstance();
    }

    /**
     * Returns the single instance of ManagerShift (Singleton pattern).
     */
    public static ManagerShift getInstance() {
        if (instance == null) {
            instance = new ManagerShift();
        }
        return instance;
    }

    /**
     * Adds a new shift.
     * @param date The date of the shift
     * @param shiftTypeString "morning" or "evening"
     */
    public void addShift(LocalDate date, String shiftTypeString) {
        if (date == null) {
            System.out.println("Invalid date format.");
            return;
        }
        if (!shiftTypeString.equals("morning") && !shiftTypeString.equals("evening")) {
            System.out.println("Invalid shift type.");
            return;
        }

        Shift shift = new Shift(date, shiftTypeString);
        if (!shifts.contains(shift)) {
            shifts.add(shift);
        }
    }

    /**
     * Removes an existing shift.
     * @param shift The shift to remove
     */
    public void removeShift(Shift shift) {
        if (!shifts.contains(shift)) {
            System.out.println("Shift doesn't exist.");
            return;
        }
        shifts.remove(shift);
        shiftRequirements.remove(shift); // also remove its requirements
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
     * Returns a complete history of all shifts, including assigned employees.
     * @return String describing shifts, roles, and employees
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
            // logic to verify availability according to constraints
        }

        shift.assignEmployee(role, employeeID);
    }

}