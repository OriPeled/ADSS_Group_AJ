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
     * @return getter for a next week's shift,
     *         could be either already published or currently on assignment process
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

        return null; // never occurs
    }

    /**
     *
     * @param date
     * @param type
     * @return getter for an existing shift, null if doesn't exist
     */
    public Shift getExistingShift(LocalDate date, ShiftType type) {
        Shift shift = null;
        for (Shift s : shifts) {
            if (s.getShiftDate().equals(date) && s.getType().equals(type)) {
                shift = s;
                break;
            }
        }

        if (shift == null)
            throw new IllegalArgumentException("Shift doesn't exist.");

        return shift;
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
        employeeManager.validateEmployeeBasic(employeeId, shift.getShiftDate());
        if (!isNeeded(shift, role))
            throw new IllegalStateException("Role already assigned");
        if (assignments.isAssignedToShift(shift, employeeId))
            throw new IllegalArgumentException("Employee " + employeeId + " already assigned to this shift" + shift.getShiftDate());
        if (!isQualified(employeeId, role))
            throw new IllegalArgumentException("Employee " + employeeId + " not qualified for this role.");
        if (!isAvailable(employeeId, shift)) {
            throw new IllegalArgumentException("Employee " + employeeId + " is not available for this shift.");
        }

        if (employeeManager.getById(employeeId).isManager() && !hasManager(shift)) {
            assignments.add(shift, role, employeeId);
            shift.setManaged(true);
        }
        else {
            assignments.add(shift, role, employeeId);
        }
    }

    public boolean hasManager(Shift shift) {
        Set<Integer> shiftEmployees = assignments.getAllEmployees(shift);
        for (Integer id : shiftEmployees) {
            if (employeeManager.getById(id).isManager())
                return true;

        }
        return false;
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        employeeManager.validateEmployeeBasic(employeeId, shift.getShiftDate());
        if (!isNeeded(shift, role))
            throw new RuntimeException("Role already assigned.");
        if (!isQualified(employeeId, role)) {
            throw new RuntimeException("Employee " + employeeId + " not qualified for this role.");
        }

        if (employeeManager.getById(employeeId).isManager() && !hasManager(shift)) {
            assignments.add(shift, role, employeeId);
            shift.setManaged(true);
        }

        assignments.add(shift, role, employeeId);
    }

    public void manualAssign(Shift shift, Role role, int employeeId) {
        if (!employeeManager.isEmployee(employeeId))
            throw new IllegalArgumentException("No such employee.");

        if (employeeManager.getById(employeeId).isManager() && !hasManager(shift)) {
            assignments.add(shift, role, employeeId);
            shift.setManaged(true);
        }

        assignments.add(shift, role, employeeId);
    }

    public void removeEmployee(Shift shift, int employeeId) {
        assignments.remove(shift, employeeId);
        if (!hasManager(shift)) {
            shift.setManaged(false);
        }
    }

    public void replaceEmployee(Shift shift, int currentEmployeeId, int newEmployeeId) {
        if (assignments.isShiftEmpty(shift))
            throw new IllegalArgumentException("Shift is empty.");
        if (currentEmployeeId == newEmployeeId)
            throw new IllegalArgumentException("You entered the same ID twice.");
        employeeManager.validateEmployeeBasic(currentEmployeeId, shift.getShiftDate());
        employeeManager.validateEmployeeBasic(newEmployeeId, shift.getShiftDate());
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

        removeEmployee(shift, currentEmployeeId);
        removeEmployee(shift, newEmployeeId);
        assignEmployee(shift, roleNew, currentEmployeeId);
        assignEmployee(shift, roleCur, newEmployeeId);
    }

    private void handleSimpleReplacement(Shift shift, int currentEmployeeId, Role roleCur, int newEmployeeId) {
        if (!isAvailable(newEmployeeId, shift))
            throw new IllegalArgumentException("Employee " + newEmployeeId + " not available for this shift.");

        removeEmployee(shift, currentEmployeeId);
        assignEmployee(shift, roleCur, newEmployeeId);
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
        /*if (role == Role.shiftManager && count < 1) {
            throw new IllegalArgumentException(
                    "Cannot set shift manager requirement below 1: every shift must have at least one shift manager.");
        }*/
        requirements.set(shift, role, count);

        Set<Integer> employees = assignments.getEmployeesByRole(shift, role);
        int assigned = employees.size();
        int required = requirements.countRequired(shift, role);

        if (assigned > required) {
            int toRemove = assigned - required;

            List<Integer> idsToRemove = new ArrayList<>(employees).subList(0, toRemove);

            for (Integer id : idsToRemove) {
                removeEmployee(shift, id);

            }
        }
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        employeeManager.validateEmployeeBasic(empID, shift.getShiftDate());
        assignments.updateExtraHours(shift, empID, hours);
    }

    /**
     * Returns all shifts for the next week (7 days from today).
     */
    private List<Shift> getNextWeekShifts() {
        /*if (!getNextWeek().isViewableByUser()) {
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
                    if (isNeeded(shift, role) || !hasManager(shift)) {
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
            if (!hasManager(shift)) {
                return false;
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
        List<Shift> weekShifts = getShiftsForWeek(dateInWeek);
        List<String> missingManager = new ArrayList<>();
        for (Shift shift : weekShifts) {
            if (!shift.hasManager()) {
                missingManager.add(shift.toString());
            }
        }
        if (!missingManager.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot publish schedule: the following shifts have no assigned shift manager: " +
                    String.join(", ", missingManager));
        }

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
            for (ShiftType type : ShiftType.values()) {;
                Shift shift = getShift(date, type);
                if (shift != null) {
                    result.add(shift);
                }
            }
        }
        return result;
    }

    // Helper 1: The UI for Extra Hours (Supports 'inline' for grid and 'block' for details)
    private String getExtraHoursStr(Shift shift, Integer targetId, boolean isInline) {
        Map<Integer, Integer> extra = assignments.getExtraHours(shift);
        if (extra == null || extra.isEmpty()) return "";

        StringJoiner sj = new StringJoiner(", ");
        extra.forEach((id, h) -> {
            if (h > 0 && (targetId == null || targetId.equals(id)))
                sj.add((targetId == null ? "ID " + id : "") + " (+" + h + "h)");
        });

        if (sj.length() == 0) return "";
        return isInline ? " [Extra: " + sj + "]" : "  Extra Hours : " + sj + "\n";
    }

    // Helper 2: The UI for Role Staffing (e.g., Driver: 1/2 assigned)
    private String getRoleAssignmentsStr(Shift shift) {
        StringBuilder sb = new StringBuilder();
        for (Role role : Role.values()) {
            int req = requirements.countRequired(shift, role);
            Set<Integer> emps = assignments.getEmployeesByRole(shift, role);
            if (req > 0 || !emps.isEmpty()) {
                sb.append(String.format("  %-12s: %d/%d assigned | Employees: %s\n",
                        role, emps.size(), req, emps));
            }
        }
        return sb.toString();
    }

    // Helper 3: The UI for a Detailed Shift Block (Vertical view)
    private String formatShiftBlock(Shift s) {
        return String.format("\nShift: %s - %s\n%s%s",
                s.getShiftDate(), s.getType(), getRoleAssignmentsStr(s), getExtraHoursStr(s, null, false));
    }

    // Helper 4: The UI for the Dashboard Grid (3-column layout)
    private String buildGrid(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        int padding = lines.stream().mapToInt(String::length).max().orElse(25) + 4;
        for (int row = 0; row < 5; row++) {
            sb.append(String.format("%-" + padding + "s", lines.get(row)));
            if (row + 5 < lines.size()) sb.append(String.format("%-" + padding + "s", lines.get(row + 5)));
            if (row + 10 < lines.size()) sb.append(lines.get(row + 10));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getUnassignedValid(Shift shift) {
        boolean rolesFull = Arrays.stream(Role.values()).noneMatch(r -> isNeeded(shift, r));
        boolean hasManager = hasManager(shift); // Using the helper already in your class

        if (rolesFull && hasManager)
            return "Shift is fully assigned and managed — no additional actions needed.";

        StringBuilder sb = new StringBuilder("=== Shift Assignment Assistant ===\n");
        if (rolesFull && !hasManager)
            sb.append("! WARNING: NO MANAGER assigned. Showing qualified replacements:\n");

        for (Role role : Role.values()) {
            // Skip roles that are full UNLESS we are specifically looking for a manager
            if (rolesFull && hasManager || (!isNeeded(shift, role) && hasManager)) continue;

            StringBuilder section = new StringBuilder("\n--- " + role + " ---\n");
            boolean desperation = nobodyToAssign(shift, role);
            boolean foundInRole = false;

            for (int id : roleManager.getListByRole(role)) {
                Employee emp = employeeManager.getById(id);
                boolean available = isAvailable(id, shift);

                // Logic: Show if Active AND Not Assigned AND (Available match Desperation status)
                if (emp.isActive(shift.getShiftDate()) && !assignments.isAssignedToShift(shift, id)
                        && (available != desperation)) {

                    String status = available ? "[READY]" : "[REJECTED]";
                    String managerTag = emp.isManager() ? " (Manager)" : "";
                    section.append(String.format("  %s %s (ID: %d)%s\n", status, emp.getName(), id, managerTag));
                    foundInRole = true;
                }
            }
            if (foundInRole) sb.append(section);
        }

        return sb.length() > 40 ? sb.toString() : "No active employees found to satisfy requirements.";
    }

    public String getShiftDetails(Shift shift) {
        return "=== " + shift + " ===\n" + formatShiftBlock(shift);
    }

    public String employeeWeekDisplay(int id, LocalDate refDate) {
        WeekSchedule week = getOrCreateWeek(refDate);
        if (!week.isViewableByUser()) return "The schedule for this week is not yet published.";

        String content = getShiftsForWeek(refDate).stream()
                .filter(s -> assignments.isAssignedToShift(s, id))
                .map(s -> String.format("- %s (%s) | Role: %s%s",
                        s.getShiftDate(), s.getType(), assignments.getEmployeeRole(s, id), getExtraHoursStr(s, id, true)))
                .collect(Collectors.joining("\n"));

        return content.isEmpty() ? "No shifts found for ID " + id : "Shifts for ID " + id + ":\n" + content;
    }

    // for HR Manager
    public String displayWeekAssignments() {
        Map<Shift, String> statusMap = weekAssignment();
        List<String> lines = statusMap.keySet().stream()
                .sorted(Comparator.comparing(Shift::getShiftDate).thenComparing(Shift::getType))
                .map(s -> String.format("%s: %s%s", s.toStringByWeekDay(), statusMap.get(s), getExtraHoursStr(s, null, true)))
                .collect(Collectors.toList());

        return lines.isEmpty() ? "No shifts to display." : buildGrid(lines);
    }

    // for employees
    public String displayPublishedWeek(LocalDate date) {
        if (!getOrCreateWeek(date).isPublished()) return "Schedule not yet published.";

        String content = getShiftsForWeek(date).stream()
                .map(this::formatShiftBlock)
                .collect(Collectors.joining());

        return "=== PUBLISHED SCHEDULE ===\n" + content;
    }

    public String ShiftHistory() {
        String content = shifts.stream()
                .filter(s -> s.getShiftDate().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Shift::getShiftDate).thenComparing(Shift::getType))
                .map(this::formatShiftBlock)
                .collect(Collectors.joining());

        return content.isEmpty() ? "No history available." : "=== SHIFT HISTORY ===\n" + content;
    }
}