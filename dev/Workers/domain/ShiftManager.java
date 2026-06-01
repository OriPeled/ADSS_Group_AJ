package dev.Workers.domain;

import dev.Workers.setup.TransportModule;
import dev.Workers.domain.Actions.RequestAction;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;
import dev.Workers.domain.Objects.Role;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static dev.Workers.domain.Enums.ShiftType.EVENING;
import static dev.Workers.domain.Enums.ShiftType.MORNING;

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
    private final RequirementsHandler requirementsHandler;

    private final Assignments assignments;
    private final ConstraintManager constraintManager;
    private final EmployeeManager employeeManager;
    private final RoleRegistry roleRegistry;

    private static ShiftManager instance;

    public static ShiftManager getInstance() {
        if (instance == null) {
            instance = new ShiftManager();
        }
        return instance;
    }

    private ShiftManager() {
        this.shifts = new HashSet<>();
        this.requirementsHandler = new RequirementsHandler();
        this.constraintManager = ConstraintManager.getInstance();
        this.assignments = new Assignments();
        this.employeeManager = EmployeeManager.getInstance();
        this.roleRegistry = RoleRegistry.getInstance();
    }

    /**
     *
     * @param date
     * @param type
     * adding shift to the system if it doesn't already exist, else nothing
     */
    public void addShift(Branch branch, LocalDate date, ShiftType type) {
        Shift newShift = new Shift(branch, date, type);
        if (shifts.add(newShift)) {
            requirementsHandler.init(newShift);
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
    public Shift getShift(Branch branch, LocalDate date, ShiftType type) {
        for (Shift s : shifts) {
            if (s.getBranch().equals(branch) && s.getDate().equals(date) && s.getType().equals(type)) {
                return s;
            }
        }

        if (type == MORNING || type == ShiftType.EVENING) {
            addShift(branch, date, type);

            for (Shift s : shifts) {
                if (s.getBranch().equals(branch) && s.getDate().equals(date) && s.getType().equals(type)) {
                    return s;
                }
            }
        }

        return null; // never occurs
    }

    public Shift getExistingShift(Branch branch, LocalDate date, ShiftType type) {
        Shift shift = null;
        for (Shift s : shifts) {
            if (s.getBranch().equals(branch) && s.getDate().equals(date) && s.getType().equals(type)) {
                shift = s;
                break;
            }
        }

        if (shift == null)
            throw new IllegalArgumentException("Shift doesn't exist.");

        return shift;
    }

    public Shift getExistingShift(Branch branch, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Shift shift = null;
        for (Shift s : shifts) {
            if (s.getBranch().equals(branch) && s.getDate().equals(date) && s.getStartTime().equals(startTime)
                    && s.getEndTime().equals(endTime)) {
                shift = s;
                break;
            }
        }

        if (shift == null)
            throw new IllegalArgumentException("Shift doesn't exist.");

        return shift;
    }

    public Assignments getAssignments() {
        return assignments;
    }

    // for rare cases
    public void resetShift(Shift shift) {
        shifts.remove(shift);
        requirementsHandler.init(shift);
        assignments.init(shift);
    }

    public boolean isShiftsWeekEmpty(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        for (Shift shift : shifts) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                return false;
            }
        }
        return true;
    }

    public int leftToAssign(Shift shift, Role role) {
        return requirementsHandler.countRequired(shift, role)
                - assignments.countAssigned(shift, role);
    }

    public void assignEmployee(Shift shift, Role role, int employeeId) {
        employeeManager.validateEmployeeBasic(employeeId, shift.getDate());
        if (!isNeeded(shift, role))
            throw new IllegalStateException("Role already assigned");
        if (assignments.isAssignedToShift(shift, employeeId))
            throw new IllegalArgumentException("Employee " + employeeId + " already assigned to this shift" + shift.getDate());
        if (assignments.isRequestedToShift(shift, employeeId))
            throw new IllegalArgumentException("Employee " + employeeId + " was already requested to assign to this shift" + shift.getDate());
        if (!role.isQualified(employeeId))
            throw new IllegalArgumentException("Employee " + employeeId + " not qualified for this role (" + role + ").");
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

    public boolean needToForceAssign(Shift shift, Role role, int employeeId) {
        Employee emp = employeeManager.getById(employeeId);
        return nobodyToAssign(shift, role)
                && role.isQualified(employeeId)
                && !assignments.isRequestedToShift(shift, employeeId);
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        /* FOR TESTS
        employeeManager.validateEmployeeBasic(employeeId, shift.getShiftDate());
        if (!isNeeded(shift, role))
            throw new RuntimeException("Role already assigned.");
        if (!isQualified(employeeId, role)) {
            throw new RuntimeException("Employee " + employeeId + " not qualified for this role.");
        }*/

        if (employeeManager.getById(employeeId).isManager() && !hasManager(shift)) {
            assignments.add(shift, role, employeeId);
            shift.setManaged(true);
        }

        assignments.add(shift, role, employeeId);

        DayOfWeek shiftDay = shift.getShiftDay();
        ShiftType shiftType = shift.getType();
        constraintManager.extendConstraints(employeeId, shiftDay, shiftType); // for potential replacement
    }

    // no required qualification/constraint/activity
    // can be at multiple roles
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

    public void replaceEmployee(Shift shift, int curId, int newId) {
        if (assignments.isShiftEmpty(shift))
            throw new IllegalArgumentException("Shift is empty.");
        if (curId == newId)
            throw new IllegalArgumentException("You entered the same ID twice.");
        employeeManager.validateEmployeeBasic(curId, shift.getDate());
        employeeManager.validateEmployeeBasic(newId, shift.getDate());
        if (!assignments.isAssignedToShift(shift, curId))
            throw new IllegalArgumentException("To be replaced employee not assigned to this shift.");
        if (assignments.isRequestedToShift(shift, newId))
            throw new IllegalArgumentException("Employee " + newId + " was already requested to assign to this shift" + shift.getDate());

        Role roleCur = assignments.getEmployeeRole(shift, curId);
        Role roleNew = assignments.getEmployeeRole(shift, newId);

        if (!roleCur.isQualified(newId))
            throw new IllegalArgumentException("Employee " + newId + " not qualified for this role.");

        if (roleNew != null) { // if newEmployee is already in this shift
            handleSwap(shift, curId, roleCur, newId, roleNew);
        } else { // if newEmployee is not in this shift
            handleSimpleReplacement(shift, curId, roleCur, newId);
        }
    }

    private void handleSwap(Shift shift, int currentEmployeeId, Role roleCur, int newEmployeeId, Role roleNew) {
        if (roleCur == roleNew)
            throw new IllegalArgumentException("Employee " + newEmployeeId + " already assigned to this role.");

        if (!roleNew.isQualified(currentEmployeeId))
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

    public boolean needToForceReplace(Shift shift, int curId, int newId) {
        Role role = assignments.getEmployeeRole(shift, curId);
        if (role == null)
            return false;
        return countUnassignedValid(shift, role) == 0
                && role.isQualified(newId);
    }

    // called when curId role is assigned and no available employees to replace
    public void forceReplace(Shift shift, int curId, int newId) {
        Role roleCur = assignments.getEmployeeRole(shift, curId);

        removeEmployee(shift, curId);
        forceAssign(shift, roleCur, newId);
    }

    /**
     * Checks if employee is available according to constraints.
     */
    private boolean isAvailable(int id, Shift shift) {
        return constraintManager.isEmployeeAvailable(
                id,
                shift.getDate().getDayOfWeek(),
                shift.getType()
        );
    }

    /**
     * Checks if role still has available demand in shift.
     */
    public boolean isNeeded(Shift shift, Role role) {
        return assignments.countAssigned(shift, role)
                < requirementsHandler.countRequired(shift, role);
    }

    public boolean nobodyToAssign(Shift shift, Role role) {
        return isNeeded(shift, role) && countUnassignedValid(shift, role) == 0;
    }

    // unassigned qualified, not available
    public int countUnassignedValid(Shift shift, Role role) {
        int count = 0;

        // We use the passed-in service here
        List<Integer> qualifiedIds = employeeManager.getListByRole(role);

        for (int id : qualifiedIds) {
            if (isAvailable(id, shift) && !assignments.isAssignedToShift(shift, id)) {
                count++;
            }
        }
        return count;
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        assignments.addRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        assignments.addRequest(shift, curId, newId);
    }

    public String processRequest(int employeeId, boolean isApproved) {
        Queue<RequestAction> queue = assignments.getRequests(employeeId);
        if (queue == null || queue.isEmpty()) return "No requests.";

        RequestAction action = queue.poll();
        String employeeName = employeeManager.getById(employeeId).getName();
        String status = isApproved ? "APPROVED" : "REJECTED";

        if (isApproved) {
            try {
                action.execute(this);
            } catch (Exception e) {
                return "Execution failed: " + e.getMessage();
            }
        }

        // BROADCAST TO HR:
        String message = String.format("Employee %s (%d) %s: %s",
                employeeName, employeeId, status, action.getDescription());

        Branch branch = action.shift().getBranch();
        assignments.addRequestAnswer(branch, message);

        return "Response recorded: " + status;
    }

    public void approveNextAssignment(int empID) {
        Queue<RequestAction> queue = assignments.getRequests(empID);
        RequestAction action = queue.poll();
        action.execute(this);
    }

    public boolean assignmentNeedsApproval(int empID) {
        return assignments.hasRequests(empID);
    }

    public String displayNextPendingAssignment(int employeeId) {
        Queue<RequestAction> queue = assignments.getRequests(employeeId);

        if (queue == null || queue.isEmpty()) {
            return "No pending requests for Employee ID: " + employeeId;
        }

        // Look at the head of the FIFO queue without removing it
        RequestAction nextAction = queue.peek();

        StringBuilder sb = new StringBuilder("=== NEXT PENDING REQUEST ===\n");
        sb.append("Employee ID: ").append(employeeId).append("\n");
        sb.append("Details    : ").append(nextAction.getDescription()).append("\n");
        sb.append("----------------------------\n");
        sb.append("Enter 1 to Approve, 0 to Skip/Stay in queue.");

        return sb.toString();
    }

    private void finalizeShiftRequests(Shift shift) {
        // Iterate through every employee's pending queue
        assignments.getAllPendingRequests().forEach((empId, queue) -> {
            // Find actions in this queue belonging to this shift
            // We use an iterator so we can safely remove items while looping
            var iterator = queue.iterator();
            while (iterator.hasNext()) {
                RequestAction action = iterator.next();
                if (action.shift().equals(shift)) {
                    // Execute the action (force-assign/replace)
                    action.execute(this);
                    // Remove it from their queue since it's now handled
                    iterator.remove();
                }
            }
        });
    }

    public void setRequirement(Shift shift, Role role, int count) {
        /*if (role == Role.shiftManager && count < 1) {
            throw new IllegalArgumentException(
                    "Cannot set shift manager requirement below 1: every shift must have at least one shift manager.");
        }*/
        if (role instanceof DriverRole) {
            throw new IllegalArgumentException("Driver requirements are set only once during init process.");
        }
        requirementsHandler.set(shift, role, count);

        Set<Integer> employees = assignments.getEmployeesByRole(shift, role);
        int assigned = employees.size();
        int required = requirementsHandler.countRequired(shift, role);

        // randomly removing redundant employees
        if (assigned > required) {
            int toRemove = assigned - required;

            List<Integer> idsToRemove = new ArrayList<>(employees).subList(0, toRemove);

            for (Integer id : idsToRemove) {
                removeEmployee(shift, id);
            }
        }
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        employeeManager.validateEmployeeBasic(empID, shift.getDate());
        assignments.updateExtraHours(shift, empID, hours);
    }

    /**
     * Returns all shifts for the next week (7 days from today).
     */
    private List<Shift> getNextWeekShifts(Branch branch) {
        /*if (!getNextWeek().isViewableByUser()) {
            throw new IllegalStateException("The schedule for the next week is not yet published.");
        }*/

        List<Shift> result = new ArrayList<>();

        LocalDate startDay = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // 2. Iterate for 7 days starting from that Sunday
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDay.plusDays(i);

            for (ShiftType type : new ShiftType[]{MORNING, ShiftType.EVENING}) {
                Shift shift = getShift(branch, date, type);
                if (shift != null) {
                    result.add(shift);
                }
            }
        }

        return result;
    }

    public Map<Shift, String> weekAssignment(Branch branch) {
        return getNextWeekShifts(branch).stream()
                .collect(Collectors.toMap(s -> s, this::getShiftStatus));
    }

    public String getShiftStatus(Shift shift) {
        boolean rolesFullApproved = roleRegistry.getAllRoles().stream()
                .allMatch(r -> assignments.getEmployeesByRole(shift, r).size() >= requirementsHandler.countRequired(shift, r));

        boolean managerApproved = hasManager(shift);

        if (rolesFullApproved && managerApproved) return "COMPLETE";

        // Check "Tentative" counts (Approved + Pending)
        boolean rolesFullTentative = roleRegistry.getAllRoles().stream()
                .allMatch(r -> {
                    int combined = assignments.getEmployeesByRole(shift, r).size() + getPendingIds(shift, r).size();
                    return combined >= requirementsHandler.countRequired(shift, r);
                });

        // Check if a manager is pending
        // Check if a manager is pending (Fixed Version)
        boolean managerPending = assignments.getAllPendingRequests().values().stream()
                .flatMap(Collection::stream)
                .anyMatch(action -> {
                    // 1. Check if it's the right shift
                    if (!action.shift().equals(shift)) {
                        return false;
                    }

                    // 2. Check if it is an Assignment Action
                    if (action instanceof RequestAction.AssignAction assignAction) {
                        // If it is, check if the person being assigned is a manager
                        return employeeManager.getById(assignAction.empId()).isManager();
                    }

                    // 3. Check if it is a Replace Action
                    if (action instanceof RequestAction.ReplaceAction replaceAction) {
                        // For replacements, check if the NEW person coming in is a manager
                        return employeeManager.getById(replaceAction.newId()).isManager();
                    }

                    return false;
                });

        if (rolesFullTentative && (managerApproved || managerPending)) {
            return "COMPLETE*";
        }

        return "INCOMPLETE";
    }

    public boolean isShiftAssigned(Shift shift) {
        return getShiftStatus(shift).startsWith("COMPLETE");
    }

    private boolean isWeekAssigned(Branch branch, LocalDate dateInWeek) {
        return getShiftsForWeek(branch, dateInWeek).stream()
                .allMatch(s -> getShiftStatus(s).equals("COMPLETE"));
    }

    public WeekStatus getWeekStatus(Branch branch, LocalDate dateInWeek) {
        WeekSchedule week = getOrCreateWeek(dateInWeek);
        boolean assigned = isWeekAssigned(branch, dateInWeek);
        return week.calculateStatus(assigned);
    }

    public void publishWeekSchedule(Branch branch, LocalDate dateInWeek) {
        List<Shift> weekShifts = getShiftsForWeek(branch, dateInWeek);

        // Find any shifts that are blocking the publication
        List<String> problematicShifts = weekShifts.stream()
                .filter(s -> !getShiftStatus(s).equals("COMPLETE"))
                .map(s -> String.format("%s (%s)", s.toStringByWeekDay(), getShiftStatus(s)))
                .collect(Collectors.toList());

        if (!problematicShifts.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot publish: The following shifts are not fully finalized:\n" +
                            String.join(", ", problematicShifts) +
                            "\n\nNote: All pending requests (*) must be approved before publishing."
            );
        }

        // Success Path
        WeekSchedule week = getOrCreateWeek(dateInWeek);
        week.setPublished(true);

        assignments.resetRequests();
        constraintManager.resetAllConstraints();
    }

    public void forcePublishWeekSchedule(Branch branch, LocalDate dateInWeek) {
        List<Shift> weekShifts = getShiftsForWeek(branch, dateInWeek);

        // 1. Check for hard-stoppers (INCOMPLETE)
        List<String> incompleteShifts = weekShifts.stream()
                .filter(s -> getShiftStatus(s).equals("INCOMPLETE"))
                .map(s -> s.toStringByWeekDay())
                .collect(Collectors.toList());

        if (!incompleteShifts.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot Force-Publish: These shifts are still INCOMPLETE:\n" +
                            String.join(", ", incompleteShifts)
            );
        }

        // 2. Convert all COMPLETE* to COMPLETE (Force-approve pending requests)
        for (Shift shift : weekShifts) {
            if (getShiftStatus(shift).equals("COMPLETE*")) {
                finalizeShiftRequests(shift);
            }
        }

        // 3. Finalize Publication
        WeekSchedule week = getOrCreateWeek(dateInWeek);
        week.setPublished(true);

        // Cleanup
        assignments.resetRequests();
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

    private List<Shift> getShiftsForWeek(Branch branch, LocalDate dateInWeek) {
        LocalDate startDay = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        List<Shift> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDay.plusDays(i);
            for (ShiftType type : ShiftType.values()) {;
                Shift shift = getShift(branch, date, type);
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
        for (Role role : roleRegistry.getAllRoles()) {
            int req = requirementsHandler.countRequired(shift, role);
            Set<Integer> approved = assignments.getEmployeesByRole(shift, role);
            Set<Integer> pending = getPendingIds(shift, role); // Get the * people

            if (req > 0 || !approved.isEmpty() || !pending.isEmpty()) {
                // Build employee list: "101, 102*, 105"
                StringJoiner sj = new StringJoiner(", ");
                approved.forEach(id -> sj.add(String.valueOf(id)));
                pending.forEach(id -> sj.add(id + "*"));

                int totalAssigned = approved.size() + pending.size();
                sb.append(String.format("  %-12s: %d/%d assigned | Employees: [%s]\n",
                        role, totalAssigned, req, sj));
            }
        }
        return sb.toString();
    }

    // Helper 3: The UI for a Detailed Shift Block (Vertical view)
    private String formatShiftBlock(Shift s) {
        String footnote = "\n* needs to approve\n";
        return String.format("\nShift: %s - %s\n%s%s%s",
                s.getDate(), s.getType(), getRoleAssignmentsStr(s), getExtraHoursStr(s, null, false), footnote);
    }

    // Helper 4: The UI for the Week Schedule Dashboard Grid (3-column layout)
    private String buildGrid(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        int padding = lines.stream().mapToInt(String::length).max().orElse(25) + 4;
        for (int row = 0; row < 5; row++) {
            sb.append(String.format("%-" + padding + "s", lines.get(row)));
            if (row + 5 < lines.size()) sb.append(String.format("%-" + padding + "s", lines.get(row + 5)));
            if (row + 10 < lines.size()) sb.append(lines.get(row + 10));
            sb.append("\n");
        }
        sb.append("\n* awaiting employee approvals\"\n");
        return sb.toString();
    }

    private Set<Integer> getPendingIds(Shift shift, Role role) {
        // Collect all employee IDs who have a pending "AssignAction" for this specific shift and role
        return assignments.getAllPendingRequests().entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(action -> action instanceof RequestAction.AssignAction a
                                && a.shift().equals(shift) && a.role().equals(role)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public String getUnassignedValid(Shift shift) {
        boolean rolesFull = roleRegistry.getAllRoles().stream().noneMatch(r -> isNeeded(shift, r));
        boolean hasManager = hasManager(shift);

        StringBuilder sb = new StringBuilder("=== Shift Assignment Assistant ===\n");

        // 1. Informative Header (Doesn't block the list anymore)
        if (rolesFull && hasManager) {
            sb.append("✔ Shift is fully assigned and managed.\n");
        } else if (rolesFull && !hasManager) {
            sb.append("! WARNING: Role counts met, but NO MANAGER assigned.\n");
        } else {
            sb.append("! Requirements not yet satisfied.\n");
        }

        sb.append("Listing all active, unassigned employees:\n");

        for (Role role : roleRegistry.getAllRoles()) {
            StringBuilder section = new StringBuilder("\n--- " + role + " ---\n");
            boolean foundInRole = false;

            // Get everyone qualified for this role
            for (int id : employeeManager.getListByRole(role)) {
                Employee emp = employeeManager.getById(id);

                // Criteria: Must be active today AND not already working this shift
                if (emp.isActive(shift.getDate()) && !assignments.isAssignedToShift(shift, id)) {

                    boolean available = isAvailable(id, shift);
                    String status = available ? "[READY]" : "[REJECTED]";
                    String managerTag = emp.isManager() ? " (Manager)" : "";

                    section.append(String.format("  %s %-15s (ID: %d)%s\n",
                            status, emp.getName(), id, managerTag));
                    foundInRole = true;
                }
            }

            // Only add the role section if there are actually people to show
            if (foundInRole) sb.append(section);
        }

        return sb.length() > 60 ? sb.toString() : "No active employees found in the system.";
    }

    public String getShiftDetails(Shift shift) {
        return "=== " + shift + " ===\n" + formatShiftBlock(shift);
    }

    public String employeeWeekDisplay(int id, LocalDate refDate) {
        WeekSchedule week = getOrCreateWeek(refDate);
        if (!week.isViewableByUser()) return "The schedule for this week is not yet published.";
        Branch empBranch = employeeManager.getById(id).getBranch();

        String content = getShiftsForWeek(empBranch, refDate).stream()
                .filter(s -> assignments.isAssignedToShift(s, id))
                .map(s -> String.format("- %s (%s) | Role: %s%s",
                        s.getDate(), s.getType(), assignments.getEmployeeRole(s, id), getExtraHoursStr(s, id, true)))
                .collect(Collectors.joining("\n"));

        return content.isEmpty() ? "No shifts found for ID " + id : "Shifts for ID " + id + ":\n" + content;
    }

    // for HR Manager
    public String displayWeekAssignments(Branch branch) {
        Map<Shift, String> statusMap = weekAssignment(branch);
        List<String> lines = statusMap.keySet().stream()
                .sorted(Comparator.comparing(Shift::getDate).thenComparing(Shift::getType))
                .map(s -> String.format("%s: %s", s.toStringByWeekDay(), statusMap.get(s)))
                .collect(Collectors.toList());

        return lines.isEmpty() ? "No shifts to display." : buildGrid(lines);
    }

    // for employees
    public String displayPublishedWeek(Branch branch, LocalDate date) {
        if (!getOrCreateWeek(date).isPublished()) return "Schedule not yet published.";

        String content = getShiftsForWeek(branch, date).stream()
                .map(this::formatShiftBlock)
                .collect(Collectors.joining());

        return "=== PUBLISHED SCHEDULE ===\n" + content;
    }

    public String ShiftHistory(Branch branch) {
        String content = shifts.stream()
                .filter(s -> s.getDate().isBefore(LocalDate.now()))
                .filter(s -> s.getBranch() == branch)
                .sorted(Comparator.comparing(Shift::getDate).thenComparing(Shift::getType))
                .map(this::formatShiftBlock)
                .collect(Collectors.joining());

        return content.isEmpty() ? "No history available." : "=== SHIFT HISTORY ===\n" + content;
    }

    // TP holds LicenseType enum
    public void getDriverReqs(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        for (Shift shift : shifts) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                Map<LicenseType, Integer> licensesNeeded = TransportModule.
                        getDriverRequirements(branch, shift.getDate(), shift.getStartTime(), shift.getEndTime());

                if (licensesNeeded != null) {
                    for (Map.Entry<LicenseType, Integer> entry : licensesNeeded.entrySet()) {
                        // Fetch the specific DriverRole for this license from the registry
                        // (Ensure the string matches how you named it in RoleRegistry!)
                        Role driverRole = roleRegistry.getRoleByName("Driver (" + entry.getKey() + ")");
                        requirementsHandler.set(shift, driverRole, entry.getValue());
                    }
                }
            }
        }
    }

    public void getStoreKeeperReqs(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        for (Shift shift : shifts) {
            LocalDate shiftDate = shift.getDate();
            if (!shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                int amount = TransportModule.
                        getStorekeeperRequirements(branch, shift.getDate(), shift.getStartTime(), shift.getEndTime());
                requirementsHandler.set(shift, storekeeperRole, amount);
            }
        }
    }

    public void setCashierWeekReqs(Branch branch, int amount) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Role cashierRole = roleRegistry.getRoleByName("Cashier");

        for (Shift shift : shifts) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                requirementsHandler.set(shift, cashierRole, amount);
            }
        }
    }

    public void setStoreKeeperWeekReqs(Branch branch, int amount) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        for (Shift shift : shifts) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                requirementsHandler.set(shift, storekeeperRole, amount);
            }
        }
    }

    // used by TP module
    public Set<Integer> getShiftDrivers(Branch branch, LocalDate shiftDate, LocalTime startTime, LocalTime endTime) {
        Shift shift = getExistingShift(branch, shiftDate, startTime, endTime);
        return assignments.getAllDrivers(shift);
    }

    public void initShiftsWeek(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDate curDay = startOfWeek;

        while (!curDay.isAfter(endOfWeek)) {
            addShift(branch, curDay, MORNING);
            addShift(branch, curDay, EVENING);
            curDay = curDay.plusDays(1);
        }
    }
}