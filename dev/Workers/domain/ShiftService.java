package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * ShiftService is the core business logic of the system.
 *
 * It manages:
 * - Shift creation and removal
 * - Employee assignment to shifts
 * - Validation of constraints, roles, and requirements
 * - Reporting shift history and status
 */
public class ShiftService {

    // all system shifts
    private static Set<Shift> shifts;
    // required roles per shift
    private Requirements requirements;
    // employee availability constraints
    private ConstraintManager constraintManager;
    //all the assigments
    private ShiftAssignments assignments;
    // employee roles manager
    private static RoleManager roleManager = RoleManager.getInstance();

    private static ShiftService instance;

    /**
     * singeltone
     * @return
     */
    public static ShiftService getInstance() {
        if (instance == null) {
            instance = new ShiftService();
        }
        return instance;
    }

    /**
     * constractor for service
     */
    private ShiftService() {
        shifts = new HashSet<>();
        requirements = new Requirements();
        constraintManager = ConstraintManager.getInstance();
        assignments = new ShiftAssignments();
    }

    /**
     *
     * @param date
     * @param type
     * add shift to the system
     */
    public void addShift(LocalDate date, shiftType type) {
        shifts.add(new Shift(date, type));
    }

    /**
     *
     * @param date
     * @param type
     * @return getter for shift, null if not exsist
     */
    public Shift getShift(LocalDate date, shiftType type) {
        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getShift().equals(type)) {
                return s;
            }
        }
        return null;
    }
    private boolean isValid(Shift shift, Role role, int employeeId) {

        return isAvailable(employeeId, shift)
                && isQualified(employeeId, role)
                && isNeeded(shift, role);
    }
    /**
     * Checks if employee is available according to constraints.
     */
    private boolean isAvailable(int id, Shift shift) {
        return constraintManager.isEmployeeAvailable(
                id,
                shift.getShiftDate().getDayOfWeek(),
                shift.getShift()
        );
    }
    /**
     * Checks if employee has the required role.
     */
    private boolean isQualified(int id, Role role) {
        return roleManager.getListById(id).contains(role);
    }
    /**
     * Checks if role still has available demand in shift.
     */
    private boolean isNeeded(Shift shift, Role role) {
        return assignments.countAssigned(shift, role)
                < requirements.get(shift, role);
    }

    /**
     *
     * @param shift
     * @param role
     * @param count
     * update equirement to roll in shift
     */
    public void setRequirement(Shift shift, Role role, int count) {
        requirements.add(shift, role, count);
    }

    /**
     *
     * @param shift
     * @param role
     * @return how much left to assiging
     */
    public int leftToAssign(Shift shift, Role role) {
        return requirements.get(shift, role)
                - assignments.countAssigned(shift, role);
    }

    /**
     *
     * @param shift
     * @param role
     * @param employeeId
     *  Assign employee to shift if valid.
     *  Prints error message if assignment fails.
     *
     */
    public void assignEmployee(Shift shift, Role role, int employeeId){
        if (!isValid(shift, role, employeeId)) {
            System.out.println("cant work");
            return;
        }
        assignments.add(shift, role, employeeId);
    }
    public void removeEmployee(Shift shift, Role role, int employeeId) {
        assignments.remove(shift, role, employeeId);
    }

    /**
     *
     * @return  full shifts assignment history by date, shift type , amout
     */
    public String ShiftHistory() {
        if (shifts.isEmpty()) {
            return "No shifts available.";
        }
        String result = "SHIFT HISTORY:\n";
        for (Shift shift : shifts) {
            result += "\nShift: " + shift + "\n";
            for (Role role : Role.values()) {
                int required = requirements.get(shift, role);
                Set<Integer> employees = assignments.getEmployees(shift, role);
                int assigned = employees.size();
                if (required > 0 || assigned > 0) {
                    result += "- " + role +
                            " | assigned: " + assigned +
                            " | employees: " + employees + "\n";
                }
            }
        }

        return result;
    }

    /**
     *
     * @param shift
     * @return String of shift detils
     */
    public String getShiftDetails(Shift shift) {
        String result = "Shift: " + shift + "\n";
        for (Role role : Role.values()) {
            int required = requirements.get(shift, role);
            Set<Integer> employees = assignments.getEmployees(shift, role);
            int assigned = employees.size();
            if (required > 0 || assigned > 0) {
                result += role +
                        ": " + employees +
                        " (" + assigned + " assigned, " +
                        (required - assigned) + " left)\n";
            }
        }
        return result;
    }



}
