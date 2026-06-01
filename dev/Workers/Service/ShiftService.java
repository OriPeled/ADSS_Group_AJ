package dev.Workers.Service;

import dev.Workers.domain.Assignments;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * ShiftService acts as the entry point for UI/Web controllers.
 * It delegates all state management and business rules to the ShiftManager.
 */
public class ShiftService {
    private static ShiftManager shiftManager;
    private static Assignments assignments;
    private static EmployeeManager employeeManager;
    private static RoleService roleService;

    private static ShiftService instance;

    /**
     * Singleton access
     */
    public static ShiftService getInstance() {
        if (instance == null) {
            instance = new ShiftService();
        }
        return instance;
    }

    private ShiftService() {
        shiftManager = ShiftManager.getInstance();
        assignments = shiftManager.getAssignments();
        employeeManager = EmployeeManager.getInstance();
        roleService = RoleService.getInstance();
    }

    public void addShift(Branch branch, LocalDate date, ShiftType type) {
        shiftManager.addShift(branch, date, type);
    }

    public Shift getShift(Branch branch, LocalDate date, ShiftType type) {
        return shiftManager.getShift(branch, date, type);
    }

    public Shift getExistingShift(Branch branch, LocalDate date, ShiftType type) {
        return shiftManager.getExistingShift(branch, date, type);
    }

    public void resetShift(Shift shift) {
        shiftManager.resetShift(shift);
    }

    public boolean isShiftsWeekEmpty(Branch branch) {
        return shiftManager.isShiftsWeekEmpty(branch);
    }

    public void setRequirement(Shift shift, Role role, int count) {
        shiftManager.setRequirement(shift, role, count);
    }

    public void assignEmployee(Shift shift, Role role, int employeeId) {
        shiftManager.assignEmployee(shift, role, employeeId);
    }

    public boolean needToForceAssign(Shift shift, Role role, int employeeId) {
        return shiftManager.needToForceAssign(shift, role, employeeId);
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        shiftManager.forceAssign(shift, role, employeeId);
    }

    // assign any existing employee for special cases
    public void manualAssign(Shift shift, Role role, int employeeId) {
        shiftManager.manualAssign(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentId, int newId) {
        shiftManager.replaceEmployee(shift, currentId, newId);
    }

    public boolean needToForceReplace(Shift shift, int curId, int newId) {
        employeeManager.validateEmployeeBasic(curId, shift.getDate());
        employeeManager.validateEmployeeBasic(newId, shift.getDate());

        return shiftManager.needToForceReplace(shift, curId, newId);
    }

    public void forceReplace(Shift shift, int curId, int newId) {
        shiftManager.forceReplace(shift, curId, newId);
    }

    public void removeEmployee(Shift shift, int employeeId) {
        shiftManager.removeEmployee(shift, employeeId);
    }

    public boolean hasRequests() {
        return assignments.hasRequests();
    }

    public void publishWeekSchedule(Branch branch) {
        LocalDate thisSunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        shiftManager.publishWeekSchedule(branch, thisSunday);
    }

    public void publishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftManager.publishWeekSchedule(branch, nextSunday);
    }

    public void forcePublishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftManager.forcePublishWeekSchedule(branch, nextSunday);
    }

    public void publishLastWeekSchedule(Branch branch) {
        LocalDate lastSunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .minusWeeks(1);
        shiftManager.publishWeekSchedule(branch, lastSunday);
    }

    // test function for adding past shifts (mainly for history purposes)
    public void publishWeekByDate(Branch branch, LocalDate date) {;
        shiftManager.publishWeekSchedule(branch, date);
    }

    public boolean isShiftAssigned(Shift shift) {
        return shiftManager.isShiftAssigned(shift);
    }

    public String getAvailableEmployeesForShift(Shift shift) {
        return shiftManager.getUnassignedValid(shift);
    }

    public String displayWeekAssignments(Branch branch) {
        return shiftManager.displayWeekAssignments(branch);
    }

    public String displayCurrentWeek(Branch branch) {
        return shiftManager.displayPublishedWeek(branch, LocalDate.now());
    }

    public String displayNextWeek(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftManager.displayPublishedWeek(branch, nextSunday);
    }

    public String getShiftHistory(Branch branch) {
        return shiftManager.ShiftHistory(branch);
    }

    public String getShiftDetails(Shift shift) {
        return shiftManager.getShiftDetails(shift);
    }

    public String getEmployeeShifts(int id) {
        return shiftManager.employeeWeekDisplay(id, LocalDate.now());
    }

    public String getNextWeekEmployeeShifts(int id) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftManager.employeeWeekDisplay(id, nextSunday);
    }

    public WeekStatus getWeekStatus(Branch branch) {
        return shiftManager.getWeekStatus(branch, shiftManager.getNextWeek().getStartOfWeek());
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        if (hours < 0 || hours > 4) {
            throw new IllegalArgumentException("The number of hours off must be between 0 and 4.");
        }
        shiftManager.updateExtraHours(shift, empID, hours);
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        shiftManager.sendRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        shiftManager.sendRequest(shift, curId, newId);
    }

    public void approveNextAssignment(int employeeId) {
        shiftManager.approveNextAssignment(employeeId);
    }

    public boolean assignmentNeedsApproval(int employeeId) {
        return shiftManager.assignmentNeedsApproval(employeeId);
    }

    public String displayNextPendingAssignment(int employeeId) {
        return shiftManager.displayNextPendingAssignment(employeeId);
    }

    public void processRequest(int employeeId, boolean b) {
        shiftManager.processRequest(employeeId, b);
    }

    public List<String> popRequestAnswers(Branch branch) {
        return assignments.popRequestAnswers(branch);
    }

    public void getDriversReqs(Branch branch) {
        shiftManager.getDriverReqs(branch);
    }

    public void getStoreKeeperReqs(Branch branch) {
        shiftManager.getStoreKeeperReqs(branch);
    }

    public void setCashierWeekReqs(Branch branch, int amount) {
        shiftManager.setCashierWeekReqs(branch, amount);
    }

    public void setStoreKeeperWeekReqs(Branch branch, int amount) {
        shiftManager.setStoreKeeperWeekReqs(branch, amount);
    }

    public void initShiftsWeek(Branch branch) {
        shiftManager.initShiftsWeek(branch);
    }

    public boolean pendingRequestsLeft() {
        return assignments.pendingRequestsLeft();
    }
}