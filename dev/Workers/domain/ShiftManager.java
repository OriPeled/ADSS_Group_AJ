package dev.Workers.domain;

import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Constraint;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

/**
 * Core service class responsible for managing shifts.
 *
 * Handles:
 * - Creating and removing shifts
 * - Assigning employees to shifts
 * - Validating employee eligibility (constraints, roles, availability)
 * - Managing shift requirements
 */
public class ShiftManager   {
    // all system shifts
    private static Set<Shift> shifts;
    // required roles per shift
    private ShiftRequirements shiftRequirements;
    // employee availability constraints
    private ConstraintManager constraintManager;
    // employee roles manager
    private static RoleManager roleManager = RoleManager.getInstance();

    private static ShiftManager instance;

    /**
     * @return singleton instance of ShiftManager
     */
    public static ShiftManager getInstance() {
        if (instance == null)
            instance = new ShiftManager();
        return instance;
    }
    /**
     * Private constructor to enforce Singleton pattern
     */
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

    /**
     * Removes an employee from a shift role and updates requirements.
     *
     * @param shift the shift
     * @param role role to remove from
     * @param employeeID employee ID
     */
    public void removeEmployeeFromShift(Shift shift, Role role, int employeeID) {
        shift.removeEmployee(role, employeeID);
        addShiftRequirement(shift, role, -1);
    }
    /**
     * Creates a new shift and adds it to the system.
     *
     * @param date shift date
     * @param shiftTypeString shift type ("morning"/"evening")
     */
    public void addShift(LocalDate date, String shiftTypeString) {
        // if date is null show Invalid date format.
        if (date == null)
            System.out.println("Invalid date format.");
        // cheking if shift type is morning ir evening
        if (shiftTypeString != "morning" && shiftTypeString != "evening")
            System.out.println("Invalid shift type.");
        Shift shift = new Shift(date, shiftTypeString);
        // cheking if shift not exist on map to add to the system
        if (!shifts.contains(shift))
            shifts.add(shift);
    }
    /**
     * Removes a shift from the system.
     *
     * @param shift shift to remove
     */
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
    /**
     * Checks whether an employee can be assigned to a shift role.
     *
     * @param shift shift
     * @param role role
     * @param employeeID employee ID
     * @return true if employee is eligible
     */
    public boolean isEmployeeApplicable(Shift shift, Role role, int employeeID) {
        return  (isEmployeeAvailable(employeeID, shift) &&
                isEmployeeQualified(employeeID, role) &&
                isRoleAvailable(shift, role));
    }
    /**
     * Checks if employee is available according to constraints.
     */
    private boolean isEmployeeAvailable(int id, Shift shift) {
        LocalDate date = shift.getShiftDate();
        DayOfWeek day = date.getDayOfWeek();
        shiftType shiftType = shift.getShift();
        return constraintManager.isEmployeeAvailable(id, day, shiftType);
    }
    /**
     * Checks if employee has the required role.
     */
    private boolean isEmployeeQualified(int employeeID, Role role) {
        List<Role> roles = roleManager.getListById(employeeID);
        return roles.contains(role);
    }
    /**
     * Checks if role still has available demand in shift.
     */
    private boolean isRoleAvailable(Shift shift, Role role) {
        return shiftRequirements.getCountByRole(shift, role) > 0;
    }
    /**
     * Displays assignment status per role in a shift.
     */
    public void displayAssignmentStatus(Shift shift) {
        String[] existingRoles = roleManager.getExistingRoles();
        // TODO
    }

}
