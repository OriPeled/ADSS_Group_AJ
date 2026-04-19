package dev.Workers.domain;

import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.WeekSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

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
    private static final Map<LocalDate, WeekSchedule> weekSchedules = new HashMap<>();

    private final Set<Shift> shifts;
    private final Requirements requirements;
    private final Assignments assignments;

    private final ConstraintManager constraintManager;
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
        Shift newShift = new Shift(date, type);
        if (shifts.add(newShift)) {
            requirements.init(newShift);
            assignments.init(newShift);
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

        if (type == ShiftType.morning || type == ShiftType.evening) {
            addShift(date, type);

            for (Shift s : shifts) {
                if (s.getShiftDate().equals(date) && s.getType().equals(type)) {
                    return s;
                }
            }
        }

        return null;
    }

    public void resetShift(Shift shift) {
        shifts.remove(shift);
        requirements.init(shift);
        assignments.init(shift);
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
        employeeManager.validateEmployeeBasic(employeeId);
        if (assignments.isAssignedToShift(shift, employeeId))
            throw new IllegalArgumentException("Employee " + employeeId + " already assigned to this shift" + shift.getShiftDate());
        if (!isQualified(employeeId, role))
            throw new IllegalArgumentException("Employee " + employeeId + " not qualified for this role.");
        if (!isNeeded(shift, role))
            throw new IllegalStateException("Role already assigned");
        if (!isAvailable(employeeId, shift)) {
            throw new IllegalArgumentException("Employee " + employeeId + " is not available for this shift.");
        }
        assignments.add(shift, role, employeeId);
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        if (!isSpecialValid(shift, role, employeeId)) {
            throw new RuntimeException("Employee " + employeeId + " not qualified for this role.");
        }
        assignments.add(shift, role, employeeId);
    }

    public void removeEmployee(Shift shift, Role role, int employeeId) {
        assignments.remove(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentEmployeeId, int newEmployeeId) {
        if (assignments.isShiftEmpty(shift))
            throw new IllegalArgumentException("Shift is empty.");
        if (currentEmployeeId == newEmployeeId)
            throw new IllegalArgumentException("You entered the same ID twice.");
        employeeManager.validateEmployeeBasic(currentEmployeeId);
        employeeManager.validateEmployeeBasic(newEmployeeId);
        if (!assignments.isAssignedToShift(shift, currentEmployeeId))
            throw new IllegalArgumentException("To be replaced employee not assigned to this shift.");

        Role roleCur = assignments.getEmployeeRole(shift, currentEmployeeId);
        Role roleNew = assignments.getEmployeeRole(shift, newEmployeeId);

        if (!isQualified(newEmployeeId, roleCur))
            throw new IllegalArgumentException("Employee " + newEmployeeId + " not qualified for this role.");

        if (roleNew != null) { // if newEmployee is already in this shift
            handleSwap(shift, currentEmployeeId, roleCur, newEmployeeId, roleNew);
        } else { // if newEmployee is not in this shift
            handleSimpleReplacement(shift, currentEmployeeId, roleCur, newEmployeeId);
        }
    }

    private void handleSwap(Shift shift, int currentEmployeeId, Role roleCur, int newEmployeeId, Role roleNew) {
        if (roleCur == roleNew)
            throw new IllegalArgumentException("Employee " + newEmployeeId + " already assigned to this role.");

        if (!isQualified(currentEmployeeId, roleNew))
            throw new IllegalArgumentException("Employee " + currentEmployeeId + "  not qualified for this role.");

        removeEmployee(shift, roleCur, currentEmployeeId);
        removeEmployee(shift, roleNew, newEmployeeId);
        assignEmployee(shift, roleNew, currentEmployeeId);
        assignEmployee(shift, roleCur, newEmployeeId);
    }

    private void handleSimpleReplacement(Shift shift, int currentEmployeeId, Role roleCur, int newEmployeeId) {
        if (!isAvailable(newEmployeeId, shift))
            throw new IllegalArgumentException("Employee " + newEmployeeId + " not available for this shift.");

        removeEmployee(shift, roleCur, currentEmployeeId);
        assignEmployee(shift, roleCur, newEmployeeId);
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
    public boolean isNeeded(Shift shift, Role role) {
        return assignments.countAssigned(shift, role)
                < requirements.countRequired(shift, role);
    }

    public boolean nobodyToAssign(Shift shift, Role role) {
        return isNeeded(shift, role) && countUnassignedValid(shift, role) == 0;
    }

    // unassigned qualified, not available
    public int countUnassignedValid(Shift shift, Role role) {
        int count = 0;
        List<Integer> qualifiedIds = roleManager.getListByRole(role);
        for (int id : qualifiedIds) {
            if (isAvailable(id, shift) && !assignments.isAssignedToShift(shift, id)) {
                count++;
            }
        }
        return count;
    }

    public void setRequirement(Shift shift, Role role, int count) {
        requirements.set(shift, role, count);

        Set<Integer> employees = assignments.getEmployees(shift, role);
        int assigned = employees.size();
        int required = requirements.countRequired(shift, role);

        if (assigned > required) {
            int toRemove = assigned - required;

            List<Integer> idsToRemove = new ArrayList<>(employees).subList(0, toRemove);

            for (Integer id : idsToRemove) {
                removeEmployee(shift, role, id);

            }
        }
    }

    /**
     * Returns all shifts for the next week (7 days from today).
     */
    private List<Shift> getNextWeekShifts() {
       /* if (!getNextWeek().isViewableByUser()) {
            throw new IllegalStateException("The schedule for the next week is not yet published.");
        }*/

        List<Shift> result = new ArrayList<>();

        LocalDate startDay = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // 2. Iterate for 7 days starting from that Sunday
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDay.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.morning, ShiftType.evening}) {
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

    private boolean isShiftAssigned(Shift shift) {
        for (Role role : Role.values()) {
            if (isNeeded(shift, role))
                return false;
        }
        return true;
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

    public WeekStatus getWeekStatus(LocalDate dateInWeek) {
        WeekSchedule week = getOrCreateWeek(dateInWeek);
        boolean assigned = isWeekAssigned(dateInWeek);
        return week.calculateStatus(assigned);
    }

    public void publishWeekSchedule(LocalDate dateInWeek) {
        WeekStatus status = getWeekStatus(dateInWeek);

        WeekSchedule week = getOrCreateWeek(dateInWeek);
        week.setPublished(true);
        constraintManager.resetAllConstraints();
    }

    public static WeekSchedule getOrCreateWeek(LocalDate date) {
        LocalDate sunday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        return weekSchedules.computeIfAbsent(sunday, WeekSchedule::new);
    }

    public static WeekSchedule getNextWeek() {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return getOrCreateWeek(nextSunday);
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

    public String getUnassignedValid(Shift shift) {
        EmployeeManager employeeManager = EmployeeManager.getInstance();
        StringBuilder result = new StringBuilder("=== Shift Assignment Assistant ===\n");
        boolean foundAnyOverall = false;

        for (Role role : Role.values()) {
            if (!isNeeded(shift, role)) continue; // Skip roles that are already full

            result.append("\n--- ").append(role).append(" ---\n");
            List<Integer> qualifiedIds = roleManager.getListByRole(role);

            // Check if we are in "Desperation Mode" (Needed but nobody available)
            boolean desperationMode = nobodyToAssign(shift, role);

            if (desperationMode) {
                result.append("!!! NO AVAILABLE EMPLOYEES !!!\n");
                result.append("Qualified employees with constraint conflicts:\n");
            }

            boolean foundForRole = false;
            for (int id : qualifiedIds) {
                boolean available = isAvailable(id, shift);
                boolean assigned = assignments.isAssignedToShift(shift, id);
                Employee emp = employeeManager.getById(id);

                if (desperationMode) {
                    // Logic: Qualified, NOT available, and NOT already in this shift
                    if (!available && !assigned) {
                        result.append(String.format("  [REJECTED] %s (ID: %d)\n",
                                emp.getName(), id));
                        foundForRole = true;
                    }
                } else {
                    // Logic: Qualified and Available
                    if (available && !assigned) {
                        result.append(String.format("  [READY] %s (ID: %d)\n", emp.getName(), id));
                        foundForRole = true;
                        foundAnyOverall = true;
                    }
                }
            }

            if (!foundForRole) {
                result.append(desperationMode ? "  (No qualified employees found even with conflicts)\n"
                        : "  (No available qualified employees)\n");
            }
        }

        return foundAnyOverall || result.length() > 30 ? result.toString() : "No assignment actions possible.";
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

    public String displayWeekAssignments() {
        Map<Shift, String> assignments = weekAssignment();

        // 1. Filter and Sort: Sun -> Sat, Morning -> Evening
        List<String> formattedLines = assignments.keySet().stream()
                .filter(s -> s.getType() == ShiftType.morning || s.getType() == ShiftType.evening)
                .sorted(Comparator.comparing(Shift::getShiftDate)
                        .thenComparing(Shift::getType))
                // REMOVED: manually adding (shiftType) here
                .map(s -> String.format("%s: %s", s.toStringByWeekDay(), assignments.get(s)))
                .collect(Collectors.toList());

        if (formattedLines.isEmpty()) return "No shifts to display.";

        // 2. Column logic (Total 14 shifts: 5 + 5 + 4)
        StringBuilder sb = new StringBuilder();
        int col1Count = 5;
        int col2Count = 5;

        // Determine padding based on the longest string to keep columns aligned
        int padding = formattedLines.stream().mapToInt(String::length).max().orElse(25) + 4;

        // 3. Print row by row (max 5 rows)
        for (int row = 0; row < 5; row++) {
            // Column 1 (Indices 0-4)
            sb.append(String.format("%-" + padding + "s", formattedLines.get(row)));

            // Column 2 (Indices 5-9)
            if (row + col1Count < formattedLines.size()) {
                sb.append(String.format("%-" + padding + "s", formattedLines.get(row + col1Count)));
            }

            // Column 3 (Indices 10-13)
            if (row + col1Count + col2Count < formattedLines.size()) {
                sb.append(formattedLines.get(row + col1Count + col2Count));
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     *
     * @return  full shifts assignment history by date, shift type , amount
     */
    public String ShiftHistory() {
        if (shifts.isEmpty()) {
            return "No shifts available.";
        }

        // Filter only past shifts (up to yesterday), and sort them
        List<Shift> publishedShifts = shifts.stream()
                .filter(shift -> {
                    // Include only shifts before today
                    if (!shift.getShiftDate().isBefore(LocalDate.now())) {
                        return false;
                    }

                    // Find the week this shift belongs to
                    LocalDate sunday = shift.getShiftDate()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                    WeekSchedule week = weekSchedules.get(sunday);

                    // Only include if the week exists
                    // If you want only published weeks, replace with:
                    // return week != null && week.isPublished();
                    return week != null;
                })
                .sorted(Comparator.comparing(Shift::getShiftDate)
                        .thenComparing(Shift::getType))
                .collect(Collectors.toList());

        if (publishedShifts.isEmpty()) {
            return "No published shifts to display.";
        }

        StringBuilder result = new StringBuilder("=== SHIFT HISTORY ===\n");

        for (Shift shift : publishedShifts) {
            long assignedCount = Arrays.stream(Role.values())
                    .mapToLong(role -> assignments.getEmployees(shift, role).size())
                    .sum();

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
}