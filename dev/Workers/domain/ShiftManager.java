package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.WeekSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static dev.Workers.domain.Enums.WeekStatus.*;

/**
 * ShiftService is the core business logic of the system.
 *
 * It manages:
 * - Shift creation and removal
 * - Employee assignment to shifts
 * - Validation of constraints, roles, and requirements
 * - Reporting shift history and status
 */
public class ShiftManager {
    private final Map<LocalDate, WeekSchedule> weekSchedules = new HashMap<>();

    private final Set<Shift> shifts;
    private final Requirements requirements;
    private final ConstraintManager constraintManager;
    private final Assignments assignments;
    private final EmployeeManager employeeManager;
    private final RoleManager roleManager;

    private static ShiftManager instance;   // this one stays static — singleton pattern requires it

    /**
     * singeltone
     * @return
     */
    public static ShiftManager getInstance() {
        if (instance == null) {
            instance = new ShiftManager();
        }
        return instance;
    }

    /**
     * constractor for service
     */
    private ShiftManager() {
        this.shifts = new HashSet<>();
        this.requirements = new Requirements();
        this.constraintManager = ConstraintManager.getInstance();
        this.assignments = new Assignments();
        this.employeeManager = EmployeeManager.getInstance();
        this.roleManager = RoleManager.getInstance();
    }

    /**
     *
     * @param date
     * @param type
     * adding shift to the system if it doesn't already exist, else nothing
     */
    public void addShift(LocalDate date, ShiftType type) {
        Shift shift = new Shift(date, type);
        if (!shifts.contains(shift)) {
            shifts.add(shift);
            requirements.init(shift);
            assignments.init(shift);
        }
    }

    /**
     *
     * @param date
     * @param type
     * @return getter for shift, null if not exist
     */
    public Shift getShift(LocalDate date, ShiftType type) {
        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getType().equals(type)) {
                return s;
            }
        }

        addShift(date, type);

        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getType().equals(type)) {
                return s;
            }
        }

        return null;
    }

    public void removeShift(Shift shift) {
        shifts.remove(shift);
        requirements.init(shift);
        assignments.init(shift);
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
    /*

     */
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

    /**
     * Returns all shifts for the next week (7 days from today).
     */
    private List<Shift> getNextWeekShifts() {
        if (!getNextWeek().isViewableByUser()) {
            throw new IllegalStateException("The schedule for the next week is not yet published.");
        }

        List<Shift> result = new ArrayList<>();

        // 1. Find the next Sunday relative to today
        // Note: 'next(SUNDAY)' will always move to the future,
        // even if today is already Sunday.
        LocalDate startDay = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // 2. Iterate for 7 days starting from that Sunday
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDay.plusDays(i);

            for (ShiftType type : ShiftType.values()) {
                Shift shift = getShift(date, type);
                if (shift != null) {
                    result.add(shift);
                }
            }
        }

        return result;
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

    private boolean isWeekAssigned(LocalDate dateInWeek) {
        List<Shift> weekShifts = getShiftsForWeek(dateInWeek);
        for (Shift shift : weekShifts) {
            for (Role role : Role.values()) {
                if (isNeeded(shift, role)) return false;
            }
        }
        return true;
    }

    private WeekSchedule getOrCreateWeek(LocalDate date) {
        LocalDate sunday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        return weekSchedules.computeIfAbsent(sunday, WeekSchedule::new);
    }

    public WeekStatus getWeekStatus(LocalDate dateInWeek) {
        WeekSchedule week = getOrCreateWeek(dateInWeek);
        boolean assigned = isWeekAssigned(dateInWeek);
        return week.calculateStatus(assigned);
    }

    public WeekSchedule getNextWeek() {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return getOrCreateWeek(nextSunday);
    }

    public void publishWeekSchedule(LocalDate dateInWeek) {
        WeekStatus status = getWeekStatus(dateInWeek);

        WeekSchedule week = getOrCreateWeek(dateInWeek);
        week.setPublished(true);

        constraintManager.setNextThursdayDeadline();
        constraintManager.resetAllConstraints();
    }

    public boolean isNextWeekPublished() {
        return getNextWeek().isPublished();
    }

    private List<Shift> getShiftsForWeek(LocalDate dateInWeek) {
        LocalDate startDay = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        List<Shift> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDay.plusDays(i);
            for (ShiftType type : ShiftType.values()) {
                Shift shift = getShift(date, type);
                if (shift != null) result.add(shift);
            }
        }
        return result;
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

        // 1. Convert Set to List so we can sort it
        List<Shift> sortedShifts = new ArrayList<>(shifts);
        sortedShifts.sort(Comparator.comparing(Shift::getShiftDate)
                .thenComparing(Shift::getType));

        StringBuilder result = new StringBuilder("=== SHIFT HISTORY ===\n");

        for (Shift shift : sortedShifts) {
            // 2. Use Arrays.stream() for the array returned by Role.values()
            long assignedCount = Arrays.stream(Role.values())
                    .mapToLong(role -> assignments.getEmployees(shift, role).size())
                    .sum();

            // Skip "rest" or "any" types if no one is assigned
            if (assignedCount == 0 && (shift.getType() == ShiftType.rest || shift.getType() == ShiftType.any)) {
                continue;
            }

            result.append(String.format("\nShift: %s - %s\n", shift.getShiftDate(), shift.getType()));

            for (Role role : Role.values()) {
                var employees = assignments.getEmployees(shift, role);
                if (!employees.isEmpty()) {
                    result.append(String.format("  - %-12s | assigned: %d | employees: %s\n",
                            role, employees.size(), employees));
                }
            }
        }

        return result.toString();
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
        String shiftList = assignments.getAssignments().entrySet().stream()
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
}
