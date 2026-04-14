package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDate;
import java.util.*;

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
    private Assignments assignments;
    // employees manager
    private static EmployeeManager employeeManager = EmployeeManager.getInstance();
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
        assignments = new Assignments();
    }

    /**
     *
     * @param date
     * @param type
     * adding shift to the system if it doesn't already exist, else nothing
     */
    public void addShift(LocalDate date, shiftType type) {
        Shift shift = new Shift(date, type);

        shifts.add(shift);
        requirements.init(shift);
        assignments.init(shift);
    }

    /**
     *
     * @param date
     * @param type
     * @return getter for shift, null if not exist
     */
    public Shift getShift(LocalDate date, shiftType type) {
        addShift(date, type);
        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getType().equals(type)) {
                return s;
            }
        }
        return null;
    }

    /**
     *
     * @param shift
     * @param role
     * @param count
     * update equirement to roll in shift
     */
    public void setRequirement(Shift shift, Role role, int count) {
        requirements.set(shift, role, count);
    }

    /**
     *
     * @param shift
     * @param role
     * @return how much left to assiging
     */
    public int leftToAssign(Shift shift, Role role) {
        return requirements.countRequired(shift, role)
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
    public void assignEmployee(Shift shift, Role role, int employeeId) {
        if (nobodyToAssign(shift, role) && isSpecialValid(shift, role, employeeId)) {
            System.out.println("Special approve granted.");
            assignments.add(shift, role, employeeId);
            return;
        }

        if (!isValid(shift, role, employeeId)) {
          throw new RuntimeException("Cannot assign employee " + employeeId + " to shift " + shift +
                                     " for role " + role + ". Check constraints, qualifications, and requirements.");

        }

        assignments.add(shift, role, employeeId);
    }

    private boolean isValid(Shift shift, Role role, int employeeId) {

        return isAvailable(employeeId, shift)
                && isQualified(employeeId, role)
                && isNeeded(shift, role);
    }

    private boolean isSpecialValid(Shift shift, Role role, int employeeId) {

        return isQualified(employeeId, role)
                && isNeeded(shift, role);
    }

    /**
     * Checks if employee is available according to constraints.
     */
    private boolean isAvailable(int id, Shift shift) {
        return constraintManager.isEmployeeAvailable(
                id,
                shift.getShiftDate().getDayOfWeek(),
                shift.getType()
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
                < requirements.countRequired(shift, role);
    }

    public boolean nobodyToAssign(Shift shift, Role role) {
        return isNeeded(shift, role) && this.countUnassignedValid(shift, role) == 0;
    }

    public String getUnassignedValid(Shift shift) {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        StringBuilder result = new StringBuilder("Available employees for shift:\n");
        boolean foundAny = false;

        for (Role role : Role.values()) {
            if (isNeeded(shift, role)) {
                result.append("--- ").append(role).append(" ---\n");
                List<Integer> qualifiedIds = roleManager.getListByRole(role);

                for (int id : qualifiedIds) {
                    if (isAvailable(id, shift) && !assignments.isAssigned(shift, role, id)) {
                        Employee emp = employeeManager.getById(id);
                        result.append("- ").append(emp.getName()).append(" (ID: ").append(id).append(")\n");
                        foundAny = true;
                    }
                }
            }
        }

        if (!foundAny) {
            return "No available valid employees for this shift.";
        }
        return result.toString();
    }

    public int countUnassignedValid(Shift shift, Role role) {
        int count = 0;
        List<Integer> qualifiedIds = roleManager.getListByRole(role);
        for (int id : qualifiedIds) {
            if (isAvailable(id, shift) && !assignments.isAssigned(shift, role, id)) {
                count++;
            }
        }
        return count;
    }

    public void setRequirements(Shift shift, Role role, int count) {
        requirements.set(shift, role, count);

        while (assignments.countAssigned(shift, role) > requirements.countRequired(shift, role)) {
            for (Integer id : assignments.getEmployees(shift, role)) {
                System.out.println("Overstaff.");
                removeEmployee(shift, role, id);
                System.out.println("Employee"
                                    + employeeManager.getById(id).getName()
                                    + '(' + id + ") removed.");
            }
        }
    }

    public void removeEmployee(Shift shift, Role role, int employeeId) {
        assignments.remove(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentEmployeeId, int newEmployeeId) {
        Role role = assignments.getEmployeeRole(shift, currentEmployeeId);
        if (isValid(shift, role, newEmployeeId)) {
            assignEmployee(shift, role, newEmployeeId);
            removeEmployee(shift, role, currentEmployeeId);
        }
    }

    private List<Shift> getNextWeekShifts() {
        // TODO
        return null;
    }

    public Map<Shift, String> weekAssignment() {
        Map<Shift, String> weekStatuses = new HashMap<>();
        List<Shift> weekShifts = getNextWeekShifts();
        for (Shift shift : weekShifts) {
            if (shift != null) {
                String status = "complete";
                for (Role role : Role.values()) {
                    if (isNeeded(shift, role)) {
                        status = "incomplete";
                        break;
                    }
                }
                weekStatuses.put(shift, status);
            }
            else weekStatuses.put(shift, "incomplete");
        }
        return weekStatuses;
    }

    public String displayWeekAssignments() {
        StringBuilder sb = new StringBuilder();
        Map<Shift, String> assignments = weekAssignment();

        for (Map.Entry<Shift, String> entry : assignments.entrySet()) {
            sb.append(entry.getKey().toStringByWeekDay())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        return sb.toString().trim();
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
                int required = requirements.countRequired(shift, role);
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
            int required = requirements.countRequired(shift, role);
            Set<Integer> employees = assignments.getEmployees(shift, role);
            int assigned = employees.size();
            if (required > 0) {
                result += role +
                        ": " + employees +
                        " (" + assigned + " assigned, " +
                        (required - assigned) + " left)\n";
            }
        }
        return result;
    }
}
